package sourcecode

private[sourcecode] object PlatformUtil {
  def isWindows = scala.util.Properties.isWin
  val separator = java.io.File.separator
  def posix(path: String): String = path.replace(separator, "/")
}