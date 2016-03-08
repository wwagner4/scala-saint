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

  val src = Source.fromIterator { () => (1 to 100).toList.iterator }

  val map = new SaintMonitor[Int]

  val r = src
    .via(map)
    .map { x => Thread.sleep(40); x }
    .runForeach { x => }

  Await.ready(r, 5.seconds)
  sys.shutdown()
}

