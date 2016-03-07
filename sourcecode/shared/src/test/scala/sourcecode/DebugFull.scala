package sourcecode

object DebugFull {
  def main() = {
    def debug[V](value: sourcecode.Text[V])(implicit enclosing: sourcecode.Enclosing) = {
      println(enclosing.value + " [" + value.source + "]: " + value.value)
    }

    class Foo(arg: Int){
      debug(arg) // sourcecode.DebugRun.main Foo [arg]: 123
      def bar(param: String) = {
        debug(param -> arg)
      }
    }
    new Foo(123).bar("lol")  // sourcecode.DebugRun.main Foo#bar [param]: lol
  }
}
