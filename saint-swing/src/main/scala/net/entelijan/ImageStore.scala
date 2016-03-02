package net.entelijan

import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.stream.scaladsl.Sink
import java.io.File
import akka.stream.scaladsl.Flow
import akka.stream.ActorAttributes
import scala.concurrent.Future
import akka.stream.scaladsl.Keep
import scala.concurrent.Await
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import doctus.swing.DoctusCanvasSwingBufferedImage
import doctus.swing.DoctusBufferedImage
import akka.stream.Materializer
import doctus.core.DoctusGraphics
import doctus.core.DoctusCanvas
import akka.stream.scaladsl.FileIO
import java.awt.image.RenderedImage

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

  def recordableOut(id: String): Source[Recordable, Future[_]] = {
    val file: File = getTxtFile(id).getOrElse(throw new IllegalStateException("no data found for id " + id))

    val lines: Source[String, Future[_]] = FileLinesSource(file)

    lines.map { line: String => upickle.default.read[Recordable](line) }
  }

  def recordableIn(id: String): Sink[Seq[Recordable], Future[_]] = {
    val file = txtFile(id)
    val flow = Flow[Seq[Recordable]].map { recs =>
      val lines = recs.map { upickle.default.write(_) }
      ByteString(lines.mkString("", "\n", "\n"))
    }
    val sink = FileIO.toFile(file, append = true)
    flow.toMat(sink)(Keep.right)
  }

}

