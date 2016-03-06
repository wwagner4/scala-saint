package net.entelijan

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.stream.io.Framing
import akka.util.ByteString

object ReadFileTryout extends App {

  implicit val sys = ActorSystem()

  try {
    implicit val mat = ActorMaterializer()
    val path = "saint-swing/src/test/resources/saint_1443162064186.txt"
    val file = new java.io.File(path)
    val src: Source[String, _] = FileIO.fromFile(file)
      .via(Framing.delimiter(ByteString("\n"), 10000))
      .map { bs: ByteString => bs.utf8String }

    val result = src.runForeach(s => println("line0:'%s'" format s))
    Thread.sleep(10)
    val result1 = src.runForeach(s => println("line1:'%s'" format s))
    Thread.sleep(10)
    val result2 = src.runForeach(s => println("line2:'%s'" format s))
    Thread.sleep(10)
    val result3 = src.runForeach(s => println("line3:'%s'" format s))

    Await.result(result, 10.seconds)
    Await.result(result1, 10.seconds)
    Await.result(result2, 10.seconds)
    Await.result(result3, 10.seconds)
  } finally {
    sys.shutdown()
  }

}
