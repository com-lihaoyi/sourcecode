package sourcecode

/**
  * Created by haoyi on 3/6/16.
  */
object ManualImplicit {
  def apply() = {
    assert(foo() == "apply")
    assert(foo()("cow") == "cow")
    assert(bar() == 10)
    assert(bar()(123) == 123)
    assert(bar()(123) == 123)
    assert(baz() == "sourcecode.ManualImplicit.apply")
    assert(baz() == "sourcecode.ManualImplicit.apply")
    def enc() =
      assert(qux() == "sourcecode.ManualImplicit.apply enc")

    enc()
    def enc2() =
      assert(
        qux()(Seq(Chunk.Pkg("sourcecode"), Chunk.Obj("ManualImplicit")))
        == "sourcecode.ManualImplicit"
      )

    enc2()
  }
  def foo()(implicit i: sourcecode.Name) = i.value
  def bar()(implicit i: sourcecode.Line) = i.value
  def baz()(implicit i: sourcecode.FullName) = i.value
  def qux()(implicit i: sourcecode.Enclosing) = i.value
}
