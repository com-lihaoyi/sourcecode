import mill._, scalalib._, scalajslib._, scalanativelib._, publish._


trait SourcecodeModule extends PublishModule {
  def artifactName = "sourcecode"

  def publishVersion = "0.2.1"

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
  object jvm extends Cross[JvmSourcecodeModule]("2.11.12", "2.12.10", "2.13.1", "0.21.0-RC1")
  class JvmSourcecodeModule(val crossScalaVersion: String)
    extends SourcecodeMainModule with ScalaModule with SourcecodeModule {

    object test extends SourcecodeTestModule{
      def scalaVersion = crossScalaVersion
      def moduleDeps = Seq(JvmSourcecodeModule.this)
      val crossScalaVersion = JvmSourcecodeModule.this.crossScalaVersion
    }

    override def docJar =
      if (crossScalaVersion.startsWith("2")) super.docJar
      else T {
        val outDir = T.ctx().dest
        val javadocDir = outDir / 'javadoc
        os.makeDir.all(javadocDir)
        mill.api.Result.Success(mill.modules.Jvm.createJar(Agg(javadocDir))(outDir))
      }
  }

  object js extends Cross[JsSourcecodeModule](
    ("2.11.12", "0.6.28"), ("2.12.10", "0.6.28"), ("2.13.1", "0.6.28"),
    ("2.11.12", "1.0.0"), ("2.12.10", "1.0.0"), ("2.13.1", "1.0.0")
  )
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
/*
  object native extends Cross[NativeSourcecodeModule](("2.11.12", "0.3.8")/*, ("2.11.12", "0.4.0-M2")*/)
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
  }*/
}
