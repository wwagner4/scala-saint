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

trait ImageStore {

  def recordableOut(id: String): Source[Recordable, _]
  def recordableIn(id: String): Sink[Seq[Recordable], _]

}

case class ImageStoreFilesys(dir: File) extends ImageStore with ImageStoreBase {

  println("ImageStoreFilesys dir: " + dir)

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  require(dir.exists())
  require(dir.isDirectory())

  def recordableOut(id: String): Source[Recordable, _] = {
    val file: File = getTxtFile(id).getOrElse(throw new IllegalStateException("no data found for id " + id))

    val lines: Source[String, Future[_]] = FileLinesSource(file)

    lines.map { line: String => upickle.default.read[Recordable](line) }
  }

  def recordableIn(id: String): Sink[Seq[Recordable], _] = {
    val file = txtFile(id)
    
    val flow = Flow[Seq[Recordable]].map { write }
    val sink = FileIO.toFile(file, append = true)

    // TODO: Difference between 'to', 'toMat'
    flow.toMat(sink)(Keep.right)
  }

  private def write(recs: Seq[Recordable]): ByteString = {
    val lines: Seq[String] = recs.map { rec => upickle.default.write(rec) }
    val str = lines.mkString("", "\n", "\n")
    ByteString(str)
  }
}

