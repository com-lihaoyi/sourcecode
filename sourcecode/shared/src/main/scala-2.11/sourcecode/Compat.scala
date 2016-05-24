package sourcecode

object Compat{
  type Context = scala.reflect.macros.blackbox.Context
  def enclosingOwner(c: Context) = c.internal.enclosingOwner

  def enclosingParamList(c: Context): List[List[c.Symbol]] = {
    def nearestEnclosingMethod(owner: c.Symbol): c.Symbol =
      if (owner.isMethod) owner
      else if (owner.isClass) owner.asClass.primaryConstructor
      else nearestEnclosingMethod(owner.owner)

    nearestEnclosingMethod(enclosingOwner(c)).asMethod.paramLists
  }
}