package sourcecode

object EnumFull {
  def run() = {
    case class EnumValue(name: String){
      override def toString = name
    }
    class Enum{
      def value(implicit name: sourcecode.FullName) = EnumValue(name.value)
    }
    object MyEnum extends Enum{
      val firstItem = value
      val secondItem = value
    }
    assertEquals(MyEnum.firstItem.toString, "sourcecode.EnumFull.MyEnum.firstItem")
    assertEquals(MyEnum.secondItem.toString, "sourcecode.EnumFull.MyEnum.secondItem")
  }
}
