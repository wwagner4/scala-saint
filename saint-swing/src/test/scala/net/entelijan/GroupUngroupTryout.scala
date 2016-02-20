package net.entelijan

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._


object GroupUngroupTryout extends App {

  val as = ActorSystem()
  implicit val sm: Materializer = ActorMaterializer()(as)
  val src = Source(0 to 99)

  src.map(x => List(x)).runForeach(x => println("a:%s" format x.mkString("[", " ", "]")))
  src.grouped(15).runForeach(x => println("b:%s" format x.mkString("[", " ", "]")))
  src.grouped(Int.MaxValue).runForeach(x => println("c:%s" format x.mkString("[", " ", "]")))

  Thread.sleep(1000)
  as.shutdown()
}
