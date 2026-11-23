package sourcecode

private[sourcecode] object PlatformUtil {
  val isWindows = scala.scalajs.js.Dynamic.global.process.platform.asInstanceOf[String] == "win32"
  val separator = if(isWindows) "\\" else "/"
  def posix(path: String): String = path.replace(separator, "/")
}