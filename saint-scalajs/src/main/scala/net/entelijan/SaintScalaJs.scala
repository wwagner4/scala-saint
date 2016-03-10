package net.entelijan

import doctus.core.DoctusScheduler
import doctus.scalajs.{ DoctusCanvasScalajs, DoctusDraggableScalajs, DoctusSchedulerScalajs }
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw._
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport
import doctus.core.template.DoctusTemplateControllerImpl
import doctus.scalajs.DoctusPointableScalajs

@JSExport("SaintScalaJs")
object SaintScalaJs {

  import scalajs.concurrent.JSExecutionContext.Implicits.runNow

  @JSExport
  def main(editmode: String) {

    val canvasElem: HTMLCanvasElement = dom.document.getElementById("canvas").asInstanceOf[HTMLCanvasElement]

    val canvas = DoctusCanvasScalajs(canvasElem)
    val sched = DoctusSchedulerScalajs
    val draggable = DoctusDraggableScalajs(canvasElem)
    val pointable = DoctusPointableScalajs(canvasElem)

    val mode = upickle.default.read[Editmode](editmode)

    // Common to all platforms
    val fw = DoctusDraggableFrameworkSaint(mode, canvas, RecorderReloaderScalaJs(sched))
    DoctusTemplateControllerImpl(fw, sched, canvas, pointable, draggable)

  }

  case class RecorderReloaderScalaJs(sched: DoctusScheduler) extends RecorderReloaderBufferingImpl {

    def recordTransport(transp: SaintTransport): Unit = {
      val data = upickle.default.write(transp)
      Ajax.post(s"/saint", data)
    }

    def reload(id: String, consumer: RecordableConsumer): Future[Unit] = {
      Future {
        val reqStr = s"/txt1/$id"
        val fresp = Ajax.get(reqStr)
        fresp.foreach { req =>
          val jsonStr = req.responseText
          val recs: Seq[Recordable] = upickle.default.read[Seq[Recordable]](jsonStr)
          recs.foreach { rec =>
            consumer.consume(rec)
          }
        }
      }
    }
  }

}