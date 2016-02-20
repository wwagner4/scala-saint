package net.entelijan

import org.scalatest._
import java.io.File
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Keep
import scala.concurrent.Await
import scala.concurrent.duration.Duration._
import scala.concurrent.duration._
import akka.stream.Materializer

class TestImageStore extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  test("one in one out") {
    implicit val sys = ActorSystem.create()
    try {
      implicit val mat = ActorMaterializer()
      val id = "1";

      val dir = createGetEmptyTestdir(id)
      val store = ImageStoreFilesys(dir)
      val rec = REC_ColorWhite

      val src = Source.single(List(rec))
      val sink = store.recordableIn(id)
      Await.ready(src.runWith(sink), 5.second)

      var result = List.empty[Recordable]
      val f = store.recordableOut(id).runForeach { x => result :+= x }
      Await.ready(f, 5.second)

      assert(result.size === 1)
      assert(result(0) === rec)
    } finally {
      sys.shutdown()
    }
  }

  test("two in two out") {
    implicit val sys = ActorSystem.create()
    try {
      implicit val mat = ActorMaterializer()

      val id = "2";
      val dir = createGetEmptyTestdir(id)
      val store = ImageStoreFilesys(dir)
      val rec1 = REC_ColorWhite
      val rec2 = REC_ColorBlack

      val src = Source.single(List(rec1, rec2))
      val sink = store.recordableIn(id)
      Await.ready(src.runWith(sink), 5.second)

      var result = Seq.empty[Recordable]
      val f = store.recordableOut(id).runForeach { x => result :+= x }
      Await.ready(f, 5.second)

      assert(result.size === 2)
      assert(result(0) === rec1)
      assert(result(1) === rec2)
    } finally {
      sys.shutdown()
    }
  }

  test("two in two out in two steps") {
    implicit val sys = ActorSystem.create()
    try {
      implicit val mat = ActorMaterializer()

      val id = "3";
      val dir = createGetEmptyTestdir(id)
      val store = ImageStoreFilesys(dir)
      val rec1 = REC_ColorWhite
      val rec2 = REC_ColorBlack

      val src1 = Source.single(List(rec1))
      val sink1 = store.recordableIn(id)
      Await.ready(src1.runWith(sink1), 5.second)

      val src2 = Source.single(List(rec2))
      val sink2 = store.recordableIn(id)
      Await.ready(src2.runWith(sink2), 5.second)

      var result = List.empty[Recordable]
      val f = store.recordableOut(id).runForeach { x => result :+= x }
      Await.ready(f, 5.second)

      assert(result.size === 2)
      assert(result(0) === rec1)
      assert(result(1) === rec2)
    } finally {
      sys.shutdown()
    }
  }

  test("no ids") {
    implicit val sys = ActorSystem.create()
    try {
      implicit val mat = ActorMaterializer()

      val id = "no-ids";
      val dir = createGetEmptyTestdir(id)
      val store = ImageStoreFilesys(dir)
      assert(true === store.ids.isEmpty)
    } finally {
      sys.shutdown()
    }
  }

  test("some ids") {
    def addSomeRecs(store: ImageStore, id: String)(implicit mat: Materializer): Unit = {
      val recs = someRecs
      val src = Source.single(recs)
      val sink = store.recordableIn(id)
      Await.ready(src.runWith(sink), 5.second)
      ()
    }
    implicit val sys = ActorSystem.create()
    try {
      implicit val mat = ActorMaterializer()
      val dir = createGetEmptyTestdir("some-ids")
      val store = ImageStoreFilesys(dir)
      addSomeRecs(store, "a")
      addSomeRecs(store, "b")
      addSomeRecs(store, "c")
      addSomeRecs(store, "a")
      assert(List("a", "b", "c") === store.ids)
    } finally {
      sys.shutdown()
    }
  }

  private def someRecs: List[Recordable] =
    List(
      REC_Brightness(20),
      REC_Cleanup,
      REC_Color(2, 3, 4),
      REC_Draw(1, 2, 3, 4),
      REC_ColorWhite)

  private def createGetEmptyTestdir(id: String): File = {
    //val tmp = new File(System.getProperty("user.home"))
    val tmp = new File(System.getProperty("java.io.tmpdir"))
    val test = new File(tmp, "saint-test-" + id)
    test.mkdirs()
    for (f <- test.listFiles()) {
      if (!f.delete()) throw new IllegalStateException("Could not delete " + f)
      // else println("deleted " + f)
    }
    test
  }

}