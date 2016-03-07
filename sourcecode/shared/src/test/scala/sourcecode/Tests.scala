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
  def enumInheritRun() = {
    class EnumValue(implicit name: sourcecode.Name){
      override def toString = name.value
    }
    object Foo extends EnumValue
    println(Foo.toString)
    assert(Foo.toString == "Foo")

    object Bar{
      assert(sourcecode.Name() == "Bar")
      assert(
        sourcecode.FullName() == "sourcecode.Tests.Bar",
        sourcecode.FullName()
      )
      assert(sourcecode.Enclosing() == "sourcecode.Tests.enumInheritRun Bar")

    }
    Bar
  }

  def enumMachineRun() = {
    class EnumValue(implicit name: sourcecode.Name.Machine){
      override def toString = name.value
    }
    object Foo extends EnumValue

    assert(Foo.toString == "<init>")

    object Bar{
      assert(sourcecode.Name.Machine() == "<local Bar>", sourcecode.Name())
      assert(
        sourcecode.Enclosing.Machine() == "sourcecode.Tests.enumMachineRun Bar.<local Bar>",
        sourcecode.Enclosing.Machine()
      )
      assert(
        sourcecode.FullName.Machine() == "sourcecode.Tests.Bar.<local Bar>",
        sourcecode.FullName.Machine()
      )
    }
    Bar
  }

  def run() = {
    Apply.applyRun()
    Implicits.implicitRun()
    logRun()
    enumRun()
    enumInheritRun()
    enumMachineRun()
    DebugRun.main()
    ManualImplicit()
    TextTests()
  }
}
