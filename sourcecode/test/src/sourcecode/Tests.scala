package sourcecode

object Tests{

  def logExample() = {
    def log(foo: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
      println(s"${file.value}:${line.value} $foo")
    }
    log("Foooooo") // sourcecode/shared/src/test/scala/sourcecode/Tests.scala:86 Fooooo
  }


  def run() = {
    println("================Test Begin================")
    Apply.applyRun()
    Implicits.implicitRun()
    EnumExaple.run()
    EnumFull.run()
    NoSynthetic.run()
    Synthetic.run()
    ManualImplicit()
    TextTests()
    ArgsTests()

    println("================LogExample================")
    logExample()
    println("================Debug Full================")
    DebugFull.main()
    println("================Debug Name================")
    DebugName.main()
    println("================Debug Lite================")
    DebugLite.main()
    println("================Regressions===============")
    Regressions.main()
    println("================Test Ended================")
  }
}
