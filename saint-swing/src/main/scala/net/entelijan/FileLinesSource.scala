package net.entelijan

import java.io.File
import akka.stream.scaladsl.Source
import akka.stream.io.Framing
import akka.util.ByteString
import scala.concurrent.Future
import akka.stream.scaladsl.FileIO

/**
 * A source for lines stored in a file
 */
object FileLinesSource {

  def apply(file: File, lineBufferSize: Int = 100000): Source[String, Future[_]] = {
    require(file.exists(), "File %s must exist" format file)
    
    FileIO.fromFile(file)
      .via(Framing.delimiter(ByteString("\n"), lineBufferSize))
      .map { bs: ByteString => bs.utf8String }
  }

}
