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
    assertEquals(MyEnum.firstItem.toString, "firstItem")
    assertEquals(MyEnum.secondItem.toString, "secondItem")
  }
}
