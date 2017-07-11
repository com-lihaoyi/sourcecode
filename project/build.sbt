
val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.18")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")
addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.8.0")
addSbtPlugin({
  if (scalaJSVersion.startsWith("0.6."))
    "org.scala-native" % "sbt-scalajs-crossproject" % "0.2.0"
  else
    "org.scala-native" % "sbt-crossproject" % "0.2.0"
})
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.3.1")
