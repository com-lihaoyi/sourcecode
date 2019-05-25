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
    assert(MyEnum.firstItem.toString == "sourcecode.EnumFull.MyEnum.firstItem")
    assert(MyEnum.secondItem.toString == "sourcecode.EnumFull.MyEnum.secondItem")
  }
}
