package net.entelijan

import java.io.File
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpRequest, MediaTypes, RequestEntity, StatusCodes }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.Materializer
import akka.stream.scaladsl.{ Source, Flow, Sink }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }
import akka.util.ByteString
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.HttpCharsets

case class Config(host: String, port: Int)

object SaintServer extends App {

  import akka.http.scaladsl.Http
  import akka.stream.ActorMaterializer
  import system.dispatcher

  implicit val system = ActorSystem.create()
  implicit val materializer = ActorMaterializer()

  val port = 8099

  val config = detectConfig

  Http().bindAndHandle(SaintRoute(system, materializer), config.host, config.port).onComplete {
    case Success(b) =>
      val host = b.localAddress.getHostName
      val port = b.localAddress.getPort
      val ip = b.localAddress.getAddress.getHostAddress
      println(s"Started Server http://$host:$port http://$ip:$port")
    case Failure(e) =>
      println("Binding of Server1 failed " + e.getMessage)
      system.shutdown()
  }

  def detectConfig: Config = {
    val name = java.net.InetAddress.getLocalHost.getCanonicalHostName
    val ip = java.net.InetAddress.getLocalHost.getHostAddress
    if (name.contains("vadmin")) Config("entelijan.net", port)
    else Config(name, port)
  }
}

object SaintRoute extends Directives {

  val dir = FileUtil.dir(List("saint", "data"))
  val store = ImageRendererFilesys(dir)

  def apply(sys: ActorSystem, mat: Materializer): Route = {
    get {
      pathSingleSlash {
        complete(html(OverviewPage.render(store)))
      } ~
        path("txt2" / Rest) { id =>

          val src: Source[ByteString, _] = store.recordableOut(id)
            .grouped(200)
            .map { upickle.default.write(_) }
            .map { ByteString(_) }

          val ct = MediaTypes.`text/plain` withCharset HttpCharsets.`UTF-8`

          encodeResponse {
            complete(HttpEntity.Chunked.fromData(ct, src))
          }
        } ~
        path("txt1" / Rest) { id =>
          val src: Source[ByteString, _] = store.recordableOut(id)
            .grouped(Int.MaxValue)
            .map { upickle.default.write(_) }
            .map { ByteString(_) }
          val ct = MediaTypes.`text/plain` withCharset HttpCharsets.`UTF-8`
          encodeResponse {
            complete(HttpEntity.Chunked.fromData(ct, src))
          }
        } ~
        path("editnew") {
          complete(html(EditorPage.render(EM_New)))
        } ~
        path("editexisting" / Rest) { id =>
          complete(html(EditorPage.render(EM_Existing(id))))
        } ~
        path("js" / Rest) { jsfile =>
          val resName = "js/" + jsfile
          getFromResource(resName)
        } ~
        path("images" / ".*".r / ".*".r) { (size, id) =>
          complete(imageEntity(size, id)(mat))
        } ~
        path("stop") {
          sys.shutdown()
          complete(StatusCodes.ServiceUnavailable, "Server stopped!")
        } ~
        extractUnmatchedPath { p =>
          complete(StatusCodes.NotFound, "Path not found '%s'" format p)
        }
    } ~
      post {
        path("saint") {
          entity(SaintTransportUnmarshaller()) { transp =>
            val result = Source.single(transp.recordables)
              .runWith(store.recordableIn(transp.id))(mat)

            onComplete(result) {
              case Success(byteCnt) => complete(byteCnt.toString())
              case Failure(ex)      => complete(StatusCodes.InternalServerError, ex.getMessage)
            }
          }
        } ~
          extractUnmatchedPath { p =>
            complete(StatusCodes.NotFound, "Path not found '%s'" format p)
          }
      } ~
      extractUnmatchedPath { p =>
        complete(StatusCodes.NotFound, "Path not found '%s'" format p)
      }

  }

  case class SaintTransportUnmarshaller() extends Unmarshaller[HttpRequest, SaintTransport] {

    def apply(request: HttpRequest)(implicit ec: ExecutionContext, mat: Materializer): Future[SaintTransport] = {
      request.entity match {
        case HttpEntity.Strict(ctype, byteString) =>
          val str = byteString.decodeString("UTF-8")
          val transp = upickle.default.read[SaintTransport](str)
          Future(transp)
        case HttpEntity.Default(ctype, length, srcOfByteStrings) =>
          srcOfByteStrings
            .runFold("") { (cuml, chunk) => cuml + chunk.decodeString("UTF-8") }
            .map { str => upickle.default.read[SaintTransport](str) }
        case _ => throw new IllegalStateException("Unsupported entity type")
      }
    }
  }

  private def imageEntity(sizeStr: String, id: String)(implicit mat: Materializer): ToResponseMarshallable = {
    val size = sizeStr match {
      case "S"  => IS_Small
      case "M"  => IS_Medium
      case "L"  => IS_Large
      case "XL" => IS_VeryLarge
      case _    => throw new IllegalStateException("Unknown size string '%s'" format sizeStr)
    }
    val imgSource = store.imageOut(id, size)
    val ct = ContentType(MediaTypes.`image/png`)
    HttpEntity.Chunked.fromData(ct, imgSource)
  }

  private def html(content: String): ToResponseMarshallable = {
    val ct = MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`
    HttpEntity(ct, content)
  }

}



