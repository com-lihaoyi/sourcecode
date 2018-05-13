package sourcecode

object AnonClassName {
  def run() = {
    abstract class Foo(implicit n : sourcecode.Name) {
      def getName = n.value
    }
    new Foo {} //just creating a nameless instance

    val foo = new Foo {}
    var foo2 = new Foo {}
    lazy val foo3 = new Foo {}

    assert(foo.getName == "foo")
    assert(foo2.getName == "foo2")
    assert(foo3.getName == "foo3$lzy")
  }
}
