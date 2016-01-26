
crossScalaVersions := Seq("2.10.4", "2.11.7")

def macroDependencies(version: String) =
  Seq(
    "org.scala-lang" % "scala-reflect" % version % "provided",
    "org.scala-lang" % "scala-compiler" % version % "provided"
  ) ++
    (if (version startsWith "2.10.")
      Seq(compilerPlugin("org.scalamacros" % s"paradise" % "2.0.0" cross CrossVersion.full),
        "org.scalamacros" %% s"quasiquotes" % "2.0.0")
    else
      Seq())

lazy val sourcecode = crossProject.settings(
  version := "0.1.0",
  scalaVersion := "2.11.7",
  name := "sourcecode",
  organization := "com.lihaoyi",
  libraryDependencies ++= macroDependencies(scalaVersion.value),
  unmanagedSourceDirectories in Compile ++= {
    if (scalaVersion.value startsWith "2.10.") Seq(baseDirectory.value / ".."/"shared"/"src"/ "main" / "scala-2.10")
    else Seq(baseDirectory.value / ".."/"shared" / "src" / "main" / "scala-2.11")
  }
)

lazy val js = sourcecode.js
lazy val jvm = sourcecode.jvm