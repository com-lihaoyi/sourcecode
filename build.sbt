
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
  version := "0.1.1",
  scalaVersion := "2.11.7",
  name := "sourcecode"  ,
  organization := "com.lihaoyi",
  libraryDependencies ++= macroDependencies(scalaVersion.value),
  unmanagedSourceDirectories in Compile ++= {
    if (scalaVersion.value startsWith "2.10.") Seq(baseDirectory.value / ".."/"shared"/"src"/ "main" / "scala-2.10")
    else Seq(baseDirectory.value / ".."/"shared" / "src" / "main" / "scala-2.11")
  },
  publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),

  pomExtra :=
    <url>https://github.com/lihaoyi/sourcecode</url>
    <licenses>
      <license>
        <name>MIT license</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git://github.com/lihaoyi/sourcecode.git</url>
      <connection>scm:git://github.com/lihaoyi/sourcecode.git</connection>
    </scm>
    <developers>
      <developer>
        <id>lihaoyi</id>
        <name>Li Haoyi</name>
        <url>https://github.com/lihaoyi</url>
      </developer>
    </developers>
)

lazy val js = sourcecode.js
lazy val jvm = sourcecode.jvm