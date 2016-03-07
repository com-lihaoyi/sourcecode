package sourcecode

object Tests{
  def applyRun() = {
    val name = sourcecode.Name()
    assert(name == "name")

    val fullName = sourcecode.FullName()
    assert(fullName == "sourcecode.Tests.fullName")

    val file = sourcecode.File()
    assert(file.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

    val line = sourcecode.Line()
    assert(line == 14)

    val enclosing = sourcecode.Enclosing()
    assert(enclosing == "sourcecode.Tests.applyRun enclosing")

    lazy val myLazy = {
      trait Bar{
        val name = sourcecode.Name()
        assert(name == "name")

        val fullName = sourcecode.FullName()
        assert(fullName == "sourcecode.Tests.Bar.fullName")

        val file = sourcecode.File()
        assert(file.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

        val line = sourcecode.Line()
        assert(line == 31)

        val enclosing = sourcecode.Enclosing()
        assert(enclosing == "sourcecode.Tests.applyRun myLazy$lzy Bar#enclosing")
      }
      val b = new Bar{}
    }
    myLazy
  }

  def implicitRun() = {
    val name = implicitly[sourcecode.Name]
    assert(name.value == "name")

    val fullName = implicitly[sourcecode.FullName]
    assert(fullName.value == "sourcecode.Tests.fullName")

    val file = implicitly[sourcecode.File]
    assert(file.value.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

    val line = implicitly[sourcecode.Line]
    assert(line.value == 52)

    val enclosing = implicitly[sourcecode.Enclosing]
    assert(enclosing.value == "sourcecode.Tests.implicitRun enclosing")

    lazy val myLazy = {
      trait Bar{
        val name = implicitly[sourcecode.Name]
        assert(name.value == "name")

        val fullName = implicitly[sourcecode.FullName]
        assert(fullName.value == "sourcecode.Tests.Bar.fullName")

        val file = implicitly[sourcecode.File]
        assert(file.value.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

        val line = implicitly[sourcecode.Line]
        assert(line.value == 69)

        val enclosing = implicitly[sourcecode.Enclosing]
        assert(enclosing.value == "sourcecode.Tests.implicitRun myLazy$lzy Bar#enclosing")
      }
      val b = new Bar{}
    }
    myLazy
  }

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
    }
    Bar
  }

  def run() = {
    applyRun()
    implicitRun()
    logRun()
    enumRun()
    enumInheritRun()
    enumMachineRun()
    DebugRun.main()
    ManualImplicit()
    TextTests()
  }
}
