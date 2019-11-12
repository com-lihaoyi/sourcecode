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
    assert(file.endsWith("/sourcecode/Apply.scala"))

    val fileName = sourcecode.FileName()
    assert(fileName == "Apply.scala")

    val line = sourcecode.Line()
    assert(line == 23)

    lazy val myLazy = {
      trait Bar{
        val name = sourcecode.Name()
        assert(name == "name")

        val fullName = sourcecode.FullName()
        assert(
          fullName == "sourcecode.Apply.Bar.fullName" ||
          fullName == "sourcecode.Apply._$Bar.fullName"  // Dotty
        )

        val file = sourcecode.File()
        assert(file.endsWith("/sourcecode/Apply.scala"))

        val fileName = sourcecode.FileName()
        assert(fileName == "Apply.scala")

        val line = sourcecode.Line()
        assert(line == 43)

        val enclosing = sourcecode.Enclosing()
        println(s"enclosing: $enclosing")
        assert(
          enclosing == "sourcecode.Apply.applyRun myLazy$lzy Bar#enclosing" ||
          enclosing == "sourcecode.Apply.applyRun myLazy Bar#enclosing" || // encoding changed in Scala 2.12
          enclosing == "sourcecode.Apply.applyRun myLazy Bar.enclosing"    // Dotty
        )
      }
      val b = new Bar{}
    }
    myLazy // FIXME seems like this is not run on dotty
  }
}
