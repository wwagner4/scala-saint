package net.entelijan

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Attributes
import akka.stream.FlowShape
import akka.stream.Inlet
import akka.stream.Outlet
import akka.stream.scaladsl.Source
import akka.stream.stage.GraphStage
import akka.stream.stage.GraphStageLogic
import akka.stream.stage.InHandler
import akka.stream.stage.OutHandler

object GraphStageTryout extends App {

  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()

  println("hello")

  val src = Source.fromIterator { () => (1 to 100).toList.iterator }

  val map = new Monitor[Int]

  val r = src
    .via(map)
    .map { x => Thread.sleep(40); x }
    .runForeach { x => }

  Await.ready(r, 5.seconds)
  sys.shutdown()
}

class Monitor[A] extends GraphStage[FlowShape[A, A]] {

  val in = Inlet[A]("Monitor.in")
  val out = Outlet[A]("Monitor.out")

  override val shape = FlowShape.of(in, out)

  var time = Option.empty[Long]
  var startTime = System.currentTimeMillis()
  var cnt = 0
  var sum = 0L

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val now = System.currentTimeMillis()
          time.foreach { prev =>
            val diff = now - prev
            sum += diff
            cnt += 1
          }
          if (now - startTime > 500) {
            val avr = sum.toDouble / cnt
            println("average throughput: %.3f" format avr)
            startTime = now
            sum = 0
            cnt = 0
          }
          time = Some(now)
          push(out, grab(in))
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}