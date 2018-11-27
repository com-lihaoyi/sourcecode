package sourcecode

object Regressions {
  def bug17() = {
    val text = sourcecode.Text(Seq(1).map(_+1))
    // FIXME From dotty, getting: { // inlined
    //   scala.package.Seq.apply[scala.Int]((1: scala.<repeated>[scala.Int])).map[scala.Int, collection.Seq[scala.Int]](((_$1: scala.Int) => _$1.+(1)))(collection.Seq.canBuildFrom[scala.Int])
    // }
    if (!TestUtil.isDotty)
      assert(text.source == "Seq(1).map(_+1)")
  }
  def main() = {
    bug17()
  }
}
