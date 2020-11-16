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
  def isSynthetic(using QuoteContext)(s: qctx.reflect.Symbol) = isSyntheticName(getName(s))
  def isSyntheticName(name: String) = {
    name == "<init>" || (name.startsWith("<local ") && name.endsWith(">")) || name == "$anonfun" || name == "macro"
  }
  def getName(using QuoteContext)(s: qctx.reflect.Symbol) = {
    s.name.trim
      .stripSuffix("$") // meh
  }
}

object Macros {

  def findOwner(using QuoteContext)(owner: qctx.reflect.Symbol, skipIf: qctx.reflect.Symbol => Boolean): qctx.reflect.Symbol = {
    var owner0 = owner
    while(skipIf(owner0)) owner0 = owner0.owner
    owner0
  }

  def actualOwner(using QuoteContext)(owner: qctx.reflect.Symbol): qctx.reflect.Symbol =
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
  def nonMacroOwner(using QuoteContext)(owner: qctx.reflect.Symbol): qctx.reflect.Symbol =
    findOwner(owner, owner0 => { owner0.flags.is(qctx.reflect.Flags.Macro) && Util.getName(owner0) == "macro"})

  def nameImpl(using QuoteContext): Expr[Name] = {
    import qctx.reflect._
    val owner = actualOwner(Symbol.currentOwner)
    val simpleName = Util.getName(owner)
    '{Name(${Expr(simpleName)})}
  }

  private def adjustName(s: String): String =
    // Required to get the same name from dotty
    if (s.startsWith("<local ") && s.endsWith("$>"))
      s.stripSuffix("$>") + ">"
    else
      s

  def nameMachineImpl(using QuoteContext): Expr[Name.Machine] = {
    import qctx.reflect._
    val owner = nonMacroOwner(Symbol.currentOwner)
    val simpleName = adjustName(Util.getName(owner))
    '{Name.Machine(${Expr(simpleName)})}
  }

  def fullNameImpl(using QuoteContext): Expr[FullName] = {
    import qctx.reflect._
    @annotation.tailrec def cleanChunk(chunk: String): String =
      val refined = chunk.stripPrefix("_$").stripSuffix("$")
      if chunk != refined then cleanChunk(refined) else refined

    val owner = actualOwner(Symbol.currentOwner)
    val fullName =
      owner.fullName.trim
        .split("\\.", -1)
        .filterNot(Util.isSyntheticName)
        .map(cleanChunk)
        .mkString(".")
    '{FullName(${Expr(fullName)})}
  }

  def fullNameMachineImpl(using QuoteContext): Expr[FullName.Machine] = {
    import qctx.reflect._
    val owner = nonMacroOwner(Symbol.currentOwner)
    val fullName = owner.fullName.trim
      .split("\\.", -1)
      .map(_.stripPrefix("_$").stripSuffix("$")) // meh
      .map(adjustName)
      .mkString(".")
    '{FullName.Machine(${Expr(fullName)})}
  }

  def fileImpl(using QuoteContext): Expr[sourcecode.File] = {
    import qctx.reflect._
    val file = qctx.reflect.Position.ofMacroExpansion.sourceFile.jpath.toAbsolutePath.toString
    '{sourcecode.File(${Expr(file)})}
  }

  def fileNameImpl(using QuoteContext): Expr[sourcecode.FileName] = {
    val name = qctx.reflect.Position.ofMacroExpansion.sourceFile.jpath.getFileName.toString
    '{sourcecode.FileName(${Expr(name)})}
  }

  def lineImpl(using QuoteContext): Expr[sourcecode.Line] = {
    val line = qctx.reflect.Position.ofMacroExpansion.startLine + 1
    '{sourcecode.Line(${Expr(line)})}
  }

  def enclosingImpl(using QuoteContext): Expr[Enclosing] = {
    import qctx.reflect._
    val path = enclosing(machine = false)(!Util.isSynthetic(_))
    '{Enclosing(${Expr(path)})}
  }

  def enclosingMachineImpl(using QuoteContext): Expr[Enclosing.Machine] = {
    val path = enclosing(machine = true)(_ => true)
    '{Enclosing.Machine(${Expr(path)})}
  }

  def pkgImpl(using QuoteContext): Expr[Pkg] = {
    val path = enclosing(machine = false) {
      case s if s.isPackageDef => true
      case _ => false
    }

    '{Pkg(${Expr(path)})}
  }

  def argsImpl(using qctx: QuoteContext): Expr[Args] = {
    import qctx.reflect._

    val param: List[List[ValDef]] = {
      def nearestEnclosingMethod(owner: Symbol): List[List[ValDef]] =
        owner match {
          case defSym if defSym.isDefDef =>
            defSym.tree.asInstanceOf[DefDef].paramss
          case classSym if classSym.isClassDef =>
            classSym.tree.asInstanceOf[ClassDef].constructor.paramss
          case _ =>
            nearestEnclosingMethod(owner.owner)
        }

      nearestEnclosingMethod(Symbol.currentOwner)
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


  def text[T: Type](v: Expr[T])(using QuoteContext): Expr[sourcecode.Text[T]] = {
    import qctx.reflect._
    val txt = Term.of(v).pos.sourceCode
    '{sourcecode.Text[T]($v, ${Expr(txt)})}
  }

  sealed trait Chunk
  object Chunk{
    case class PkgObj(name: String) extends Chunk
    case class ClsTrt(name: String) extends Chunk
    case class ValVarLzyDef(name: String) extends Chunk

  }

  def enclosing(using QuoteContext)(machine: Boolean)(filter: qctx.reflect.Symbol => Boolean): String = {
    import qctx.reflect._

    var current = Symbol.currentOwner
    if (!machine)
      current = actualOwner(current)
    else
      current = nonMacroOwner(current)
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
