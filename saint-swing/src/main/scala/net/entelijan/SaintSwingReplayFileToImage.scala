package net.entelijan

import java.io.File
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.FileIO

object SaintSwingReplayFileToImage extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val is = new ImageSize {
    def width = 3000
    def height = 3000
  }
  val user = System.getProperty("user.home")
  val dir = new File("%s/tmp/saint-server" format user)
  val outdir = new File("%s/saint-images" format user)
  outdir.mkdirs
  val store = ImageStoreFilesys(dir)
  for (id <- store.ids) {
    val s = store.imageOut(id, is)
    val f = new File(outdir, "saint_%s_%d_%d.png" format (id, is.width, is.height))
    s.runWith(FileIO.toFile(f))
    println("writing to " + f)
  }
  system.shutdown()
}
