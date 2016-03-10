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
import doctus.scalajs.DoctusTemplateCanvasScalajs

@JSExport("SaintScalaJs")
object SaintScalaJs {

  import scalajs.concurrent.JSExecutionContext.Implicits.runNow

  @JSExport
  def main(editmode: String) {

    val canvasElem: HTMLCanvasElement = dom.document.getElementById("canvas").asInstanceOf[HTMLCanvasElement]

    val canvas = DoctusTemplateCanvasScalajs(canvasElem)
    val sched = DoctusSchedulerScalajs

    val mode = upickle.default.read[Editmode](editmode)

    // Common to all platforms
    val fw = DoctusTemplateSaint(mode, canvas, RecorderReloaderScalaJs(sched))
    DoctusTemplateControllerImpl(fw, sched, canvas)

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