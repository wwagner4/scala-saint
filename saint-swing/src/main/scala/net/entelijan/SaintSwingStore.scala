package net.entelijan

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import doctus.core.DoctusCanvas
import doctus.core.DoctusDraggable
import doctus.core.DoctusScheduler
import java.io.File
import akka.stream.scaladsl.Sink
import scala.util.Success
import scala.util.Failure
import doctus.core.DoctusPointable
import doctus.core.template.DoctusTemplateControllerImpl
import doctus.core.template.DoctusTemplateCanvas

object SaintSwingStore extends App with SaintSwing {

  val workdir = FileUtil.dir(List("saint", "data"))
  val editMode = EM_Existing("1456297635929")
  //val editMode = EM_New

  println("workdir: " + workdir)
  println("editMode: " + editMode)

  run

  def runController(
    editMode: Editmode, canvas: DoctusTemplateCanvas, sched: DoctusScheduler, sys: ActorSystem, mat: Materializer): Unit = {

    val store = new ImageStoreFilesys(workdir)
    val recRel: RecorderReloader = RecorderReloaderStore(sched, store, mat)

    // Common to all Platforms
    val framework = DoctusTemplateSaint(editMode, canvas, recRel)
    DoctusTemplateControllerImpl(framework, sched, canvas)
  }
}

case class RecorderReloaderStore(sched: DoctusScheduler, store: ImageStore, mat: Materializer) extends RecorderReloaderBufferingImpl {

  def reload(id: String, consumer: RecordableConsumer): Future[Unit] = {
    store.recordableOut(id)
      .runForeach(rec => consumer.consume(rec))(mat)
  }

  def recordTransport(transp: SaintTransport): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val storeSink: Sink[Seq[Recordable], Future[_]] = store.recordableIn(transp.id)

    val result = Source.single(transp)
      .map { t => t.recordables }
      .runWith(storeSink)(mat)

    result onComplete {
      case Success(_) => // Nothing to do
      case Failure(t) => println("Error during recording: " + t.getMessage)
    }
  }

}



