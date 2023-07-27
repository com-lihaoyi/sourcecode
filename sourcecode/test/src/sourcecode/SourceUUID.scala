package sourcecode

object SourceUUID {
  def run() = {
    // Verify that UUIDs are different and are generated at compile time, not at run time.
    def generateUUIDs() = {
      val uuid1 = implicitly[sourcecode.SourceUUID] // Generate some UUID.
      val uuid2 = implicitly[sourcecode.SourceUUID] // This will be a different UUID.
      (uuid1, uuid2)
    }

    val (u1a, u2a) = generateUUIDs() // Generate a pair of UUIDs.
    val (u1b, u2b) = generateUUIDs() // This will be the same pair of UUID.

    assert(u1a != u2a && u1a.toString != u2a.toString) // Verify that the two UUIDs are different.
    assert(u1a == u1b && u1a.toString == u1b.toString) // The UUIDs are generated at compile time.
    assert(u2a == u2b && u2a.toString == u2b.toString) // So, calling `generateUUIDs()` several times will produce the same results.
  }
}
