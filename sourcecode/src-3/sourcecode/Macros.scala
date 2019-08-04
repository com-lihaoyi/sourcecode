package sourcecode

import language.experimental.macros
import scala.quoted._
import scala.tasty._

abstract class NameMacros {
  inline given as Name = ${Macros.nameImpl}
}

abstract class NameMachineMacros {
  inline given as Name.Machine = ${Macros.nameMachineImpl}
}

abstract class FullNameMacros {
  inline given as FullName = ${Macros.fullNameImpl}
}

abstract class FullNameMachineMacros {
  inline given as FullName.Machine = ${Macros.fullNameMachineImpl}
}

abstract class FileMacros {
  inline given as File = ${Macros.fileImpl}
}

abstract class LineMacros {
  inline given as sourcecode.Line = ${Macros.lineImpl}
}

abstract class EnclosingMacros {
  inline given as Enclosing = ${Macros.enclosingImpl}
}

abstract class EnclosingMachineMacros {
  inline given as Enclosing.Machine = ${Macros.enclosingMachineImpl}
}

abstract class PkgMacros {
  inline given as Pkg = ${Macros.pkgImpl}
}

abstract class TextMacros {
  // given [T] as Conversion[T, Text[T]] {
  //   inline def apply(v: T): Text[T] = ${Macros.text[T]('v)}
  // }
  import scala.language.implicitConversions
  inline implicit def generate[T](v: T): Text[T] = ${Macros.text[T]('v)}
  inline def apply[T](v: T): Text[T] = ${Macros.text[T]('v)}
}

abstract class ArgsMacros {
  inline given as Args = ${Macros.argsImpl}
}

object Util{
  def isSynthetic(qctx: QuoteContext)(s: qctx.tasty.Symbol): Boolean = isSyntheticName(getName(qctx)(s))
  def isSyntheticName(name: String) = {
    name == "<init>" || (name.startsWith("<local ") && name.endsWith(">"))
  }
  def getName(qctx: QuoteContext)(s: qctx.tasty.Symbol): String = {
    import qctx.tasty._
    s.name.trim
  }
  def cleanName(name0: String): String = {
    name0 match {
      case name if name.endsWith("$")    => cleanName(name.dropRight(1))
      case name if name.startsWith("_$") => cleanName(name.drop(2))
      case _ => name0
    }
  }
  def literal(qctx: QuoteContext)(value: String): Expr[String] = {
    import qctx.tasty._
    Literal(Constant(value)).seal.asInstanceOf[Expr[String]]
  }
  def literal(qctx: QuoteContext)(value: Int): Expr[Int] = {
    import qctx.tasty._
    Literal(Constant(value)).seal.asInstanceOf[Expr[Int]]
  }
}

object Macros {

  def nameImpl given (qctx: QuoteContext): Expr[Name] = {
    import qctx.tasty._
    var owner = rootContext.owner
    while(Util.isSynthetic(qctx)(owner)) owner = owner.owner
    val simpleName = Util.cleanName(Util.getName(qctx)(owner))
    '{ Name(${Util.literal(qctx)(simpleName)}) }
  }

  def nameMachineImpl given (qctx: QuoteContext): Expr[Name.Machine] = {
    import qctx.tasty._
    val owner = rootContext.owner
    val simpleName = Util.getName(qctx)(owner)
    '{ Name.Machine(${Util.literal(qctx)(simpleName)}) }
  }

  def fullNameImpl given (qctx: QuoteContext): Expr[FullName] = {
    import qctx.tasty._
    val owner = rootContext.owner
    val fullName =
      owner.fullName.trim
        .split("\\.", -1)
        .filterNot(Util.isSyntheticName)
        .map(Util.cleanName)
        .mkString(".")
    '{ FullName(${Util.literal(qctx)(fullName)}) }
  }

  def fullNameMachineImpl given (qctx: QuoteContext): Expr[FullName.Machine] = {
    import qctx.tasty._
    val owner = rootContext.owner
    val fullName = owner.fullName.trim
    '{ FullName.Machine(${Util.literal(qctx)(fullName)}) }
  }

  def fileImpl given (qctx: QuoteContext): Expr[File] = {
    import qctx.tasty._
    val file = rootContext.source
    '{ sourcecode.File(${Util.literal(qctx)(file.toString)}) }
  }

  def lineImpl given (qctx: QuoteContext): Expr[sourcecode.Line] = {
    import qctx.tasty._
    val line = rootPosition.startLine + 1
    '{ Line(${Util.literal(qctx)(line)}) }
  }

  def enclosingImpl given (qctx: QuoteContext): Expr[Enclosing] = {
    val path = enclosing(qctx)(!Util.isSynthetic(qctx)(_))
    '{ Enclosing(${Util.literal(qctx)(path)}) }
  }

  def enclosingMachineImpl given (qctx: QuoteContext): Expr[Enclosing.Machine] = {
    val path = enclosing(qctx)(_ => true)
    '{ Enclosing.Machine(${Util.literal(qctx)(path)}) }
  }

  def pkgImpl given (qctx: QuoteContext): Expr[Pkg] = {
    import qctx.tasty._
    val path = enclosing(qctx)(_ match {
      case IsPackageDefSymbol(_) => true
      case _                     => false
    })
    '{ Pkg(${Util.literal(qctx)(path)}) }
  }

  def argsImpl given (qctx: QuoteContext): Expr[Args] = {
    import qctx.tasty._
    // import quoted._

    def nearestEnclosingMethod(owner: Symbol): Symbol =
      owner match {
        case IsDefDefSymbol(x)   => x
        case IsClassDefSymbol(x) => x
        case _                   => nearestEnclosingMethod(owner.owner)
      }
    def enclosingParamList: Seq[Seq[Symbol]] = {
      nearestEnclosingMethod(rootContext.owner) match {
        case IsDefDefSymbol(x) =>
          x.tree.paramss map { _ map {
            _.symbol
          }}
        case IsClassDefSymbol(x) =>
          x.tree.constructor.paramss map { _ map {
            _.symbol
          }}
      }
    }
    val param = enclosingParamList
    val texts: Seq[Seq[Expr[Text[_]]]] = param.map(_.map(p =>
      // this causes compiler to crash
      // '{ sourcecode.Text(${Ref(p).seal}, ${Util.literal(qctx)(p.name.toString)}) }
      '{ sourcecode.Text("?", ${Util.literal(qctx)(p.name.toString)}) }
    ))
    val textSeqs: Seq[Expr[Seq[Text[_]]]] = texts map { seq: Seq[Expr[Text[_]]] =>
      '{ Seq(${ Repeated(seq.map(_.unseal).toList, '[Text[_]].unseal).seal.asInstanceOf[Expr[Seq[Text[_]]]] }: _*) }
    }
    val seqss = '{ Seq(${ Repeated(textSeqs.map(_.unseal).toList, '[Seq[Text[_]]].unseal).seal.asInstanceOf[Expr[Seq[Seq[Text[_]]]]] }: _*) }
    '{ Args($seqss) }
  }

  def text[T: Type](v: Expr[T]) given (qctx: QuoteContext): Expr[sourcecode.Text[T]] = {
    import qctx.tasty._
    '{ Text($v, ${Util.literal(qctx)(rootPosition.sourceCode)}) }
  }

  enum Chunk {
    case Pkg(name: String)
    case Obj(name: String)
    case Cls(name: String)
    case Trt(name: String)
    case Val(name: String)
    case Var(name: String)
    case Lzy(name: String)
    case Def(name: String)
  }

  def enclosing(qctx: QuoteContext)(filter: qctx.tasty.Symbol => Boolean): String = {
    import qctx.tasty._
    var current = rootContext.owner
    var path = List.empty[Chunk]

    while(current != NoSymbol && current.toString != "package <root>" && current.toString != "module class <root>"){
      if (filter(current)) {

        val chunk = current match {
          case IsPackageDefSymbol(_) => Chunk.Pkg
          case IsClassDefSymbol(x) if x.flags.is(Flags.ModuleClass) => Chunk.Obj
          case IsClassDefSymbol(x) if x.flags.is(Flags.Trait) => Chunk.Trt
          case IsClassDefSymbol(_) => Chunk.Cls
          case IsDefDefSymbol(_) => Chunk.Def
          case IsValDefSymbol(_) => Chunk.Val
        }

        path = chunk(Util.getName(qctx)(current)) :: path
      }
      current = current.owner
    }
    val renderedPath: String = path.map{
      case Chunk.Pkg(s) => s + "."
      case Chunk.Obj(s) => Util.cleanName(s) + "."
      case Chunk.Cls(s) => s + "#"
      case Chunk.Trt(s) => s + "#"
      case Chunk.Val(s) => s + " "
      case Chunk.Var(s) => s + " "
      case Chunk.Lzy(s) => s + " "
      case Chunk.Def(s) => s + " "
    }.mkString.dropRight(1)
    renderedPath
  }

}
