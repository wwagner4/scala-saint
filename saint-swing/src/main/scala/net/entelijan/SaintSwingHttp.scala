package net.entelijan

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpEntity.apply
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import doctus.core.DoctusCanvas
import doctus.core.DoctusDraggable
import doctus.core.DoctusScheduler
import doctus.core.framework.DefaultDraggableController

object SaintSwingHttp extends App with SaintSwing {

  val hostName = "wallace.lan"
  //val hostName = "entelijan.net"

  val port = 8099

  val editMode = EM_Existing("1456297635929")
  //val editMode = EM_New

  println(s"host: $hostName:$port")
  println(s"editMode: $editMode")
  
  run

  def runController(
    editMode: Editmode, canvas: DoctusCanvas, sched: DoctusScheduler, draggable: DoctusDraggable,
    system: ActorSystem, mat: Materializer): Unit = {

    val clientFlow: Flow[HttpRequest, HttpResponse, _] =
      Http(system).outgoingConnection(host = hostName, port = port)
    val recRel: RecorderReloader = RecorderReloaderHttp(sched, clientFlow, mat)
    println("Client-Http: Connectong to '%s:%d'" format (hostName, port))

    // Common to all Platforms
    val framework = DoctusDraggableFrameworkSaint(editMode, canvas, recRel)
    DefaultDraggableController(framework, canvas, sched, draggable)
  }
}

case class RecorderReloaderHttp(
  sched: DoctusScheduler, clientFlow: Flow[HttpRequest, HttpResponse, _], mat: Materializer)
    extends RecorderReloaderBuffering {

  def reload(id: String, consumer: RecordableConsumer): Future[Unit] = {

    def mapIdToHttpRequest(id: String): HttpRequest = {
      val uriStr = "/txt2/%s" format id
      HttpRequest(method = GET, uri = Uri(uriStr))
    }

    def responseToChunkSource(resp: HttpResponse): Source[HttpEntity.ChunkStreamPart, _] = {
      resp match {
        case HttpResponse(StatusCodes.OK, _, httpEntity: HttpEntity.Chunked, _) => httpEntity.chunks
        case any => throw new IllegalStateException("Illegal Response. %s" format any)
      }
    }

    Source.single(id)
      .map(id => mapIdToHttpRequest(id))
      .via(clientFlow)
      .map(resp => responseToChunkSource(resp)).map { srcChunk =>
        srcChunk
          .map(chunkedStreamPart => chunkedStreamPart.data().decodeString("UTF-8"))
          .map(string => upickle.default.read[Seq[Recordable]](string))
          .runForeach(recList => recList.foreach { rec =>
            consumer.consume(rec)
          })(mat)
      }.runWith(Sink.ignore)(mat)
  }

  def recordTransport(transp: SaintTransport): Unit = {

    def toHttpRequest(transp: SaintTransport): HttpRequest = {
      val content = upickle.default.write(transp)
      val headers = List(RawHeader("Content-Encoding", "gzip"))
      HttpRequest(method = POST, uri = Uri("/saint"), entity = content, headers = headers)
    }

    val sink = Sink.foreach { resp: HttpResponse =>
      val status = resp.status
      if (status.isFailure()) {
        println("Sending data to the server resulted in a failure. %d - %s"
          format (status.intValue(), status.reason()))
      }
    }

    Source.single(transp)
      .map(transp => toHttpRequest(transp))
      .via(clientFlow)
      .runWith(sink)(mat)
  }

}


