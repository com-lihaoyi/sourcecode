package sourcecode

object NameOfTests {
  def run() = {
    abstract class Foo[T](implicit name : Name.Of[T], fullName : FullName.Of[T]) {
      def getName = name.value
      def getFullName = fullName.value
    }
    trait TTT
    val foo = new Foo[TTT] {}
    assert(foo.getName == "TTT")
    assert(foo.getFullName == "sourcecode.NameOfTests.TTT")
  }
}
