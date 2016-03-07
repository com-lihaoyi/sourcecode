package sourcecode

object EnumExaple {
  def run() = {
    case class EnumValue(name: String){
      override def toString = name
    }
    class Enum{
      def value(implicit name: sourcecode.Name) = EnumValue(name.value)
    }
    object MyEnum extends Enum{
      val firstItem = value
      val secondItem = value
    }
    assert(MyEnum.firstItem.toString == "firstItem")
    assert(MyEnum.secondItem.toString == "secondItem")
  }
}
