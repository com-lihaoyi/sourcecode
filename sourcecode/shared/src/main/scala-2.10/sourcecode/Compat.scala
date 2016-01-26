package sourcecode

object Compat{
  type Context = scala.reflect.macros.Context
  def enclosingOwner(c: Context) = {
    c.asInstanceOf[scala.reflect.macros.runtime.Context]
      .callsiteTyper
      .context
      .owner
      .asInstanceOf[c.Symbol]
  }
}