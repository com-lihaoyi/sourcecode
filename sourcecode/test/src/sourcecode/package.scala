package object sourcecode {
  def assertEquals[T](value: T, expected: T): Unit = {
    assert(value == expected, s"expected $expected, got $value")
  }
  def posix(path: String): String = path.replace(java.io.File.separator, "/")
}
