package sourcecode

object Compat{
  type Context = scala.reflect.macros.blackbox.Context
  def enclosingOwner(c: Context) = c.internal.enclosingOwner
}