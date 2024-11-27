package sourcecode

object FileNameOffset {
  def main() = {
    //SOURCECODE_ORIGINAL_FILE_PATH=Hello/World.Test

    //SOURCECODE_ORIGINAL_CODE_START_MARKER
    assert(sourcecode.Line() == 1)
    assert(sourcecode.Line() == 2)
    assert(sourcecode.File() == "Hello/World.Test")
    assert(sourcecode.FileName() == "World.Test")
  }
}





