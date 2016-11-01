package sourcecode

object Apply {
  def applyRun() = {
    val name = sourcecode.Name()
    assert(name == "name")

    val fullName = sourcecode.FullName()
    assert(fullName == "sourcecode.Apply.fullName")

    val enclosing = sourcecode.Enclosing()
    assert(enclosing == "sourcecode.Apply.applyRun enclosing")

    val pkg = sourcecode.Pkg()
    assert(pkg == "sourcecode")

    val file = sourcecode.File()
    assert(file.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Apply.scala"))

    val line = sourcecode.Line()
    assert(line == 20)

    lazy val myLazy = {
      trait Bar{
        val name = sourcecode.Name()
        assert(name == "name")

        val fullName = sourcecode.FullName()
        assert(fullName == "sourcecode.Apply.Bar.fullName")

        val file = sourcecode.File()
        assert(file.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Apply.scala"))

        val line = sourcecode.Line()
        assert(line == 34)

        val enclosing = sourcecode.Enclosing()
        assert(
          (enclosing == "sourcecode.Apply.applyRun myLazy$lzy Bar#enclosing") ||
          (enclosing == "sourcecode.Apply.applyRun myLazy Bar#enclosing") // encoding changed in Scala 2.12
        )
      }
      val b = new Bar{}
    }
    myLazy
  }
}
