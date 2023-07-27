package sourcecode

object SourceUUID {
  def run() = {
    // Verify that UUIDs are different and are generated at compile time, not at run time.
    def generateUUIDs() = {
      val uuid1 = implicitly[sourcecode.SourceUUID]
      val uuid2 = implicitly[sourcecode.SourceUUID]
      (uuid1, uuid2)
    }

    val (u1a, u2a) = generateUUIDs()
    val (u1b, u2b) = generateUUIDs()

    assert(u1a != u2a) // The two UUIDs are different.
    assert(u1a == u1b) // The UUIDs are generated at compile time.
    assert(u2a == u2b) // So, calling `generateUUIDs()` several times will produce the same results.
  }
}
