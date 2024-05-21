package sourcecode

object SpecialName {

  def macroValRun() = {
    def keyword(implicit name: sourcecode.Name): String = name.value

    val `macro` = keyword

    assert(`macro` == "macro")
  }

}
