package sourcecode

object FileNameOffset {
  def main() = {
    //SOURCECODE_ORIGINAL_FILE_PATH=Hello/World.Test

    //SOURCECODE_ORIGINAL_CODE_START_MARKER
    assertEquals(sourcecode.Line(), 1)
    assertEquals(sourcecode.Line(), 2)
    assertEquals(sourcecode.File(), "Hello/World.Test")
    assertEquals(sourcecode.RelFile(), "Hello/World.Test")
    assertEquals(sourcecode.FileName(), "World.Test")
  }
}





