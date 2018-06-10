package sourcecode

object AnonClassName {
  def run() = {
    abstract class Foo(implicit n : sourcecode.Name) {
      def getName = n.value
    }
    val foo = new Foo {}
    var foo2 = new Foo {}
    lazy val foo3 = new Foo {}

    assert(new Foo {}.getName == "$anon")
    assert(foo.getName == "foo")
    assert(foo2.getName == "foo2")
    assert(foo3.getName == "foo3$lzy" || foo3.getName == "foo3")
  }
}
