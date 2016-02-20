package net.entelijan

import akka.stream.{ FlowShape, Materializer }
import akka.stream.scaladsl.{ Source, _ }
import scala.concurrent._
import scala.concurrent.duration._

object ImageFit {

  case class Borders(xmin: Double, xmax: Double, ymin: Double, ymax: Double)

  def zipWithAffine(width: Int, height: Int): Flow[Recordable, (Recordable, SaintAffine), _] = {
    println("zipWithAffine")
    val borders = new ZipWithCumulated[Recordable, Borders] {
      def bufferSize: Int = 100000
      def cumulate: (Borders, Recordable) => Borders = (cuml, elem) => {
        elem match {
          case REC_Draw(fromx, fromy, tox, toy) => max(cuml, fromx, fromy, tox, toy)
          case _                                => cuml
        }
      }
      def start: Borders = startBorders
    }.create
    borders.map {
      case (r, b) => (r, calcAffineParams(b, width, height))
    }
  }

  def calcAffineToFitBlocking(recs: Source[Recordable, _], width: Int, height: Int)(implicit mat: Materializer): SaintAffine = {

    def bordersSink: Sink[Recordable, Future[Borders]] = {

      Sink.fold(startBorders)((cuml, elem) => {
        elem match {
          case REC_Draw(fromx, fromy, tox, toy) => max(cuml, fromx, fromy, tox, toy)
          case _                                => cuml
        }
      })
    }

    val fb = recs.runWith(bordersSink)
    val b = Await.result(fb, 10.seconds)
    calcAffineParams(b, width, height)
  }

  private def startBorders = Borders(Double.MaxValue, Double.MinValue, Double.MaxValue, Double.MinValue)

  private def max(cuml: Borders, fromx: Double, fromy: Double, tox: Double, toy: Double): Borders = {
    val maxx = math.max(fromx, tox)
    val minx = math.min(fromx, tox)
    val maxy = math.max(fromy, toy)
    val miny = math.min(fromy, toy)
    Borders(math.min(minx, cuml.xmin),
      math.max(maxx, cuml.xmax),
      math.min(miny, cuml.ymin),
      math.max(maxy, cuml.ymax))
  }

  private def calcAffineParams(imageBorders: Borders, screenWidth: Double, screenHeight: Double): SaintAffine = {
    val borders = if (imageBorders.xmin == Double.MaxValue || imageBorders.xmax == Double.MinValue
      || imageBorders.ymin == Double.MaxValue || imageBorders.ymax == Double.MinValue) Borders(0, 1200, 0, 900)
    else imageBorders
    require(borders.xmax >= borders.xmin, "xmax must be greater equal xmin. %s" format borders)
    require(borders.ymax >= borders.ymin, "ymax must be greater equal ymin. %s" format borders)
    val imgW = borders.xmax - borders.xmin
    val imgH = borders.ymax - borders.ymin
    val imgRatio = imgW / imgH
    val screenRatio = screenWidth / screenHeight
    if (imgRatio <= screenRatio) {
      val scale = screenHeight / imgH
      val w = imgW * scale
      val xoff = (screenWidth - w) / 2 - (borders.xmin * scale)
      val yoff = -(borders.ymin * scale)
      SaintAffine(xoff, yoff, scale)
    } else {
      val scale = screenWidth / imgW
      val h = imgH * scale
      val xoff = -(borders.xmin * scale)
      val yoff = (screenHeight - h) / 2 - (borders.ymin * scale)
      SaintAffine(xoff, yoff, scale)
    }
  }

}
