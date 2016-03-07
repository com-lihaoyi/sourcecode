package sourcecode

object Apply {
  def applyRun() = {
    val name = sourcecode.Name()
    assert(name == "name")

    val fullName = sourcecode.FullName()
    assert(fullName == "sourcecode.Apply.fullName")

    val enclosing = sourcecode.Enclosing()
    assert(enclosing == "sourcecode.Apply.applyRun enclosing")

    val file = sourcecode.File()
    assert(file.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Apply.scala"))

    val line = sourcecode.Line()
    assert(line == 17)

    lazy val myLazy = {
      trait Bar{
        val name = sourcecode.Name()
        assert(name == "name")

        val fullName = sourcecode.FullName()
        assert(fullName == "sourcecode.Apply.Bar.fullName")

        val file = sourcecode.File()
        assert(file.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Apply.scala"))

        val line = sourcecode.Line()
        assert(line == 31)

        val enclosing = sourcecode.Enclosing()
        assert(enclosing == "sourcecode.Apply.applyRun myLazy$lzy Bar#enclosing")
      }
      val b = new Bar{}
    }
    myLazy
  }

}
