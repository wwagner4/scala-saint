package net.entelijan

import java.io.File
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink

object SaintSwingReplayFileToImage extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val is = new ImageSize {
    def width = 6000
    def height = 6000
  }
  val user = System.getProperty("user.home")
  val dir = new File("%s/saint/data/01" format user)
  val store = ImageRendererFilesys(dir)
  for (id <- store.ids) {
    val s = store.imageOut(id, is)
    s.runWith(Sink.ignore)
    println("Created file for: " + id)
  }
  system.shutdown()
}
