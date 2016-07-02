package sourcecode

object Abstract {
  def apply() = {

    abstract class Foo(implicit val name: sourcecode.Name)

    val x = new Foo {}

    assert(x.name.value == "x")

  }
}
