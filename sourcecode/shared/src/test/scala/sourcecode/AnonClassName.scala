package sourcecode

object AnonClassName {
  def run() = {
    abstract class Foo(implicit name : sourcecode.Name, ownerName: OwnerName) {
      def getName = name.value
      def getOwnerName = ownerName.value
    }
    val foo = new Foo {}
    var foo2 = new Foo {}
    lazy val foo3 = new Foo {}

    //It would have been better if the name will get "$anon", but the owner is `run` def
    assert(new Foo {}.getOwnerName == "run")
    assert(foo.getOwnerName == "foo")
    assert(foo2.getOwnerName == "foo2")
    assert(foo3.getOwnerName == "foo3$lzy" || foo3.getOwnerName == "foo3")
  }
}
