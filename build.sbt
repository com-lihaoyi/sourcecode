import sbtcrossproject.{crossProject, CrossType}

lazy val testAllCommand = Command.command("testall"){state =>
  "project sourcecodeNative" :: "clean" :: "test:run" ::
    "project sourcecodeJVM" :: "clean" :: "+test:run" ::
      "project sourcecodeJS" :: "clean" :: "+test:run" ::
        state
}

commands += testAllCommand

lazy val crossVersions = Seq("2.10.6", "2.11.11", "2.12.2")

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

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-encoding",
  "UTF-8",
  "-feature",
  "-Xlog-reflective-calls",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-value-discard",
  "-Xlint",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Xfuture"
)

lazy val scalacOptionsExt = Seq(
  "-Ywarn-infer-any",
  "-Ywarn-unused"
  )

def scalacCompileOptions(version: String) = if(version.startsWith("2.10")) commonScalacOptions 
  else  scalacOptionsExt ++ commonScalacOptions

lazy val sourcecode = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(
    version := "0.1.4-SNAPSHOT",
    scalaVersion := "2.11.11",
    name := "sourcecode",
    organization := "com.lihaoyi",
    commands += testAllCommand,
    libraryDependencies ++= macroDependencies(scalaVersion.value),
    scalacOptions ++= scalacCompileOptions(scalaVersion.value),
    incOptions := incOptions.value.withLogRecompileOnMacro(false),
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

lazy val js = sourcecode.js.settings(
    crossScalaVersions := crossVersions
  )
lazy val jvm = sourcecode.jvm.settings(
  crossScalaVersions := crossVersions
  )
lazy val native = sourcecode.native
