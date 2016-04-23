package sourcecode

object ArgsTests {
  def apply() = {
    def debug(implicit args: sourcecode.Args): Unit = {
      assert(args.value.size == 2)
      assert(args.value(0).size == 3)
      assert(args.value(0)(0).source == "p1")
      assert(args.value(0)(0).value == "text")
      assert(args.value(0)(1).source == "p2")
      assert(args.value(0)(1).value == 42)
      assert(args.value(0)(2).source == "p3")
      assert(args.value(0)(2).value == false)
      assert(args.value(1)(0).source == "foo")
      assert(args.value(1)(0).value == "foo")
      assert(args.value(1)(1).source == "bar")
      assert(args.value(1)(1).value == "bar")
    }

    def foo(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String): Unit = {
      debug
    }

    def bar(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String): Unit = {
      val bar = {
        debug
        "bar"
      }
    }

    foo("text", 42, false)("foo", "bar")
    bar("text", 42, false)("foo", "bar")
  }
}
