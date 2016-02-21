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
  //val editMode = EM_Existing("1231312")
  val editMode = EM_New
  
  println("workdir: " + workdir)
  println("editMode: " + editMode)

  def runController(
    editMode: Editmode, canvas: DoctusCanvas, sched: DoctusScheduler, draggable: DoctusDraggable, sa: SaintAffine,
    system: ActorSystem)(implicit mat: Materializer): Unit = {
    val store = ImageStoreFilesys(workdir)
    val recRel: RecorderReloader = RecorderReloaderStore(sched, store)
    val framework = DraggableFramework(editMode, canvas, recRel, sa)
    DefaultDraggableController(framework, canvas, sched, draggable)
  }

  run
}

case class RecorderReloaderStore(sched: DoctusScheduler, store: ImageStore)(
    implicit mat: Materializer) extends RecorderReloaderScheduling {

  def reload(id: String, consumer: RecordableConsumer, saffine: SAffine): Future[Unit] = {
    store.recordableOut(id)
      .map { rec => saffine.transformReload(rec) }
      .runForeach { rec => consumer.consume(rec) }
  }

  def recordTransport(transp: SaintTransport): Unit = {
    Source.single(transp)
      .map { t => t.recordables }
      .runWith(store.recordableIn(transp.id))
  }

}



