package doctus.math

import doctus.core.util.DoctusPoint

case class V2(x: Double, y: Double)

case class M2(l1: V2, l2: V2) {
  def det: Double = (l1.x * l2.y) - (l2.x * l1.y)
}

case class V3(x: Double, y: Double, z: Double) {
  def *(s: Double) = V3(x * s, y * s, z * s)
}

case class M3(l1: V3, l2: V3, l3: V3) {
  def *(v: V3): V3 = {
    val x1 = l1.x * v.x + l1.y * v.y + l1.z * v.z
    val y1 = l2.x * v.x + l2.y * v.y + l2.z * v.z
    val z1 = l3.x * v.x + l3.y * v.y + l3.z * v.z
    V3(x1, y1, z1)
  }

  def *(s: Double): M3 = {
    M3(l1 * s, l2 * s, l3 * s)
  }

  //noinspection ZeroIndexToHead,ZeroIndexToHead
  def inverse: M3 = {
    def invertSign(i: Int, j: Int): Int = {
      val x = (i + j) % 2
      x match {
        case 0 => 1
        case _ => -1
      }
    }
    val d = det
    val x = for (i <- 0 to 2; j <- 0 to 2) yield sub(i, j).det * invertSign(i, j)
    val cm = M3(V3(x(0), x(1), x(2)), V3(x(3), x(4), x(5)), V3(x(6), x(7), x(8)))
    cm * (1 / d)
  }

  def det: Double =
    (l1.x * l2.y * l3.z) + (l1.y * l2.z * l3.x) + (l1.z * l2.x * l3.y) -
      (l3.x * l2.y * l1.z) - (l3.y * l2.z * l1.x) - (l3.z * l2.x * l1.y)

  def sub(i: Int, j: Int): M2 = {
    (i, j) match {
      case (0, 0) => M2(V2(l2.y, l2.z), V2(l3.y, l3.z))
      case (0, 1) => M2(V2(l1.y, l1.z), V2(l3.y, l3.z))
      case (0, 2) => M2(V2(l1.y, l1.z), V2(l2.y, l2.z))

      case (1, 0) => M2(V2(l2.x, l2.z), V2(l3.x, l3.z))
      case (1, 1) => M2(V2(l1.x, l1.z), V2(l3.x, l3.z))
      case (1, 2) => M2(V2(l1.x, l1.z), V2(l2.x, l2.z))

      case (2, 0) => M2(V2(l2.x, l2.y), V2(l3.x, l3.y))
      case (2, 1) => M2(V2(l1.x, l1.y), V2(l3.x, l3.y))
      case (2, 2) => M2(V2(l1.x, l1.y), V2(l2.x, l2.y))

      case _ => throw new IllegalArgumentException("Illegal index pair %d %d" format(i, j))
    }
  }

  def transpose: M3 = {
    M3(V3(l1.x, l2.x, l3.x), V3(l1.y, l2.y, l3.y), V3(l1.z, l2.z, l3.z))
  }
}

object Affine {
  def matrix(xoff: Double, yoff: Double, scale: Double): M3 = {
    M3(V3(scale, 0, xoff * scale), V3(0, scale, yoff * scale), V3(0, 0, 1))
  }

  def transforme(p: DoctusPoint, transm: M3): DoctusPoint = {
    val v = V3(p.x, p.y, 1)
    val v1 = transm * v
    DoctusPoint(v1.x, v1.y)
  }

}

