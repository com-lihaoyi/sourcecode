import sbtcrossproject.{crossProject, CrossType}

crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.0")

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
    version := "0.1.4-SNAPSHOT",
    scalaVersion := "2.11.11",
    name := "sourcecode"  ,
    organization := "com.lihaoyi",
    libraryDependencies ++= macroDependencies(scalaVersion.value),
    unmanagedSourceDirectories in Compile ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 12 =>
          Seq(baseDirectory.value / ".."/"shared"/"src"/ "main" / "scala-2.11")
        case _ =>
          Seq()
      }
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
lazy val native = sourcecode.native
