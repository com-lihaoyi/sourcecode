package sourcecode

object NoSynthetic {
  def run() = {
    class EnumValue(implicit name: sourcecode.Name){
      override def toString = name.value
    }
    object Foo extends EnumValue

    assert(Foo.toString == "Foo")

    object Bar{
      assert(sourcecode.Name() == "Bar")
      assert(sourcecode.FullName() == "sourcecode.NoSynthetic.Bar")
      assert(sourcecode.Enclosing() == "sourcecode.NoSynthetic.run Bar")
    }
    Bar
  }
}
