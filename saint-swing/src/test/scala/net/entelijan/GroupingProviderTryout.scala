package net.entelijan

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Await

object GroupingProviderTryout extends App {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  implicit val sys = ActorSystem.create()
  implicit val mat = ActorMaterializer()

  val ints = 1 to 10

  val src: Source[Int, _] = Source(ints)

  val sink = Sink.fold(0){(a, b: Int) => a + b}

  val sum = Await.result(src.runWith(sink), 5.second)

  println(sum)

  sys.shutdown()

}
