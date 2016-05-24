package sourcecode

/**
  * TopLevelClass doc.
  */
class TopLevelClass {

  /**
    * eta expanded.
    */
  def foo(x: Any) = 2

  /**
    * No args.
    */
  def noArg = 2
}

/**
  * I haz type param.
  */
class HasTypeParam[T]

/**
  * TopLevelTrait doc.
  */
trait TopLevelTrait

/**
  * TopLevelObject doc.
  */
object TopLevelObject

object DocTests {

  /**
    * NestedClass doc.
    */
  class NestedClass

  /**
    * NestedTrait doc.
    */
  trait NestedTrait

  /**
    * NestedObject doc.
    */
  object NestedObject

  object Nested {

    /**
      * TripleNestedClass doc.
      */
    class TripleNestedClass
  }

  /**
    * This is a variable.
    */
  val variable = 23

  /**
    * This is apply().
    */
  def apply(): Unit = {
    assert(DocString[TopLevelClass].value == "TopLevelClass doc.")
    assert(DocString[TopLevelTrait].value == "TopLevelTrait doc.")
    assert(DocString[TopLevelObject.type].value == "TopLevelObject doc.")
    assert(DocString[NestedObject.type].value == "NestedObject doc.")
    assert(DocString[NestedTrait].value == "NestedTrait doc.")
    assert(DocString[NestedClass].value == "NestedClass doc.")
    assert(DocString[Nested.TripleNestedClass].value == "TripleNestedClass doc.")
    // You need to pass in dummy parameters.
    assert(DocString[HasTypeParam[Int]].value == "I haz type param.")

    // FIXME extract docstrings for types.
//    assert(Doc((x: TopLevelClass) => x.foo _).value == "Eta expanded.")
//    assert(Doc((x: TopLevelClass) => x.noArg).value == "No args.")
//    assert(Doc(variable).value == "Triples is a variable")

    // FIXME negative test.
//    Doc.generate[String] // fails compilation
  }
}
