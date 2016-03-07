package sourcecode

object Tests{

  def logRun() = {
    def log(foo: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
      println(s"${file.value}:${line.value} $foo")
    }
    log("Foooooo") // sourcecode/shared/src/test/scala/sourcecode/Tests.scala:86 Fooooo
  }
  def enumRun() = {

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



  def run() = {
    Apply.applyRun()
    Implicits.implicitRun()
    logRun()
    enumRun()
    NoSynthetic.run()
    Synthetic.run()
    DebugRun.main()
    ManualImplicit()
    TextTests()
  }
}
