package sourcecode

import scala.language.implicitConversions
import scala.quoted._
import scala.tasty.Reflection

trait NameMacros {
  inline implicit def generate: Name =
    ${ Macros.nameImpl }
}

trait NameMachineMacros {
  inline implicit def generate: Name.Machine =
    ${ Macros.nameMachineImpl }
}

trait FullNameMacros {
  inline implicit def generate: FullName =
    ${ Macros.fullNameImpl }
}

trait FullNameMachineMacros {
  inline implicit def generate: FullName.Machine =
    ${ Macros.fullNameMachineImpl }
}

trait FileMacros {
  inline implicit def generate: sourcecode.File =
    ${ Macros.fileImpl }
}

trait FileNameMacros {
  inline implicit def generate: sourcecode.FileName =
    ${ Macros.fileNameImpl }
}

trait LineMacros {
  inline implicit def generate: sourcecode.Line =
    ${ Macros.lineImpl }
}

trait EnclosingMacros {
  inline implicit def generate: Enclosing =
    ${ Macros.enclosingImpl }
}

trait EnclosingMachineMacros {
  inline implicit def generate: Enclosing.Machine =
    ${ Macros.enclosingMachineImpl }
}

trait PkgMacros {
  inline implicit def generate: Pkg =
    ${ Macros.pkgImpl }
}

trait TextMacros {
  inline implicit def generate[T](v: => T): Text[T] = ${ Macros.text('v) }
  inline def apply[T](v: => T): Text[T] = ${ Macros.text('v) }
}

trait ArgsMacros {
  inline implicit def generate: Args =
    ${ Macros.argsImpl }
}

object Util{
  def isSynthetic(c: Reflection)(s: c.Symbol) = isSyntheticName(getName(c)(s))
  def isSyntheticName(name: String) = {
    name == "<init>" || (name.startsWith("<local ") && name.endsWith(">")) || name == "$anonfun" || name.startsWith("macro$")
  }
  def getName(c: Reflection)(s: c.Symbol) = {
    import c.given
    s.name.trim
      .stripSuffix("$") // meh
  }
}

object Macros {

  def findOwner(c: Reflection)(owner: c.Symbol, skipIf: (c: Reflection) => (c.Symbol) => Boolean): c.Symbol = {
    import c.given
    var owner0 = owner
    while(skipIf(c)(owner0)) owner0 = owner0.owner
    owner0
  }

  def actualOwner(c: Reflection)(owner: c.Symbol): c.Symbol =
    findOwner(c)(owner, c => owner0 => Util.isSynthetic(c)(owner0) || Util.getName(c)(owner0) == "ev")

  /**
   * In Scala 3, macro `mcr()` is expanded to:
   *
   * val macro$n = ...
   * macro$n
   *
   * Where n is an ordinal. This method returns the first owner that is not
   * such a synthetic variable.
   */
  def nonMacroOwner(c: Reflection)(owner: c.Symbol): c.Symbol =
    findOwner(c)(owner, c => owner0 => Util.getName(c)(owner0).startsWith("macro$"))

  def nameImpl(given ctx: QuoteContext): Expr[Name] = {
    import ctx.tasty.given
    val owner = actualOwner(ctx.tasty)(ctx.tasty.rootContext.owner)
    val simpleName = Util.getName(ctx.tasty)(owner)
    '{Name(${Expr(simpleName)})}
  }

  private def adjustName(s: String): String =
    // Required to get the same name from dotty
    if (s.startsWith("<local ") && s.endsWith("$>"))
      s.stripSuffix("$>") + ">"
    else
      s

  def nameMachineImpl(given ctx: QuoteContext): Expr[Name.Machine] = {
    import ctx.tasty.given
    val owner = nonMacroOwner(ctx.tasty)(ctx.tasty.rootContext.owner)
    val simpleName = adjustName(Util.getName(ctx.tasty)(owner))
    '{Name.Machine(${Expr(simpleName)})}
  }

  def fullNameImpl(given ctx: QuoteContext): Expr[FullName] = {
    import ctx.tasty.given
    @annotation.tailrec def cleanChunk(chunk: String): String =
      val refined = chunk.stripPrefix("_$").stripSuffix("$")
      if chunk != refined then cleanChunk(refined) else refined

    val owner = actualOwner(ctx.tasty)(ctx.tasty.rootContext.owner)
    val fullName =
      owner.fullName.trim
        .split("\\.", -1)
        .filterNot(Util.isSyntheticName)
        .map(cleanChunk)
        .mkString(".")
    '{FullName(${Expr(fullName)})}
  }

  def fullNameMachineImpl(given ctx: QuoteContext): Expr[FullName.Machine] = {
    import ctx.tasty.given
    val owner = nonMacroOwner(ctx.tasty)(ctx.tasty.rootContext.owner)
    val fullName = owner.fullName.trim
      .split("\\.", -1)
      .map(_.stripPrefix("_$").stripSuffix("$")) // meh
      .map(adjustName)
      .mkString(".")
    '{FullName.Machine(${Expr(fullName)})}
  }

  def fileImpl(given ctx: QuoteContext): Expr[sourcecode.File] = {
    import ctx.tasty.given
    val file = ctx.tasty.rootPosition.sourceFile.jpath.toAbsolutePath.toString
    '{sourcecode.File(${Expr(file)})}
  }

  def fileNameImpl(given ctx: QuoteContext): Expr[sourcecode.FileName] = {
    import ctx.tasty.given
    val name = ctx.tasty.rootPosition.sourceFile.jpath.getFileName.toString
    '{sourcecode.FileName(${Expr(name)})}
  }

  def lineImpl(given ctx: QuoteContext): Expr[sourcecode.Line] = {
    import ctx.tasty.given
    val line = ctx.tasty.rootPosition.startLine + 1
    '{sourcecode.Line(${Expr(line)})}
  }

  def enclosingImpl(given ctx: QuoteContext): Expr[Enclosing] = {
    val path = enclosing(ctx.tasty)(
      !Util.isSynthetic(ctx.tasty)(_)
    )

    '{Enclosing(${Expr(path)})}
  }

  def enclosingMachineImpl(given ctx: QuoteContext): Expr[Enclosing.Machine] = {
    val path = enclosing(ctx.tasty, machine = true)(_ => true)
    '{Enclosing.Machine(${Expr(path)})}
  }

  def pkgImpl(given ctx: QuoteContext): Expr[Pkg] = {
    import ctx.tasty.given
    val path = enclosing(ctx.tasty) {
      case s if s.isPackageDef => true
      case _ => false
    }

    '{Pkg(${Expr(path)})}
  }

  def argsImpl(given ctx: QuoteContext): Expr[Args] = {
    import ctx.tasty.{ _, given }

    val param: List[List[ctx.tasty.ValDef]] = {
      def nearestEnclosingMethod(owner: ctx.tasty.Symbol): List[List[ctx.tasty.ValDef]] =
        owner match {
          case defSym if defSym.isDefDef =>
            defSym.tree.asInstanceOf[DefDef].paramss
          case classSym if classSym.isClassDef =>
            classSym.tree.asInstanceOf[ClassDef].constructor.paramss
          case _ =>
            nearestEnclosingMethod(owner.owner)
        }

      nearestEnclosingMethod(ctx.tasty.rootContext.owner)
    }

    val texts0 = param.map(_.foldRight('{List.empty[Text[_]]}) {
      case (vd @ ValDef(nme, _, optV), l) =>
        '{Text(${optV.fold('None)(_.seal)}, ${Expr(nme)}) :: $l}
    })
    val texts = texts0.foldRight('{List.empty[List[Text[_]]]}) {
      case (l, acc) =>
        '{$l :: $acc}
    }

    '{Args($texts)}
  }


  def text[T: Type](v: Expr[T])(given ctx: QuoteContext): Expr[sourcecode.Text[T]] = {
    import ctx.tasty.given
    val txt = v.unseal.pos.sourceCode
    '{sourcecode.Text[T]($v, ${Expr(txt)})}
  }

  sealed trait Chunk
  object Chunk{
    case class PkgObj(name: String) extends Chunk
    case class ClsTrt(name: String) extends Chunk
    case class ValVarLzyDef(name: String) extends Chunk

  }

  def enclosing(c: Reflection, machine: Boolean = false)(filter: c.Symbol => Boolean): String = {
    import c.{ _, given }

    var current = c.rootContext.owner
    if (!machine)
      current = actualOwner(c)(current)
    else
      current = nonMacroOwner(c)(current)
    var path = List.empty[Chunk]
    while(current != Symbol.noSymbol && current != defn.RootPackage && current != defn.RootClass){
      if (filter(current)) {

        val chunk = current match {
          case sym if
            sym.isValDef || sym.isDefDef => Chunk.ValVarLzyDef
          case sym if
            sym.isPackageDef ||
            sym.moduleClass != Symbol.noSymbol => Chunk.PkgObj
          case sym if sym.isClassDef => Chunk.ClsTrt
          case _ => Chunk.PkgObj
        }

        path = chunk(Util.getName(c)(current).stripSuffix("$")) :: path
      }
      current = current.owner
    }
    path.map{
      case Chunk.PkgObj(s) => adjustName(s) + "."
      case Chunk.ClsTrt(s) => adjustName(s) + "#"
      case Chunk.ValVarLzyDef(s) => adjustName(s) + " "
    }.mkString.dropRight(1)
  }
}
