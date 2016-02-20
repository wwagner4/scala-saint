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

  def runController(
    editMode: Editmode, canvas: DoctusCanvas, sched: DoctusScheduler, draggable: DoctusDraggable, sa: SaintAffine,
    system: ActorSystem)(implicit mat: Materializer): Unit = {
    val serverConfig = SaintSwingConfig.serverConfig
    val clientFlow = Http(system).outgoingConnection(host = serverConfig.hostName, port = serverConfig.port)
    println("Client-Http: Connectong to '%s:%d'" format (serverConfig.hostName, serverConfig.port))
    val recRel: RecorderReloader = RecorderReloaderHttp(sched, clientFlow)
    val framework = DraggableFramework(editMode, canvas, recRel, sa)
    DefaultDraggableController(framework, canvas, sched, draggable)
  }

  run

}

case class RecorderReloaderHttp(
  sched: DoctusScheduler, clientFlow: Flow[HttpRequest, HttpResponse, _])(
    implicit mat: Materializer)
    extends RecorderReloaderScheduling {

  def reload(id: String, consumer: RecordableConsumer, saffine: SAffine): Future[Unit] = {

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
            val recTransf = saffine.transformReload(rec)
            consumer.consume(recTransf)
          })
      }.runWith(Sink.ignore)
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
      .runWith(sink)
  }

}


