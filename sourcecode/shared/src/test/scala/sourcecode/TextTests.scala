package sourcecode

object TextTests {
  def apply() = {
    assert(foo(1) == (1, "1"))
    val bar = Seq("lols")
    assert(foo(bar) == (Seq("lols"), "bar"))
    assert(
      foo(Symbol("lol").toString * 2) == ("'lol'lol", "Symbol(\"lol\").toString * 2"),
      s"Expected: ${("'lol'lol", "Symbol(\"lol\").toString * 2")}, got: ${foo(Symbol("lol").toString * 2)}"
    )
    assert(foo{println("Hello"); Symbol("lol").toString * 2} == ("'lol'lol", "Symbol(\"lol\").toString * 2"))
  }
  def foo[T](v: sourcecode.Text[T]) = (v.value, v.source)
}
