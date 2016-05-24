package sourcecode

object ArgsTests {
  def apply() = {

    var args: Seq[Seq[(String, Any)]] = Seq()

    def debug(implicit arguments: sourcecode.Args): Unit = args = arguments.value.map(_.map(t => t.source -> t.value))

    def foo(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String): Unit = {
      debug
    }

    def bar(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String): Unit = {
      val bar = {
        debug
        "bar"
      }
    }

    def baz: Unit = {
      debug
    }

    def withImplicit(p1: String, p2: Long, p3: Boolean)(implicit foo: String): Unit = {
      debug
    }

    class Foo(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String) {
      debug

      def this(p1: String, p2: Long) = {
        this(p1, p2, false)("foo", "bar")
        debug
      }
    }

    new Foo("text", 42, false)("foo", "bar")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo", "bar" -> "bar")))

    new Foo("text", 42)
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42)))

    foo("text", 42, false)("foo", "bar")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo", "bar" -> "bar")))

    bar("text", 42, false)("foo", "bar")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo", "bar" -> "bar")))

    baz
    assert(args == Seq())

    withImplicit("text", 42, false)("foo")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo")))

    implicit val implicitFoo = "bar"
    withImplicit("text", 42, false)
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "bar")))
  }
}
