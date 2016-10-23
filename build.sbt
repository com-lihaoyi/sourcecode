val baseSettings = Seq(
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0-RC2"),
  version := "0.2.0",
  name := "sourcecode"  ,
  organization := "com.lihaoyi",
  publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/lihaoyi/sourcecode"),
    connection = "scm:git:git@github.com:lihaoyi/sourcecode.git"
  )),
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  developers += Developer(
    email = "haoyi.sg@gmail.com",
    id = "lihaoyi",
    name = "Li Haoyi",
    url = url("https://github.com/lihaoyi")
  )
)

baseSettings

def macroDependencies(version: String, binaryVersion: String) = {
  val quasiquotes = 
    if(binaryVersion == "2.10")
      Seq(
        compilerPlugin("org.scalamacros" % s"paradise" % "2.1.0" cross CrossVersion.full),
        "org.scalamacros" %% s"quasiquotes" % "2.1.0"
      )
    else Seq()

  Seq(
    "org.scala-lang" % "scala-reflect" % version % "provided",
    "org.scala-lang" % "scala-compiler" % version % "provided"
  ) ++ quasiquotes
}

lazy val sourcecode = crossProject.settings(baseSettings).settings(
  libraryDependencies ++= macroDependencies(scalaVersion.value, scalaBinaryVersion.value),
  unmanagedSourceDirectories in Compile ++= {
    if (Set("2.11", "2.12.0-RC2").contains(scalaBinaryVersion.value)) 
      Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-2.11_2.12")
    else
      Seq()
  }
)

lazy val js = sourcecode.js
lazy val jvm = sourcecode.jvm
