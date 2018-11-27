package sourcecode

import scala.language.implicitConversions
import scala.quoted.Exprs.TastyTreeExpr
import scala.quoted.{Expr, LiftExprOps, Type}
import scala.tasty.Tasty

trait NameMacros {
  inline implicit def generate: Name =
    ~Macros.nameImpl
}

trait NameMachineMacros {
  inline implicit def generate: Name.Machine =
    ~Macros.nameMachineImpl
}

trait FullNameMacros {
  inline implicit def generate: FullName =
    ~Macros.fullNameImpl
}

trait FullNameMachineMacros {
  inline implicit def generate: FullName.Machine =
    ~Macros.fullNameMachineImpl
}

trait FileMacros {
  inline implicit def generate: sourcecode.File =
    ~Macros.fileImpl
}

trait LineMacros {
  inline implicit def generate: sourcecode.Line =
    ~Macros.lineImpl
}

trait EnclosingMacros {
  inline implicit def generate: Enclosing =
    ~Macros.enclosingImpl
}

trait EnclosingMachineMacros {
  inline implicit def generate: Enclosing.Machine =
    ~Macros.enclosingMachineImpl
}

trait PkgMacros {
  inline implicit def generate: Pkg =
    ~Macros.pkgImpl
}

trait TextMacros {
  inline implicit def generate[T](v: => T): Text[T] = ~Macros.text('(v))
  inline def apply[T](v: => T): Text[T] = ~Macros.text('(v))
}

trait ArgsMacros {
  inline implicit def generate: Args =
    ~Macros.argsImpl
}

object Util{
  def isSynthetic(c: Tasty)(s: c.Symbol) = isSyntheticName(getName(c)(s))
  def isSyntheticName(name: String) = {
    name == "<init>" || (name.startsWith("<local ") && name.endsWith(">"))
  }
  def getName(c: Tasty)(s: c.Symbol) = {
    import c._
    s.name.trim
      .stripSuffix("$") // meh
  }
}

object Macros {

  def actualOwner(c: Tasty)(owner: c.Symbol): c.Symbol = {
    import c._
    var owner0 = owner
    // second condition is meh
    while(Util.isSynthetic(c)(owner0) || Util.getName(c)(owner0) == "ev") {
      owner0 = owner0.owner
    }
    owner0
  }

  def nameImpl(implicit c: Tasty): Expr[Name] = {
    import c._
    val owner = actualOwner(c)(c.rootContext.owner)
    val simpleName = Util.getName(c)(owner)
    '(Name(~simpleName.toExpr))
  }

  private def adjustName(s: String): String =
    // Required to get the same name from dotty
    if (s.startsWith("<local ") && s.endsWith("$>"))
      s.stripSuffix("$>") + ">"
    else
      s

  def nameMachineImpl(implicit c: Tasty): Expr[Name.Machine] = {
    import c._
    val owner = c.rootContext.owner
    val simpleName = adjustName(Util.getName(c)(owner))
    '(Name.Machine(~simpleName.toExpr))
  }

  def fullNameImpl(implicit c: Tasty): Expr[FullName] = {
    import c._
    val owner = actualOwner(c)(c.rootContext.owner)
    val fullName =
      owner.fullName.trim
        .split("\\.", -1)
        .filterNot(Util.isSyntheticName)
        .map(_.stripPrefix("_$").stripSuffix("$")) // meh
        .mkString(".")
    '(FullName(~fullName.toExpr))
  }

  def fullNameMachineImpl(implicit c: Tasty): Expr[FullName.Machine] = {
    import c._
    val owner = c.rootContext.owner
    val fullName = owner.fullName.trim
      .split("\\.", -1)
      .map(_.stripPrefix("_$").stripSuffix("$")) // meh
      .map(adjustName)
      .mkString(".")
    '(FullName.Machine(~fullName.toExpr))
  }

  def fileImpl(implicit c: Tasty): Expr[sourcecode.File] = {
    import c._
    val file = c.rootPosition.sourceFile.toAbsolutePath.toString
    '(sourcecode.File(~file.toExpr))
  }

  def lineImpl(implicit c: Tasty): Expr[sourcecode.Line] = {
    import c._
    val line = c.rootPosition.startLine + 1
    '(sourcecode.Line(~line.toExpr))
  }

  def enclosingImpl(implicit c: Tasty): Expr[Enclosing] = {
    val path = enclosing(c)(
      !Util.isSynthetic(c)(_)
    )

    '(Enclosing(~path.toExpr))
  }

  def enclosingMachineImpl(implicit c: Tasty): Expr[Enclosing.Machine] = {
    val path = enclosing(c, machine = true)(_ => true)
    '(Enclosing.Machine(~path.toExpr))
  }

  def pkgImpl(implicit c: Tasty): Expr[Pkg] = {
    import c._
    val path = enclosing(c)(
      // _.isPackage
      s => s.tree match {
        case Some(PackageDef(_)) => true
        case _ => false
      }
    )

    '(Pkg(~path.toExpr))
  }

  def argsImpl(implicit c: Tasty): Expr[Args] = {
    import c._

    val param: List[List[c.ValDef]] = {
      def nearestEnclosingMethod(owner: c.Symbol): List[List[c.ValDef]] =
        owner.tree match {
          case Some(DefDef((_, _, paramss, _, _))) =>
            paramss
          case Some(ClassDef((_, constructor, _, _, _))) =>
            constructor.paramss
          case Some(ValDef(_, _, rhs)) =>
            nearestEnclosingMethod(owner.owner)
          case _ =>
            nearestEnclosingMethod(owner.owner)
        }

      nearestEnclosingMethod(c.rootContext.owner)
    }

    val texts0 = param.map(_.foldRight('(List.empty[Text[_]])) {
      case (vd @ ValDef(nme, _, optV), l) =>
        '(Text(~{optV.fold('(None))(v => new TastyTreeExpr(v))}, ~nme.toExpr) :: ~l)
    })
    val texts = texts0.foldRight('(List.empty[List[Text[_]]])) {
      case (l, acc) =>
        '(~l :: ~acc)
    }

    '(Args(~texts))
  }


  def text[T: Type](v: Expr[T])(implicit c: Tasty): Expr[sourcecode.Text[T]] = {
    import c._
    import scala.quoted.Toolbox.Default._
    val txt = v.show
    '(sourcecode.Text[T](~v, ~txt.toExpr))
  }

  sealed trait Chunk
  object Chunk{
    case class PkgObj(name: String) extends Chunk
    case class ClsTrt(name: String) extends Chunk
    case class ValVarLzyDef(name: String) extends Chunk

  }

  def enclosing(c: Tasty, machine: Boolean = false)(filter: c.Symbol => Boolean): String = {

    import c._
    var current = c.rootContext.owner
    if (!machine)
      current = actualOwner(c)(current)
    var path = List.empty[Chunk]
    while(current.toString != "NoSymbol" && current != definitions.RootPackage && current != definitions.RootClass){
      if (filter(current)) {

        val chunk = current.tree match {
          case Some(ValDef(_)) => Chunk.ValVarLzyDef
          case Some(DefDef(_)) => Chunk.ValVarLzyDef
          case _ => Chunk.PkgObj
        }

        // TODO
        // val chunk = current match {
        //   case x if x.flags.isPackage => Chunk.PkgObj
        //   case x if x.flags.isModuleClass => Chunk.PkgObj
        //   case x if x.flags.isClass && x.asClass.isTrait => Chunk.ClsTrt
        //   case x if x.flags.isClass => Chunk.ClsTrt
        //   case x if x.flags.isMethod => Chunk.ValVarLzyDef
        //   case x if x.flags.isTerm && x.asTerm.isVar => Chunk.ValVarLzyDef
        //   case x if x.flags.isTerm && x.asTerm.isLazy => Chunk.ValVarLzyDef
        //   case x if x.flags.isTerm && x.asTerm.isVal => Chunk.ValVarLzyDef
        // }
        //
        // path = chunk(Util.getName(c)(current)) :: path

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
