package sourcecode

object Tests{
  def applyRun() = {
    val nameA = sourcecode.Name()
    assert(nameA == "sourceContextA")

    val fullNameA = sourcecode.FullName()
    assert(fullNameA == "sourcecode.Tests.sourceContextA")

    val fileA = sourcecode.File()
    assert(fileA.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

    val lineA = sourcecode.Line()
    assert(lineA == 16)

    val enclosingA = sourcecode.Enclosing()
    assert(enclosingA == "sourcecode.Tests.run sourceContextA")

    lazy val myLazy = {
      trait Bar{
        val nameA = sourcecode.Name()
        assert(nameA == "sourceContextA")

        val fullNameA = sourcecode.FullName()
        assert(fullNameA == "sourcecode.Tests.sourceContextA")

        val fileA = sourcecode.File()
        assert(fileA.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

        val lineA = sourcecode.Line()
        assert(lineA == 16)

        val enclosingA = sourcecode.Enclosing()
        assert(enclosingA == "sourcecode.Tests.applyRun myLazy Bar#sourceContextA")
      }
      val b = new Bar{}
    }
    myLazy
  }

  def implicitRun() = {
    val nameA = implicitly[sourcecode.Name]
    assert(nameA.value == "sourceContextA")

    val fullNameA = implicitly[sourcecode.FullName]
    assert(fullNameA.value == "sourcecode.Tests.sourceContextA")

    val fileA = implicitly[sourcecode.File]
    assert(fileA.value.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

    val lineA = implicitly[sourcecode.Line]
    assert(lineA.value == 16)

    val enclosingA = implicitly[sourcecode.Enclosing]
    assert(enclosingA.value == "sourcecode.Tests.run sourceContextA")

    lazy val myLazy = {
      trait Bar{
        val nameA = implicitly[sourcecode.Name]
        assert(nameA.value == "sourceContextA")

        val fullNameA = implicitly[sourcecode.FullName]
        assert(fullNameA.value == "sourcecode.Tests.sourceContextA")

        val fileA = implicitly[sourcecode.File]
        assert(fileA.value.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

        val lineA = implicitly[sourcecode.Line]
        assert(lineA.value == 16)

        val enclosingA = implicitly[sourcecode.Enclosing]
        assert(enclosingA.value == "sourcecode.Tests.run myLazy Bar#sourceContextA")
      }
      val b = new Bar{}
    }
    myLazy
  }
  def run() = {
    applyRun()
    implicitRun()
  }
}