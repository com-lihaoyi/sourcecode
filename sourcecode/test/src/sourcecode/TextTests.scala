package sourcecode

object TextTests {
  def apply() = {
    assert(foo(1) == (1, "1"))
    val bar = Seq("lols")
    assert(foo(bar) == (Seq("lols"), "bar"))
    assert(foo('lol.toString * 2) == ("'lol'lol", "'lol.toString * 2"))
    assert(foo{println("Hello"); 'lol.toString * 2} == ("'lol'lol", "'lol.toString * 2"))


    //[E] [E-1] illegal start of simple expression
    //[E]       L1: :String) => s.length)("foo") == (3, "_:String"))
    //[E]           ^
    assert(foon((s:String) => s.length)("foo") == (3, "_:String"))

    //actual
    assert(foon((_:String).length)("foo") == (3, "_:String"))
    //expected
    // assert(foon((_:String).length)("foo") == (3, "(_:String).length"))
  }

  def foo[T](v: sourcecode.Text[T]) = (v.value, v.source)

  def foon[T](v: sourcecode.Text[T => Int])(t: T) = (v.value(t), v.source)
}
