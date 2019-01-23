package sourcecode

object TextTests {
  def apply() = {
    assert(foo(1) == (1, "1"))
    val bar = Seq("lols")
    assert(foo(bar) == (Seq("lols"), "bar"))
    // FIXME Don't pass on dotty (second element not ok)
    if (TestUtil.isDotty) {
      assert(foo(Symbol("lol").toString * 2)._1 == "'lol'lol")
      assert(foo{println("Hello"); Symbol("lol").toString * 2}._1 == "'lol'lol")
    } else {
      assert(foo(Symbol("lol").toString * 2) == ("'lol'lol", "'lol.toString * 2"))
      assert(foo{println("Hello"); Symbol("lol").toString * 2} == ("'lol'lol", "'lol.toString * 2"))
    }
  }
  def foo[T](v: sourcecode.Text[T]) = (v.value, v.source)
}
