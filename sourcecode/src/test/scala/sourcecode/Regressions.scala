package sourcecode

object Regressions {
  def bug17() = {
    val text = sourcecode.Text(Seq(1).map(_+1))
    assert(text.source == "Seq(1).map(_+1)")
  }
  def main() = {
    bug17()
  }
}
