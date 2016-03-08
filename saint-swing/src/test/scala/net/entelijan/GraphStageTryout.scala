package net.entelijan

import akka.stream.scaladsl.Source
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent._
import scala.concurrent.duration._

object GraphStageTryout extends App {
  
  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()
  
  println("hello")
  
  val iter = (1 to 100).toList
  
  val src = Source.fromIterator { () => (1 to 100).toList.iterator }
  
  val r = src.runForeach { x => println(x) }
  
  Await.ready(r, 5.seconds)
}