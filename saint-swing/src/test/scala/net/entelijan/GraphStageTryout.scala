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
  
  val src = Source.fromIterator { () => (1 to 10).toList.iterator }
  
  def f(in: Int): Int = in * 2
  
  val map = new Map[Int, Int](f)
  
  val r = src
    .via(map)
    .map {x => Thread.sleep(10); x}
    .runForeach { x => }
  
  Await.ready(r, 5.seconds)
  sys.shutdown()
}

class Map[A, B](f: A => B) extends GraphStage[FlowShape[A, B]] {
 
  val in = Inlet[A]("Map.in")
  val out = Outlet[B]("Map.out")
 
  override val shape = FlowShape.of(in, out)
  
  var time = Option.empty[Long]
 
  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val now = System.currentTimeMillis()
          time.foreach { prev => println(now - prev) }
          time = Some(now)
          push(out, f(grab(in)))
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}