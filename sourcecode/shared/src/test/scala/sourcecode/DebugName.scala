package sourcecode

object DebugName {
  def main() = {
    def debug[V](value: sourcecode.Text[V])(implicit name: sourcecode.Name) = {
      println(name.value + " [" + value.source + "]: " + value.value)
    }

    class Foo(arg: Int){
      debug(arg) // Foo [arg]: 123
      def bar(param: String) = {
        debug(param -> arg)
      }
    }
    new Foo(123).bar("lol")  // bar [param]: lol
  }
}
