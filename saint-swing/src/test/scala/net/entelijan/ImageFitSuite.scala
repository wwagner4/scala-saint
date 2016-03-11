package net.entelijan

import utest._

object ImageFitSuite extends TestSuite {

  def tests = TestSuite {

    "saint affine" - {
      * - {
        val sa = SaintAffine(0, 0, 1)
        val r = REC_Color(1, 2, 3)
        val r1 = sa.transformRecord(r)
        assert(r == r1)
      }
      * - {
        val sa = SaintAffine(0, 0, 1)
        val r = REC_Color(1, 2, 3)
        val r1 = sa.transformReload(r)
        assert(r == r1)
      }
      * - {
        val sa = SaintAffine(0, 0, 1)
        val r = REC_Color(1, 2, 3)
        val r1 = sa.transformReload(r)
        assert(r == r1)
      }
      * - {
        val sa = SaintAffine(0, 0, 1)
        val r = REC_StrokeWidth(1.0)
        val r1 = sa.transformReload(r)
        assert(r == r1)
      }
      * - {
        val sa = SaintAffine(0, 0, 2.0)
        val r = REC_StrokeWidth(1.0)
        val is = sa.transformRecord(r)
        val should = REC_StrokeWidth(2.0)
        assert(is == should)
      }
      * - {
        val sa = SaintAffine(0, 0, 2)
        val r = REC_StrokeWidth(1.0)
        val is = sa.transformReload(r)
        val should = REC_StrokeWidth(0.5)
        assert(is == should)
      }
      * - {
        val sa = SaintAffine(100, -330, 2)
        val r = REC_StrokeWidth(1.0)
        val is = sa.transformReload(r)
        val should = REC_StrokeWidth(0.5)
        assert(is == should)
      }
    }
    "Reactive streams" - {

      import akka.actor._
      import akka.stream._
      import akka.stream.scaladsl._

      import scala.concurrent._
      import scala.concurrent.duration._

      case class Collector[T](cont: List[T])
    }
  }

}
