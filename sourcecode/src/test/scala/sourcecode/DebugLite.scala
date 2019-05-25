package sourcecode

object DebugLite {
  def main() = {
    def debug[V](value: sourcecode.Text[V]) = {
      println("[" + value.source + "]: " + value.value)
    }

    class Foo(arg: Int){
      debug(arg) // [arg]: 123
      def bar(param: String) = {
        debug(param -> arg)
      }
    }
    new Foo(123).bar("lol")  // [param]: lol
  }
}
