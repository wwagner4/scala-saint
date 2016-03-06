package net.entelijan

import java.io.File
import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.stream.io.Framing

trait ImageStore {

  def recordableOut(id: String): Source[Recordable, _]
  def recordableIn(id: String): Sink[Seq[Recordable], Future[Long]]

}

class ImageStoreFilesys(_dir: File) extends ImageStore with ImageStoreBase {

  val dir = _dir

  println("ImageStoreFilesys dir: " + dir)

  import scala.concurrent.ExecutionContext.Implicits.global

  require(dir.exists())
  require(dir.isDirectory())

  def recordableOut(id: String): Source[Recordable, _] = {
    val file: File = getTxtFile(id).getOrElse(throw new IllegalStateException("no data found for id " + id))

    FileIO.fromFile(file)
      .via(Framing.delimiter(ByteString("\n"), 100000))
      .map { bs: ByteString => bs.utf8String }
      .map { line: String => upickle.default.read[Recordable](line) }
  }

  def recordableIn(id: String): Sink[Seq[Recordable], Future[Long]] = {
    val file = txtFile(id)

    Flow[Seq[Recordable]]
      .map { reqSeq: Seq[Recordable] => write(reqSeq) }
      .toMat(FileIO.toFile(file, append = true))(Keep.right)
  }

  private def write(recs: Seq[Recordable]): ByteString = {
    val lines: Seq[String] = recs.map { rec => upickle.default.write(rec) }
    val str = lines.mkString("", "\n", "\n")
    ByteString(str)
  }
}

