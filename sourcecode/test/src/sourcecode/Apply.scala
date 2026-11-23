package sourcecode

import sourcecode.PlatformUtil.posix

object Apply {
  def applyRun() = {
    val name = sourcecode.Name()
    assertEquals(name, "name")

    val fullName = sourcecode.FullName()
    assertEquals(fullName, "sourcecode.Apply.fullName")

    val enclosing = sourcecode.Enclosing()
    assertEquals(enclosing, "sourcecode.Apply.applyRun enclosing")

    val pkg = sourcecode.Pkg()
    assertEquals(pkg, "sourcecode")

    val file = sourcecode.File()
    assert(posix(file).endsWith("/sourcecode/Apply.scala"))

    val relFile = sourcecode.RelFile()
    if (TestUtil.isScala3) {
      val relFileExpected = "sourcecode/test/src/sourcecode/Apply.scala"
      assertEquals(posix(relFile), relFileExpected)
    } else {
      assert(posix(relFile).endsWith("/sourcecode/Apply.scala"))
    }

    val fileName = sourcecode.FileName()
    assertEquals(fileName, "Apply.scala")

    val line = sourcecode.Line()
    assertEquals(line, 33) // Line-sensitive magic number !

    lazy val myLazy = {
      /* Bar used to be a trait, but that ran into the upstream bug
       * https://github.com/scala-js/scala-js/issues/3918 in Scala.js 1.0.0-RC2
       * and Scala 2.12+. We use an abstract class as a workaround.
       */
      abstract class Bar {
        val name = sourcecode.Name()
        assertEquals(name, "name")

        val fullName = sourcecode.FullName()
        assertEquals(fullName, "sourcecode.Apply.Bar.fullName")

        val file = sourcecode.File()
        assert(posix(file).endsWith("/sourcecode/Apply.scala"))

        val relFile = sourcecode.RelFile()
        if (TestUtil.isScala3) {
          val relFileExpected = "sourcecode/test/src/sourcecode/Apply.scala"
          assertEquals(posix(relFile), relFileExpected)
        } else {
          assert(posix(relFile).endsWith("/sourcecode/Apply.scala"))
        }

        val fileName = sourcecode.FileName()
        assertEquals(fileName, "Apply.scala")

        val line = sourcecode.Line()
        assertEquals(line, 62) // Line-sensitive magic number !

        val enclosing = sourcecode.Enclosing()
        assert(
          enclosing == "sourcecode.Apply.applyRun myLazy$lzy Bar#enclosing" ||
            enclosing == "sourcecode.Apply.applyRun myLazy Bar#enclosing" // encoding changed in Scala 2.12
        )
      }
      val b = new Bar {}
    }
    myLazy
  }
}
