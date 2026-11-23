package object sourcecode {
  def assertEquals[T](value: T, expected: T): Unit = {
    assert(value == expected, s"expected $expected, got $value")
  }
  private val separator = if(scala.util.Properties.isWin) "\\" else "/"
  def posix(path: String): String = path.replace(separator, "/")
}
