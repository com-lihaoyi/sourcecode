package sourcecode

import sourcecode.PlatformUtil.posix

object Implicits {
  def implicitRun() = {
    val name = implicitly[sourcecode.Name]
    assertEquals(name.value, "name")

    val fullName = implicitly[sourcecode.FullName]
    assertEquals(fullName.value, "sourcecode.Implicits.fullName")

    val enclosing = implicitly[sourcecode.Enclosing]
    assertEquals(enclosing.value, "sourcecode.Implicits.implicitRun enclosing")

    val pkg = implicitly[sourcecode.Pkg]
    assertEquals(pkg.value, "sourcecode")

    val file = implicitly[sourcecode.File]
    assert(posix(file.value).endsWith("/sourcecode/Implicits.scala"))
    assert(file.posix.endsWith("/sourcecode/Implicits.scala"))

    val relFile = implicitly[sourcecode.RelFile]
    if (TestUtil.isScala3) {
      assertEquals(posix(relFile.value), "sourcecode/test/src/sourcecode/Implicits.scala")
      assertEquals(relFile.posix, "sourcecode/test/src/sourcecode/Implicits.scala")
    } else {
      assert(posix(relFile.value).endsWith("sourcecode/test/src/sourcecode/Implicits.scala"))
      assert(relFile.posix.endsWith("sourcecode/test/src/sourcecode/Implicits.scala"))
    }

    val fileName = implicitly[sourcecode.FileName]
    assertEquals(fileName.value, "Implicits.scala")

    val line = implicitly[sourcecode.Line]
    assertEquals(line.value, 35) // Line-sensitive magic number !

    lazy val myLazy = {
      /* Bar used to be a trait, but that ran into the upstream bug
       * https://github.com/scala-js/scala-js/issues/3918 in Scala.js 1.0.0-RC2
       * and Scala 2.12+. We use an abstract class as a workaround.
       */
      abstract class Bar {
        val name = implicitly[sourcecode.Name]
        assertEquals(name.value, "name")

        val fullName = implicitly[sourcecode.FullName]
        assert(
          fullName.value == "sourcecode.Implicits.Bar.fullName" ||
            fullName.value == "sourcecode.Implicits._$Bar.fullName" // Dotty
        )

        val file = implicitly[sourcecode.File]
        assert(posix(file.value).endsWith("/sourcecode/Implicits.scala"))
        assert(file.posix.endsWith("/sourcecode/Implicits.scala"))

        val fileName = implicitly[sourcecode.FileName]
        assertEquals(fileName.value, "Implicits.scala")

        val line = implicitly[sourcecode.Line]
        assertEquals(line.value, 60) // Line-sensitive magic number !

        val enclosing = implicitly[sourcecode.Enclosing]
        assert(
          enclosing.value == "sourcecode.Implicits.implicitRun myLazy$lzy Bar#enclosing" ||
            enclosing.value == "sourcecode.Implicits.implicitRun myLazy Bar#enclosing" || // encoding changed in Scala 2.12
            enclosing.value == "sourcecode.Implicits.implicitRun myLazy Bar.enclosing" // Dotty
        )
      }
      val b = new Bar {}
    }
    myLazy
  }
}
