package sourcecode

import java.util.UUID

object SourceUUIDTests {
  case class DataWithId(x: Int, id: UUID)

  case class DataWithImplicitId(x: Int)(implicit val id: sourcecode.SourceUUID)

  def run() = {
    // Verify that UUIDs are different and are generated at compile time, not at run time.
    def generateUUIDs() = {
      val uuid1 = implicitly[sourcecode.SourceUUID].value
      val uuid2 = implicitly[sourcecode.SourceUUID].value
      (uuid1, uuid2)
    }

    val (u1a, u2a) = generateUUIDs() // Generate a pair of UUIDs.
    val (u1b, u2b) = generateUUIDs() // This will have the same UUIDs.
    assert(u1a != u2a && u1a.toString != u2a.toString, "the two UUIDs are different")
    assert(u1a == u1b && u1a.toString == u1b.toString, "the UUIDs are generated at compile time")
    assert(u2a == u2b && u2a.toString == u2b.toString, "calling `generateUUIDs()` several times will produce the same results")

    def generateData1(x: Int)(implicit uuid: sourcecode.SourceUUID) = DataWithId(x, uuid.value)

    def generateData2(x: Int) = DataWithId(x, implicitly[sourcecode.SourceUUID].value)

    val u10 = generateData1(10)
    val u20 = generateData2(20)
    val u30 = generateData1(30)
    val u40 = generateData2(40)

    assert(u10.id.toString != u30.id.toString, "generateData1() produces different IDs each time")
    assert(u20.id.toString == u40.id.toString, "generateData2() produces the same IDs each time")
    assert(u10.id.toString != u20.id.toString && u30.id.toString != u40.id.toString, "generateData1() and generateData2() produce different IDs")

    val u50 = DataWithImplicitId(50)
    val u60 = DataWithImplicitId(60)
    assert(u50.id.toString != u60.id.toString, "DataWithImplicitId() produces different IDs each time")

    def generateData3(x: Int) = DataWithImplicitId(x)

    val u70 = generateData3(70)
    val u80 = generateData3(80)
    assert(u70.id.toString == u80.id.toString, "generateData3() produces the same IDs each time")
  }
}
