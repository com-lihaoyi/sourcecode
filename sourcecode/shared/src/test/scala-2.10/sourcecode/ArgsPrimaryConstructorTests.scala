package sourcecode

object ArgsPrimaryConstructorTests {
  def apply() = {

    var args: Seq[Seq[(String, Any)]] = Seq()

    def debug(implicit arguments: sourcecode.Args): Unit = args = arguments.value.map(_.map(t => t.source -> t.value))

    class Foo(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String) {
      debug
    }

    new Foo("text", 42, false)("foo", "bar")
    assert(args == Seq())
  }
}
