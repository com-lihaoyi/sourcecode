package sourcecode

import scala.language.implicitConversions
import scala.quoted._

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
  def isSynthetic(using Quotes)(s: quotes.reflect.Symbol) = isSyntheticName(getName(s))
  def isSyntheticName(name: String) = {
    name == "<init>" || (name.startsWith("<local ") && name.endsWith(">")) || name == "$anonfun" || name == "macro"
  }
  def getName(using Quotes)(s: quotes.reflect.Symbol) = {
    s.name.trim
      .stripSuffix("$") // meh
  }
}

object Macros {

  def findOwner(using Quotes)(owner: quotes.reflect.Symbol, skipIf: quotes.reflect.Symbol => Boolean): quotes.reflect.Symbol = {
    var owner0 = owner
    while(skipIf(owner0)) owner0 = owner0.owner
    owner0
  }

  def actualOwner(using Quotes)(owner: quotes.reflect.Symbol): quotes.reflect.Symbol =
    findOwner(owner, owner0 => Util.isSynthetic(owner0) || Util.getName(owner0) == "ev")

  /**
   * In Scala 3, macro `mcr()` is expanded to:
   *
   * val macro = ...
   * macro
   *
   * Where n is an ordinal. This method returns the first owner that is not
   * such a synthetic variable.
   */
  def nonMacroOwner(using Quotes)(owner: quotes.reflect.Symbol): quotes.reflect.Symbol =
    findOwner(owner, owner0 => { owner0.flags.is(quotes.reflect.Flags.Macro) && Util.getName(owner0) == "macro"})

  def nameImpl(using Quotes): Expr[Name] = {
    import quotes.reflect._
    val owner = actualOwner(Symbol.spliceOwner)
    val simpleName = Util.getName(owner)
    '{Name(${Expr(simpleName)})}
  }

  private def adjustName(s: String): String =
    // Required to get the same name from dotty
    if (s.startsWith("<local ") && s.endsWith("$>"))
      s.stripSuffix("$>") + ">"
    else
      s

  def nameMachineImpl(using Quotes): Expr[Name.Machine] = {
    import quotes.reflect._
    val owner = nonMacroOwner(Symbol.spliceOwner)
    val simpleName = adjustName(Util.getName(owner))
    '{Name.Machine(${Expr(simpleName)})}
  }

  def fullNameImpl(using Quotes): Expr[FullName] = {
    import quotes.reflect._
    @annotation.tailrec def cleanChunk(chunk: String): String =
      val refined = chunk.stripPrefix("_$").stripSuffix("$")
      if chunk != refined then cleanChunk(refined) else refined

    val owner = actualOwner(Symbol.spliceOwner)
    val fullName =
      owner.fullName.trim
        .split("\\.", -1)
        .filterNot(Util.isSyntheticName)
        .map(cleanChunk)
        .mkString(".")
    '{FullName(${Expr(fullName)})}
  }

  def fullNameMachineImpl(using Quotes): Expr[FullName.Machine] = {
    import quotes.reflect._
    val owner = nonMacroOwner(Symbol.spliceOwner)
    val fullName = owner.fullName.trim
      .split("\\.", -1)
      .map(_.stripPrefix("_$").stripSuffix("$")) // meh
      .map(adjustName)
      .mkString(".")
    '{FullName.Machine(${Expr(fullName)})}
  }

  def fileImpl(using Quotes): Expr[sourcecode.File] = {
    import quotes.reflect._
    val file = quotes.reflect.Position.ofMacroExpansion.sourceFile.jpath.toAbsolutePath.toString
    '{sourcecode.File(${Expr(file)})}
  }

  def fileNameImpl(using Quotes): Expr[sourcecode.FileName] = {
    val name = quotes.reflect.Position.ofMacroExpansion.sourceFile.jpath.getFileName.toString
    '{sourcecode.FileName(${Expr(name)})}
  }

  def lineImpl(using Quotes): Expr[sourcecode.Line] = {
    val line = quotes.reflect.Position.ofMacroExpansion.startLine + 1
    '{sourcecode.Line(${Expr(line)})}
  }

  def enclosingImpl(using Quotes): Expr[Enclosing] = {
    import quotes.reflect._
    val path = enclosing(machine = false)(!Util.isSynthetic(_))
    '{Enclosing(${Expr(path)})}
  }

  def enclosingMachineImpl(using Quotes): Expr[Enclosing.Machine] = {
    val path = enclosing(machine = true)(_ => true)
    '{Enclosing.Machine(${Expr(path)})}
  }

  def pkgImpl(using Quotes): Expr[Pkg] = {
    val path = enclosing(machine = false) {
      case s if s.isPackageDef => true
      case _ => false
    }

    '{Pkg(${Expr(path)})}
  }

  def argsImpl(using qctx: Quotes): Expr[Args] = {
    import quotes.reflect._

    val param: List[List[ValDef]] = {
      def nearestEnclosingMethod(owner: Symbol): List[List[ValDef]] =
        owner match {
          case defSym if defSym.isDefDef =>
            defSym.tree.asInstanceOf[DefDef].paramss
              // FIXME Could be a List[TypeDef] too, although I'm not
              // sure under which conditions this can happen…
              .map(_.asInstanceOf[List[ValDef]])
          case classSym if classSym.isClassDef =>
            classSym.tree.asInstanceOf[ClassDef].constructor.paramss
              // FIXME Could be a List[TypeDef] too, although I'm not
              // sure under which conditions this can happen…
              .map(_.asInstanceOf[List[ValDef]])
          case _ =>
            nearestEnclosingMethod(owner.owner)
        }

      nearestEnclosingMethod(Symbol.spliceOwner)
    }

    val texts0 = param.map(_.foldRight('{List.empty[Text[_]]}) {
      case (vd @ ValDef(nme, _, optV), l) =>
        '{Text(${optV.fold('None)(_.asExpr)}, ${Expr(nme)}) :: $l}
    })
    val texts = texts0.foldRight('{List.empty[List[Text[_]]]}) {
      case (l, acc) =>
        '{$l :: $acc}
    }

    '{Args($texts)}
  }


  def text[T: Type](v: Expr[T])(using Quotes): Expr[sourcecode.Text[T]] = {
    import quotes.reflect._
    val txt = v.asTerm.pos.sourceCode.get
    '{sourcecode.Text[T]($v, ${Expr(txt)})}
  }

  sealed trait Chunk
  object Chunk{
    case class PkgObj(name: String) extends Chunk
    case class ClsTrt(name: String) extends Chunk
    case class ValVarLzyDef(name: String) extends Chunk

  }

  def enclosing(using Quotes)(machine: Boolean)(filter: quotes.reflect.Symbol => Boolean): String = {
    import quotes.reflect._

    var current = Symbol.spliceOwner
    if (!machine)
      current = actualOwner(current)
    else
      current = nonMacroOwner(current)
    var path = List.empty[Chunk]
    while(current != Symbol.noSymbol && current != defn.RootPackage && current != defn.RootClass){
      if (filter(current)) {

        val chunk = current match {
          case sym if
            sym.isValDef || sym.isDefDef => Chunk.ValVarLzyDef.apply
          case sym if
            sym.isPackageDef ||
            sym.moduleClass != Symbol.noSymbol => Chunk.PkgObj.apply
          case sym if sym.isClassDef => Chunk.ClsTrt.apply
          case _ => Chunk.PkgObj.apply
        }

        path = chunk(Util.getName(current).stripSuffix("$")) :: path
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
