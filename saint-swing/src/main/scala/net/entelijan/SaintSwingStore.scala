package net.entelijan

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import doctus.core.DoctusCanvas
import doctus.core.DoctusDraggable
import doctus.core.DoctusScheduler
import doctus.core.framework.DefaultDraggableController
import java.io.File

object SaintSwingStore extends App with SaintSwing {

  val workdir = FileUtil.dir(List("saint", "data"))
  val editMode = EM_Existing("1456042177739")
  //val editMode = EM_New
  
  println("workdir: " + workdir)
  println("editMode: " + editMode)

  def runController(
    editMode: Editmode, canvas: DoctusCanvas, sched: DoctusScheduler, draggable: DoctusDraggable,
    system: ActorSystem)(implicit mat: Materializer): Unit = {
    val store = ImageStoreFilesys(workdir)
    val recRel: RecorderReloader = RecorderReloaderStore(sched, store)
    val framework = SaintDraggableFramework(editMode, canvas, recRel)
    DefaultDraggableController(framework, canvas, sched, draggable)
  }

  run
}

case class RecorderReloaderStore(sched: DoctusScheduler, store: ImageStore)(
    implicit mat: Materializer) extends RecorderReloader {

  def reload(id: String, consumer: RecordableConsumer): Future[Unit] = {
    store.recordableOut(id)
      .runForeach { rec => consumer.consume(rec) }
  }

  def record(transp: SaintTransport): Unit = {
    Source.single(transp)
      .map { t => t.recordables }
      .runWith(store.recordableIn(transp.id))
  }

}



