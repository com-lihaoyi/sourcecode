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
      /* Bar used to be a trait, but that ran into the upstream bug
       * https://github.com/scala-js/scala-js/issues/3918 in Scala.js 1.0.0-RC2
       * and Scala 2.12+. We use an abstract class as a workaround.
       */
      abstract class Bar{
        val name = sourcecode.Name()
        assert(name == "name")

        val fullName = sourcecode.FullName()
        assert(fullName == "sourcecode.Apply.Bar.fullName")

        val file = sourcecode.File()
        assert(file.endsWith("/sourcecode/Apply.scala"))

        val fileName = sourcecode.FileName()
        assert(fileName == "Apply.scala")

        val line = sourcecode.Line()
        assert(line == 44)

        val enclosing = sourcecode.Enclosing()
        assert(
          enclosing == "sourcecode.Apply.applyRun myLazy$lzy Bar#enclosing" ||
          enclosing == "sourcecode.Apply.applyRun myLazy Bar#enclosing" // encoding changed in Scala 2.12
        )
      }
      val b = new Bar{}
    }
    myLazy
  }
}
