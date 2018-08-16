package sourcecode

import language.experimental.macros


object Util{
  def isSynthetic(c: Compat.Context)(s: c.Symbol) = isSyntheticName(getName(c)(s))
  def isSyntheticName(name: String) = {
    name == "<init>" || (name.startsWith("<local ") && name.endsWith(">"))
  }
  def getName(c: Compat.Context)(s: c.Symbol) = s.name.decoded.toString.trim
}
abstract class SourceValue[T]{
  def value: T
}
abstract class SourceCompanion[T, V <: SourceValue[T]](build: T => V){
  def apply()(implicit s: V): T = s.value
  implicit def wrap(s: T): V = build(s)
}
case class Name(value: String) extends SourceValue[String]
object Name extends SourceCompanion[String, Name](new Name(_)){
  implicit def generate: Name = macro impl

  def impl(c: Compat.Context): c.Expr[Name] = {
    import c.universe._
    var owner = Compat.enclosingOwner(c)
    while(Util.isSynthetic(c)(owner)) owner = owner.owner
    val simpleName = Util.getName(c)(owner)
    c.Expr[sourcecode.Name](q"""${c.prefix}($simpleName)""")
  }
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends SourceCompanion[String, Machine](new Machine(_)){
    implicit def generate: Machine = macro impl
    def impl(c: Compat.Context): c.Expr[Machine] = {
      import c.universe._
      val owner = Compat.enclosingOwner(c)
      val simpleName = Util.getName(c)(owner)
      c.Expr[Machine](q"""${c.prefix}($simpleName)""")
    }
  }
}
case class FullName(value: String) extends SourceValue[String]
object FullName extends SourceCompanion[String, FullName](new FullName(_)){
  implicit def generate: FullName = macro impl

  def impl(c: Compat.Context): c.Expr[FullName] = {
    import c.universe._
    val owner = Compat.enclosingOwner(c)
    val fullName =
      owner.fullName.trim
           .split("\\.", -1)
           .filterNot(Util.isSyntheticName)
           .mkString(".")
    c.Expr[sourcecode.FullName](q"""${c.prefix}($fullName)""")
  }
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends SourceCompanion[String, Machine](new Machine(_)){
    implicit def generate: Machine = macro impl

    def impl(c: Compat.Context): c.Expr[Machine] = {
      import c.universe._
      val owner = Compat.enclosingOwner(c)
      val fullName = owner.fullName.trim
      c.Expr[Machine](q"""${c.prefix}($fullName)""")
    }
  }
}
case class File(value: String) extends SourceValue[String]
object File extends SourceCompanion[String, File](new File(_)){
  implicit def generate: sourcecode.File = macro impl

  def impl(c: Compat.Context): c.Expr[sourcecode.File] = {
    import c.universe._
    val file = c.enclosingPosition.source.path
    c.Expr[sourcecode.File](q"""${c.prefix}($file)""")
  }
}
case class Line(value: Int) extends SourceValue[Int]
object Line extends SourceCompanion[Int, Line](new Line(_)){
  implicit def generate: sourcecode.Line = macro impl
  def impl(c: Compat.Context): c.Expr[sourcecode.Line] = {
    import c.universe._
    val line = c.enclosingPosition.line
    c.Expr[sourcecode.Line](q"""${c.prefix}($line)""")
  }
}
case class Column(value: Int) extends SourceValue[Int]
object Column extends SourceCompanion[Int, Line](new Line(_)){
  implicit def generate: sourcecode.Column = macro impl
  def impl(c: Compat.Context): c.Expr[sourcecode.Column] = {
    import c.universe._
    val column = c.enclosingPosition.column
    c.Expr[sourcecode.Column](q"""${c.prefix}($column)""")
  }
}
case class Enclosing(value: String) extends SourceValue[String]

object Enclosing extends SourceCompanion[String, Enclosing](new Enclosing(_)){
  implicit def generate: Enclosing = macro impl
  def impl(c: Compat.Context): c.Expr[Enclosing] = Impls.enclosing[Enclosing](c)(
    !Util.isSynthetic(c)(_)
  )
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends SourceCompanion[String, Machine](new Machine(_)){
    implicit def generate: Machine = macro impl
    def impl(c: Compat.Context): c.Expr[Machine] = Impls.enclosing[Machine](c)(_ => true)
  }
}


case class Pkg(value: String) extends SourceValue[String]
object Pkg extends SourceCompanion[String, Pkg](new Pkg(_)){
  implicit def generate: Pkg = macro impl
  def impl(c: Compat.Context): c.Expr[Pkg] = Impls.enclosing[Pkg](c)(_.isPackage)
}

case class Text[T](value: T, source: String)
object Text{
  implicit def generate[T](v: T): Text[T] = macro Impls.text[T]
  def apply[T](v: T): Text[T] = macro Impls.text[T]

}

case class Args(value: Seq[Seq[Text[_]]]) extends SourceValue[Seq[Seq[Text[_]]]]
object Args extends SourceCompanion[Seq[Seq[Text[_]]], Args](new Args(_)) {
  implicit def generate: Args = macro impl
  def impl(c: Compat.Context): c.Expr[Args] = {
    import c.universe._
    val param = Compat.enclosingParamList(c)
    val texts = param.map(_.map(p => c.Expr[Text[_]](q"""sourcecode.Text($p, ${p.name.toString})""")))
    val textSeqs = texts.map(s => c.Expr(q"""Seq(..$s)"""))
    c.Expr[Args](q"""Seq(..$textSeqs)""")
  }
}

object Impls{
  def text[T: c.WeakTypeTag](c: Compat.Context)(v: c.Expr[T]): c.Expr[sourcecode.Text[T]] = {
    import c.universe._
    val fileContent = new String(v.tree.pos.source.content)
    val start = v.tree.collect {
      case treeVal => treeVal.pos match {
        case NoPosition ⇒ Int.MaxValue
        case p ⇒ p.startOrPoint
      }
    }.min
    val g = c.asInstanceOf[reflect.macros.runtime.Context].global
    val parser = g.newUnitParser(fileContent.drop(start))
    parser.expr()
    val end = parser.in.lastOffset
    val txt = fileContent.slice(start, start + end)
    val tree = q"""${c.prefix}(${v.tree}, $txt)"""
    c.Expr[sourcecode.Text[T]](tree)
  }
  sealed trait Chunk
  object Chunk{
    case class Pkg(name: String) extends Chunk
    case class Obj(name: String) extends Chunk
    case class Cls(name: String) extends Chunk
    case class Trt(name: String) extends Chunk
    case class Val(name: String) extends Chunk
    case class Var(name: String) extends Chunk
    case class Lzy(name: String) extends Chunk
    case class Def(name: String) extends Chunk

  }

  def enclosing[T](c: Compat.Context)(filter: c.Symbol => Boolean): c.Expr[T] = {

    import c.universe._
    var current = Compat.enclosingOwner(c)
    var path = List.empty[Chunk]
    while(current != NoSymbol && current.toString != "package <root>"){
      if (filter(current)) {

        val chunk = current match {
          case x if x.isPackage => Chunk.Pkg
          case x if x.isModuleClass => Chunk.Obj
          case x if x.isClass && x.asClass.isTrait => Chunk.Trt
          case x if x.isClass => Chunk.Cls
          case x if x.isMethod => Chunk.Def
          case x if x.isTerm && x.asTerm.isVar => Chunk.Var
          case x if x.isTerm && x.asTerm.isLazy => Chunk.Lzy
          case x if x.isTerm && x.asTerm.isVal => Chunk.Val
        }

        path = chunk(Util.getName(c)(current)) :: path
      }
      current = current.owner
    }
    val renderedPath = path.map{
      case Chunk.Pkg(s) => s + "."
      case Chunk.Obj(s) => s + "."
      case Chunk.Cls(s) => s + "#"
      case Chunk.Trt(s) => s + "#"
      case Chunk.Val(s) => s + " "
      case Chunk.Var(s) => s + " "
      case Chunk.Lzy(s) => s + " "
      case Chunk.Def(s) => s + " "
    }.mkString.dropRight(1)
    c.Expr[T](q"""${c.prefix}($renderedPath)""")
  }
}
