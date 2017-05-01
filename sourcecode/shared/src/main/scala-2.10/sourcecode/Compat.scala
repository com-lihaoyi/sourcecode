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

  def enclosingParamList(c: Context): List[List[c.Symbol]] = {
    def nearestClassOrMethod(owner: c.Symbol): c.Symbol =
      if (owner.isMethod || owner.isClass) owner else nearestClassOrMethod(owner.owner)

    val com = nearestClassOrMethod(enclosingOwner(c))
    if (com.isClass) {
      val pc = com.typeSignature.members.filter(m => m.isMethod && m.asMethod.isPrimaryConstructor)
      pc.head.asMethod.paramss
    } else {
      com.asMethod.paramss
    }
  }
  def posExtractor(c: Context)(pos: c.universe.Position): Int = pos match {
    case c.universe.NoPosition ⇒ Int.MaxValue
    case p ⇒ p.startOrPoint
  }
}