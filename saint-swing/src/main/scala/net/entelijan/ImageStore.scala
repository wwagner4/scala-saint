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

trait ImageSize {
  def width: Int
  def height: Int
}

case object IS_Small extends ImageSize {
  def width = 300
  def height = 200
}

case object IS_Medium extends ImageSize {
  def width = 600
  def height = 400
}

case object IS_Large extends ImageSize {
  def width = 1200
  def height = 800
}

case object IS_VeryLarge extends ImageSize {
  def width = 6000
  def height = 4000
}

trait ImageStore {

  def ids: Seq[String]
  def imageOut(id: String, size: ImageSize)(implicit mat: Materializer): Source[ByteString, _]
  def recordableOut(id: String): Source[Recordable, Future[_]]
  def recordableIn(id: String): Sink[Seq[Recordable], Future[_]]

}

case class ImageStoreFilesys(dir: File) extends ImageStore {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  require(dir.exists())
  require(dir.isDirectory())

  val imgTyp = "png"

  def ids: Seq[String] = {
    val TxtFile = "saint_(.*)\\.txt".r
    val ids = dir.list().map {
      case TxtFile(id) => Some(id)
      case _           => None
    }
    ids.flatten.toList
  }

  def imageOut(id: String, size: ImageSize)(implicit mat: Materializer): Source[ByteString, _] = {

    def leftFileOlder(left: File, right: File): Boolean = {
      require(left.exists())
      require(right.exists())
      left.lastModified() > right.lastModified()
    }

    def drawImageBlocking(bi: BufferedImage, imgFile: File): Future[File] = {
      val recs: Source[Recordable, _] = recordableOut(id)
      val affine: SaintAffine = ImageFit.calcAffineToFitBlocking(recs, size.width, size.height)
      val drawingSink = DrawingSinkFactory(bi).create
      recs
        .map { rec => (rec, affine) }
        .runWith(drawingSink)
        .map { _ =>
          ImageIO.write(bi, imgTyp, imgFile)
          imgFile
        }
    }

    def drawImageLessBlocking(bi: BufferedImage, imgFile: File): Future[File] = {
      val zipWithAffine = ImageFit.zipWithAffine(size.width, size.height)
      val drawingSink = DrawingSinkFactory(bi).create
      recordableOut(id)
        .via(zipWithAffine)
        .runWith(drawingSink)
        .map { _ =>
          ImageIO.write(bi, imgTyp, imgFile)
          imgFile
        }
    }

    def createImageFile(ifile: File): Unit = {
      val bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB)
      val future = drawImageBlocking(bi, ifile)
      Await.ready(future, 10.seconds)
    }

    def createOrGetImage: File = {

      val tfile = txtFile(id)
      val ifile = imgFile(id, size)

      if (!ifile.exists() || leftFileOlder(tfile, ifile)) synchronized {
        createImageFile(ifile)
      }
      ifile
    }

    val file = createOrGetImage
    FileIO.fromFile(file, 100000)
  }

  def recordableOut(id: String): Source[Recordable, Future[_]] = {
    val file = getTxtFile(id).getOrElse(throw new IllegalStateException("no data found for id " + id))
    val lines = FileLinesSource(file)
    lines.map { upickle.default.read[Recordable] }
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

  private def imgFile(id: String, is: ImageSize): File = {
    val sizeStr = "%d_%d" format (is.width, is.height)
    val ifileName = "saint_%s_%s.%s" format (id, sizeStr, imgTyp)
    new File(dir, ifileName)
  }

  private def txtFileName(id: String) = "saint_%s.txt" format id

  private def txtFile(id: String): File = {
    new File(dir, txtFileName(id))
  }

  private def getTxtFile(id: String): Option[File] = {
    val re = txtFile(id)
    if (!re.exists()) None else Some(re)
  }

  case class DrawingSinkFactory(bi: BufferedImage) {

    val canvas = createCanvas(bi)

    canvas.onRepaint { g => draw(g) }

    val engine: DrawEngine = DrawEngineLine

    var init = true
    var recordables = List.empty[Recordable]

    def create: Sink[(Recordable, SaintAffine), Future[Unit]] = {
      recordables = List.empty[Recordable]
      Sink.foreach {
        case (r, a) =>
          recordables ::= a.transformRecord(r)
          canvas.repaint()
      }
    }

    private def draw(g: DoctusGraphics): Unit = {
      if (init) {
        engine.drawInit(g, canvas.width, canvas.height)
        init = false
      }
      val r1 = recordables.reverse
      recordables = List.empty[Recordable]
      r1.foreach { rec =>
        DrawUtil.drawRecordable(rec, canvas, g, engine)
      }
    }

    private def createCanvas(bi: BufferedImage): DoctusCanvas = {
      val dbi = DoctusBufferedImage(bi)
      DoctusCanvasSwingBufferedImage(dbi)
    }

  }

}


