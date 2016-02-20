package doctus.math

import utest._
import doctus.core.util.DoctusPoint

object MathSuite extends TestSuite {

  def ~=(x: Double, y: Double) = {
    if ((x - y).abs < 0.00001) true else false
  }

  def tests = TestSuite {

    "V3" - {

      "Multiplication" - {
        * - {
          val v = V3(-2, 1, 0)
          val m = M3(V3(1, 2, 3), V3(4, 5, 6), V3(7, 8, 9))
          val vsoll = V3(0, -3, -6)
          val vist = m * v
          assert(vist == vsoll)
        }
        * - {
          val v = V3(3, 2, 3)
          val m = M3(V3(1, 2, 2), V3(5, 7, 3), V3(4, 9, 3))
          val vsoll = V3(13, 38, 39)
          val vist = m * v
          assert(vist == vsoll)
        }
      }

      import Affine._
      "Transform" - {
        * - {
          val m = matrix(0, 0, 1)
          val p = DoctusPoint(0, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(0, 0)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(0, 0, 10)
          val p = DoctusPoint(0, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(0, 0)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(0, 0, 10)
          val p = DoctusPoint(1, 1)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(10, 10)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(0, 0, 10)
          val p = DoctusPoint(1, 2)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(10, 20)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(0, 0, 10)
          val p = DoctusPoint(2, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(20, 0)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(0, 0, 10)
          val p = DoctusPoint(-2, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(-20, 0)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(0, 0, 0.5)
          val p = DoctusPoint(-2, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(-1, 0)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(1, 1, 1)
          val p = DoctusPoint(0, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(1, 1)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(1, 2, 1)
          val p = DoctusPoint(0, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(1, 2)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(1, 2, 1)
          val p = DoctusPoint(1, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(2, 2)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(1, -2, 1)
          val p = DoctusPoint(1, 0)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(2, -2)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(-5, -5, 0.5)
          val p = DoctusPoint(6, 5)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(0.5, 0)
          assert(pist == psoll)
        }
        * - {
          val m = matrix(-5, -5, 2)
          val p = DoctusPoint(6, 4)
          val pist = transforme(p, m)
          val psoll = DoctusPoint(2, -2)
          assert(pist == psoll)
        }
      }
      "Sub" - {
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(0, 0)
          val ssoll = M2(V2(1, 5), V2(6, 0))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(0, 1)
          val ssoll = M2(V2(2, 3), V2(6, 0))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(1, 1)
          val ssoll = M2(V2(1, 3), V2(5, 0))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(1, 0)
          val ssoll = M2(V2(0, 5), V2(5, 0))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(2, 1)
          val ssoll = M2(V2(1, 2), V2(5, 6))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(2, 2)
          val ssoll = M2(V2(1, 2), V2(0, 1))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(2, 1)
          val ssoll = M2(V2(1, 2), V2(5, 6))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(1, 2)
          val ssoll = M2(V2(1, 3), V2(0, 5))
          assert(sist == ssoll)
        }
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val sist = m.sub(0, 2)
          val ssoll = M2(V2(2, 3), V2(1, 5))
          assert(sist == ssoll)
        }
      }
      "Det3" - {
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val detist = m.det
          val detsoll = 5
          assert(detist == detsoll)
        }
      }
      "Det2" - {
        * - {
          val m = M2(V2(1, 2), V2(0, 1))
          val detist = m.det
          val detsoll = 1
          assert(detist == detsoll)
        }
        * - {
          val m = M2(V2(1, 2), V2(2, 5))
          val detist = m.det
          val detsoll = 1
          assert(detist == detsoll)
        }
      }
      "Inverse" - {
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val msoll = M3(V3(-6, 18.0 / 5, 7.0 / 5), V3(5, -3, -1), V3(-1, 4.0 / 5, 1.0 / 5))
          val mist = m.inverse
          assert(~=(mist.l1.x, msoll.l1.x))
          assert(~=(mist.l1.y, msoll.l1.y))
          assert(~=(mist.l1.z, msoll.l1.z))
          assert(~=(mist.l2.x, msoll.l2.x))
          assert(~=(mist.l2.y, msoll.l2.y))
          assert(~=(mist.l2.z, msoll.l2.z))
          assert(~=(mist.l3.x, msoll.l3.x))
          assert(~=(mist.l3.y, msoll.l3.y))
          assert(~=(mist.l3.z, msoll.l3.z))
        }

      }
      "Transpose" - {
        * - {
          val m = M3(V3(1, 2, 3), V3(0, 1, 5), V3(5, 6, 0))
          val msoll = M3(V3(1, 0, 5), V3(2, 1, 6), V3(3, 5, 0))
          val mist = m.transpose
          assert(mist == msoll)
        }

      }
    }
  }
}
  
