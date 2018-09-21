import sbtcrossproject.{crossProject, CrossType}
import OsgiKeys._

val scala210 = "2.10.7"
val scala211 = "2.11.12"
val scala212 = "2.12.6"
val scala213 = "2.13.0-M5"
val baseSettings = Seq(
  organization := "com.lihaoyi",
  name := "sourcecode",
  version := "0.1.5-SNAPSHOT",
  scalaVersion := scala211,
  crossScalaVersions := Seq(scala210, scala211, scala212, scala213),
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/lihaoyi/sourcecode"),
    connection = "scm:git:git@github.com:lihaoyi/sourcecode.git"
  )),
  homepage := Some(url("https://github.com/lihaoyi/sourcecode")),
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  developers += Developer(
    email = "haoyi.sg@gmail.com",
    id = "lihaoyi",
    name = "Li Haoyi",
    url = url("https://github.com/lihaoyi")
  ),
  publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
)
lazy val noPublish = Seq(
  publishArtifact := false,
  publish := {},
  publishLocal := {}
)

baseSettings
noPublish

def macroDependencies(version: String) =
  Seq(
    "org.scala-lang" % "scala-reflect" % version % "provided",
    "org.scala-lang" % "scala-compiler" % version % "provided"
  ) ++
    (if (version startsWith "2.10.")
      Seq(compilerPlugin("org.scalamacros" % s"paradise" % "2.1.0" cross CrossVersion.full),
        "org.scalamacros" %% s"quasiquotes" % "2.1.0")
    else
      Seq())

lazy val sourcecode = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(
    baseSettings,
    libraryDependencies ++= macroDependencies(scalaVersion.value),
    test in Test := (run in Test).toTask("").value,
    unmanagedSourceDirectories in Compile ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 12 =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-2.11")
        case _ =>
          Seq()
      }
    },
    // Osgi settings
    osgiSettings,
    exportPackage := Seq("sourcecode.*"),
    privatePackage := Seq(),
    dynamicImportPackage := Seq("*")
  )
  .enablePlugins(SbtOsgi)
  .jsSettings(
    scalaJSUseMainModuleInitializer in Test := true // use JVM-style main.
  )
  .nativeSettings(
    crossScalaVersions := Seq(scala211)
  )

lazy val js = sourcecode.js
lazy val jvm = sourcecode.jvm
lazy val native = sourcecode.native
