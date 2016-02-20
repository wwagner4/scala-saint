package net.entelijan

import java.io.File
import akka.stream.scaladsl.Source
import akka.stream.io.Framing
import akka.util.ByteString
import scala.concurrent.Future
import akka.stream.scaladsl.FileIO

object FileLinesSource {

  def apply(file: File, lineBufferSize: Int = 100000): Source[String, Future[_]] = {
    require(file.exists(), "File %s must exist" format file)
    FileIO.fromFile(file).via(Framing.delimiter(
      ByteString("\n"), maximumFrameLength = lineBufferSize, allowTruncation = true))
      .map(_.utf8String)
  }

}
