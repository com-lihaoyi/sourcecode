val baseSettings = Seq(
  organization := "com.lihaoyi",
  name := "sourcecode",
  version := "0.1.4",
  scalaVersion := "2.11.11",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2", "2.13.0-M1"),
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
  )
)

baseSettings

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

lazy val sourcecode = crossProject
  .settings(baseSettings)
  .settings(
    libraryDependencies ++= macroDependencies(scalaVersion.value),
    unmanagedSourceDirectories in Compile ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 12 =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-2.11")
        case _ =>
          Seq()
      }
    },
    publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  )

lazy val js = sourcecode.js
lazy val jvm = sourcecode.jvm
