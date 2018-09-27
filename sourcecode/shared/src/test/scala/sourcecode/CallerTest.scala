package sourcecode

/**
  * @author 张志豪 (izhangzhihao) &lt;izhangzhihao@hotmail.com&gt;
  */
object CallerTest {
  def run(): Unit = {
    assert(Foo.call == CallerTest.getClass.getName)
  }
}

object Foo {
  def call(implicit caller: Caller[_]): String = {
    caller.value.getClass.getName
  }
}