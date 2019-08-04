package sourcecode

object Synthetic {
  def run() = {
    class EnumValue(implicit name: sourcecode.Name.Machine){
      override def toString = name.value
    }
    object Foo extends EnumValue

    assert(Foo.toString == "<init>")

    object Bar{
      assert(
        (sourcecode.Name.Machine() == "<local Bar>") ||
        (sourcecode.Name.Machine() == "<local Bar$>")
      )
      assert(
        (sourcecode.FullName.Machine() == "sourcecode.Synthetic.Bar.<local Bar>") ||
        (sourcecode.FullName.Machine() == "sourcecode.Synthetic$._$Bar$.<local Bar$>")
      )
      assert(
        (sourcecode.Enclosing.Machine() == "sourcecode.Synthetic.run Bar.<local Bar>") ||
        (sourcecode.Enclosing.Machine() == "sourcecode.Synthetic.run Bar.<local Bar$>")
      )
    }
    Bar
  }
}
