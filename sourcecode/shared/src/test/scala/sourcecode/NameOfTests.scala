package sourcecode

object NameOfTests {
  def run() = {
    abstract class Foo[T](implicit name : Name.OfSymbol[T], fullName : FullName.OfSymbol[T], typeName : Name.OfType[T]) {
      def getName = name.value
      def getFullName = fullName.value
      def getTypeName = typeName.value
    }
    trait TTT[T]
    trait MM
    val foo = new Foo[TTT[MM]] {}
    assert(foo.getName == "TTT")
    assert(foo.getFullName == "sourcecode.NameOfTests.TTT")
    assert(foo.getTypeName == "TTT[MM]")
  }
}
