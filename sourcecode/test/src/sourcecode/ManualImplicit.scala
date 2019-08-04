package sourcecode


object ManualImplicit {
  def apply() = {
    assert(foo() == "apply")
    assert(foo()(Name("cow")) == "cow")
    assert(bar() == 8)
    assert(bar()(Line(123)) == 123)
    assert(bar()(Line(123)) == 123)
    assert(baz() == "sourcecode.ManualImplicit.apply", baz())
    assert(baz() == "sourcecode.ManualImplicit.apply")
    def enc() =
      assert(qux() == "sourcecode.ManualImplicit.apply enc")

    enc()
    def enc2() =
      assert(
        qux()(Enclosing("sourcecode.ManualImplicit"))
        == "sourcecode.ManualImplicit"
      )

    enc2()
  }
  def foo()(implicit i: sourcecode.Name) = i.value
  def bar()(implicit i: sourcecode.Line) = i.value
  def baz()(implicit i: sourcecode.FullName) = i.value
  def qux()(implicit i: sourcecode.Enclosing) = i.value
}
