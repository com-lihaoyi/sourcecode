import mill._, scalalib._, scalajslib._, scalanativelib._, publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import $ivy.`com.github.lolgab::mill-mima::0.0.23`
import com.github.lolgab.mill.mima._

val dottyCommunityBuildVersion = sys.props.get("dottyVersion").toList

val scalaVersions =
  "2.12.16" :: "2.13.8" :: "3.3.1" :: dottyCommunityBuildVersion

trait MimaCheck extends Mima {
  def mimaPreviousVersions = VcsVersion.vcsState().lastTag.toSeq
}

trait SourcecodeModule extends PublishModule with MimaCheck {
  def artifactName = "sourcecode"

  def publishVersion = VcsVersion.vcsState().format()

  def crossScalaVersion: String

  // Temporary until the next version of Mima gets released with
  // https://github.com/lightbend/mima/issues/693 included in the release.
  def mimaPreviousArtifacts =
    if(crossScalaVersion.startsWith("3.")) Agg.empty[Dep] else super.mimaPreviousArtifacts()

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "com.lihaoyi",
    url = "https://github.com/com-lihaoyi/sourcecode",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github(owner = "com-lihaoyi", repo = "sourcecode"),
    developers = Seq(
      Developer("lihaoyi", "Li Haoyi", "https://github.com/lihaoyi")
    )
  )
}
trait SourcecodeMainModule extends CrossScalaModule with PlatformScalaModule {
  def compileIvyDeps =
    if (crossScalaVersion.startsWith("2")) Agg(
      ivy"org.scala-lang:scala-reflect:${crossScalaVersion}",
      ivy"org.scala-lang:scala-compiler:${crossScalaVersion}"
    )
    else Agg.empty[Dep]
}

object sourcecode extends Module {
  object jvm extends Cross[JvmSourcecodeModule](scalaVersions)
  trait JvmSourcecodeModule extends SourcecodeMainModule with ScalaModule with SourcecodeModule {

    object test extends ScalaTests{
      def testFramework = ""
    }
  }

  object js extends Cross[JsSourcecodeModule](scalaVersions)
  trait JsSourcecodeModule extends SourcecodeMainModule with ScalaJSModule with SourcecodeModule {

    def scalaJSVersion = "1.12.0"
    object test extends ScalaJSTests{
      def testFramework = ""
    }
  }

  object native extends Cross[NativeSourcecodeModule](scalaVersions)
  trait NativeSourcecodeModule extends SourcecodeMainModule with ScalaNativeModule with SourcecodeModule {

    def scalaNativeVersion = "0.5.0"
    object test extends ScalaNativeTests{
      def mainClass = Some("sourcecode.Main")
      def testFramework = ""
    }
  }
}
