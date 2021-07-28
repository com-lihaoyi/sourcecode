import mill._, scalalib._, scalajslib._, scalanativelib._, publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version_mill0.9:0.1.1`
import de.tobiasroeser.mill.vcs.version.VcsVersion

val dottyVersions = sys.props.get("dottyVersion").toList

val scalaVersions = "2.11.12" :: "2.12.13" :: "2.13.4" :: "3.0.0" :: dottyVersions
val scala2Versions = scalaVersions.filter(_.startsWith("2."))

val scalaJSVersions = for {
  scalaV <- scalaVersions
  scalaJSV <- Seq("0.6.33", "1.5.1")
  if scalaV.startsWith("2.") || scalaJSV.startsWith("1.")
} yield (scalaV, scalaJSV)

val scalaNativeVersions = for {
  scalaV <- scala2Versions
  scalaNativeV <- Seq("0.4.0")
} yield (scalaV, scalaNativeV)

trait SourcecodeModule extends PublishModule {
  def artifactName = "sourcecode"

  def publishVersion = VcsVersion.vcsState().format()

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "com.lihaoyi",
    url = "https://github.com/lihaoyi/sourcecode",
    licenses = Seq(License.MIT),
    scm = SCM(
      "git://github.com/lihaoyi/sourcecode.git",
      "scm:git://github.com/lihaoyi/sourcecode.git"
    ),
    developers = Seq(
      Developer("lihaoyi", "Li Haoyi", "https://github.com/lihaoyi")
    )
  )
}
trait SourcecodeMainModule extends CrossScalaModule {
  def millSourcePath = super.millSourcePath / offset

  def offset: os.RelPath = os.rel

  def compileIvyDeps =
    if (crossScalaVersion.startsWith("2")) Agg(
      ivy"org.scala-lang:scala-reflect:${crossScalaVersion}",
      ivy"org.scala-lang:scala-compiler:${crossScalaVersion}"
    )
    else Agg.empty[Dep]

  def sources = T.sources(
    super.sources()
      .flatMap(source =>
        Seq(
          PathRef(source.path / os.up / source.path.last),
          PathRef(source.path / os.up / os.up / source.path.last)
        )
      )
  )
}


trait SourcecodeTestModule extends ScalaModule {
  def crossScalaVersion: String

  def offset: os.RelPath = os.rel
  def millSourcePath = super.millSourcePath / os.up

  def sources = T.sources(
    super.sources()
      .++(CrossModuleBase.scalaVersionPaths(crossScalaVersion, s => millSourcePath / s"src-$s" ))
      .flatMap(source =>
        Seq(
          PathRef(source.path / os.up / "test" / source.path.last),
          PathRef(source.path / os.up / os.up / "test" / source.path.last)
        )
      )
      .distinct
  )
}

object sourcecode extends Module {
  val dottyVersion = sys.props.get("dottyVersion")
  object jvm extends Cross[JvmSourcecodeModule](scalaVersions: _*)
  class JvmSourcecodeModule(val crossScalaVersion: String)
    extends SourcecodeMainModule with ScalaModule with SourcecodeModule {

    object test extends SourcecodeTestModule{
      def scalaVersion = crossScalaVersion
      def moduleDeps = Seq(JvmSourcecodeModule.this)
      val crossScalaVersion = JvmSourcecodeModule.this.crossScalaVersion
    }
  }

  object js extends Cross[JsSourcecodeModule](scalaJSVersions: _*)
  class JsSourcecodeModule(val crossScalaVersion: String, crossJSVersion: String)
    extends SourcecodeMainModule with ScalaJSModule with SourcecodeModule {
    def offset = os.up

    def scalaJSVersion = crossJSVersion
    object test extends SourcecodeTestModule with ScalaJSModule{
      def scalaVersion = crossScalaVersion
      def scalaJSVersion = crossJSVersion
      def offset = os.up
      def moduleDeps = Seq(JsSourcecodeModule.this)
      val crossScalaVersion = JsSourcecodeModule.this.crossScalaVersion
    }
  }

  object native extends Cross[NativeSourcecodeModule](scalaNativeVersions: _*)
  class NativeSourcecodeModule(val crossScalaVersion: String, crossScalaNativeVersion: String)
    extends SourcecodeMainModule with ScalaNativeModule with SourcecodeModule {
    def offset = os.up

    def scalaNativeVersion = crossScalaNativeVersion

    object test extends SourcecodeTestModule with ScalaNativeModule{
      def scalaVersion = crossScalaVersion
      def scalaNativeVersion = crossScalaNativeVersion
      def offset = os.up
      def moduleDeps = Seq(NativeSourcecodeModule.this)
      val crossScalaVersion = NativeSourcecodeModule.this.crossScalaVersion
    }
  }
}
