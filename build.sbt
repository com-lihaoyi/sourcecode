import sbtcrossproject.{crossProject, CrossType}
import OsgiKeys._

val scala210 = "2.10.7"
val scala211 = "2.11.12"
val scala212 = "2.12.8"
val scala213 = "2.13.0-RC1"

inThisBuild(List(
  organization := "com.lihaoyi",
  name := "sourcecode",
  scalaVersion := scala211,
  crossScalaVersions := Seq(scala210, scala211, scala212, scala213),
  homepage := Some(url("https://github.com/lihaoyi/sourcecode")),
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  developers += Developer(
    email = "haoyi.sg@gmail.com",
    id = "lihaoyi",
    name = "Li Haoyi",
    url = url("https://github.com/lihaoyi")
  )
))

skip in publish := true
crossScalaVersions := List() // required for `++2.12.8 test` to ignore native project

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
    libraryDependencies ++= macroDependencies(scalaVersion.value),
    test in Test := (run in Test).toTask("").value,
    unmanagedSourceDirectories in Compile ++= {
      val crossVer = CrossVersion.partialVersion(scalaVersion.value)

      val scala211plus = crossVer match {
        case Some((2, n)) if n >= 11 =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-2.11+")
        case _ =>
          Seq()
      }

      val scala2 = crossVer match {
        case Some((2, _)) =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-2.x")
        case _ =>
          Seq()
      }

      scala211plus ++ scala2
    },
    // Java 9 settings
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
      "Automatic-Module-Name" -> "com.lihaoyi.sourcecode"
    ),
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
