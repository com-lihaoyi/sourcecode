package sourcecode

import language.experimental.macros
object Util{
  def isSynthetic(s: String) = {
    s == "<init>" || (s.startsWith("<local ") && s.endsWith(">"))
  }
}
abstract class SourceValue[T]{
  def value: T
}
abstract class SourceCompanion[T, V <: SourceValue[T]]{
  def apply()(implicit s: V): T = s.value
}

case class Name(value: String) extends SourceValue[String]
object Name extends SourceCompanion[String, Name]{
  implicit def generate: sourcecode.Name = macro impl
  implicit def wrap(s: String): Name = Name(s)
  def impl(c: Compat.Context): c.Expr[sourcecode.Name] = {
    import c.universe._
    var owner = Compat.enclosingOwner(c)
    def getName(s: Symbol): String = s.name.decoded.toString.trim
    while(Util.isSynthetic(getName(owner))) owner = owner.owner
    val simpleName = getName(owner)
    c.Expr[sourcecode.Name](q"""_root_.sourcecode.Name($simpleName)""")
  }

}
case class FullName(value: String) extends SourceValue[String]
object FullName extends SourceCompanion[String, FullName]{
  implicit def generate: sourcecode.FullName = macro impl
  implicit def wrap(s: String): FullName = FullName(s)

  def impl(c: Compat.Context): c.Expr[sourcecode.FullName] = {
    import c.universe._
    val owner = Compat.enclosingOwner(c)
    val fullName = owner.fullName.trim
    c.Expr[sourcecode.FullName](q"""_root_.sourcecode.FullName($fullName)""")
  }
}
case class File(value: String) extends SourceValue[String]
object File extends SourceCompanion[String, File]{
  implicit def generate: sourcecode.File = macro impl
  implicit def wrap(s: String): File = File(s)

  def impl(c: Compat.Context): c.Expr[sourcecode.File] = {
    import c.universe._
    val file = c.enclosingPosition.source.path
    c.Expr[sourcecode.File](q"""_root_.sourcecode.File($file)""")
  }
}
case class Line(value: Int) extends SourceValue[Int]
object Line extends SourceCompanion[Int, Line]{
  implicit def generate: sourcecode.Line = macro impl
  implicit def wrap(i: Int): Line = Line(i)
  def impl(c: Compat.Context): c.Expr[sourcecode.Line] = {
    import c.universe._
    val line = c.enclosingPosition.line
    c.Expr[sourcecode.Line](q"""_root_.sourcecode.Line($line)""")
  }
}
case class Enclosing(rawPath: Vector[Chunk]) extends SourceValue[String]{
  override def toString = value
  def value = rawPath.map{
    case Chunk.Pkg(s) => s + "."
    case Chunk.Obj(s) => s + "."
    case Chunk.Cls(s) => s + "#"
    case Chunk.Trt(s) => s + "#"
    case Chunk.Val(s) => s + " "
    case Chunk.Var(s) => s + " "
    case Chunk.Lzy(s) => s + " "
    case Chunk.Def(s) => s + " "
  }.mkString.dropRight(1)
}

object Enclosing extends SourceCompanion[String, Enclosing]{
  implicit def generate: sourcecode.Enclosing = macro impl
  implicit def wrap(s: Seq[Chunk]): Enclosing = Enclosing(s.toVector)
  def impl(c: Compat.Context): c.Expr[sourcecode.Enclosing] = {
    import c.universe._
    val owner = Compat.enclosingOwner(c)
    def getName(s: Symbol): String = s.name.decoded.toString.trim
    var current = owner
    var path = List.empty[Tree]
    while(current != NoSymbol && current.toString != "package <root>"){
      if (!Util.isSynthetic(getName(current))) {
        val pre = q"_root_.sourcecode.Chunk"
        val chunk = current match {
          case x if x.isPackage => "Pkg"
          case x if x.isModuleClass => "Obj"
          case x if x.isClass && x.asClass.isTrait => "Trt"
          case x if x.isClass => "Cls"
          case x if x.isMethod => "Def"
          case x if x.isTerm && x.asTerm.isVar => "Var"
          case x if x.isTerm && x.asTerm.isLazy => "Lzy"
          case x if x.isTerm && x.asTerm.isVal => "Val"
        }

        path = q"$pre.${newTermName(chunk)}(${getName(current)})" :: path
      }
      current = current.owner
    }
    c.Expr[sourcecode.Enclosing](q"""_root_.sourcecode.Enclosing(Vector(..$path))""")
  }

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
case class Text[T](value: T, source: String)
object Text{
  implicit def generate[T](v: T): Text[T] = macro Impls.treeImpl[T]
  def apply[T](v: T): Text[T] = macro Impls.treeImpl[T]

}
object Impls{
  def treeImpl[T: c.WeakTypeTag](c: Compat.Context)(v: c.Expr[T]): c.Expr[sourcecode.Text[T]] = {
    val generate = () // shadow the implicit so we don't accidentally use it in here
    import c.universe._

    // If the source positions don't have a full range, then fall back to `showCode`
    val txt: String =
      if (v.tree.pos.end <= v.tree.pos.start) {
        c.abort(
          c.enclosingPosition,
          "sourcecode.Text could not find the range position of your snippet " +
            s"${v.tree}; this probably means you need to enable the `-Yrangepos` " +
            "flag in your Scala compiler settings."
        )
      } else {
        v.tree.pos.source.content.slice(v.tree.pos.start, v.tree.pos.end).mkString
      }

    val tree = q"""_root_.sourcecode.Text(${v.tree}, $txt)"""

    c.Expr[sourcecode.Text[T]](tree)
  }
}