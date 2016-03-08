package net.entelijan

import akka.stream.Attributes
import akka.stream.FlowShape
import akka.stream.Inlet
import akka.stream.Outlet
import akka.stream.stage.GraphStage
import akka.stream.stage.GraphStageLogic
import akka.stream.stage.InHandler
import akka.stream.stage.OutHandler


class SaintMonitor[A] extends GraphStage[FlowShape[A, A]] {

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
          if (now - startTime > 1000) {
            val avr = sum.toDouble / cnt
            println("-- %15.2f ms %10d" format (avr, cnt))
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