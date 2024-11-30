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
    EnumExample.run()
    EnumFull.run()
    NoSynthetic.run()
    Synthetic.run()
    SpecialName.macroValRun()
    ManualImplicit()
    TextTests()
    ArgsTests()
    FileNameOffset.main()

    println("================LogExample================")
    logExample()
    println("================Debug Full================")
    DebugFull.main()
    println("================Debug Name================")
    DebugName.main()
    println("================Debug Lite================")
    DebugLite.main()
    println("================Unique IDs================")
    SourceUUIDTests.run()
    println("================Regressions===============")
    Regressions.main()
    println("================Test Ended================")
  }
}
