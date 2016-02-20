package doctus.core.framework

import doctus.core.util.DoctusPoint

trait Rect {

  def origin: DoctusPoint

  def width: Double

  def height: Double

  def isEnclosing(p: DoctusPoint): Boolean = {
    def between(x: Double, a: Double, b: Double): Boolean = {
      if (a <= b) x >= a && x <= b
      else x <= a && x >= b

    }
    between(p.x, origin.x, origin.x + width) &&
      between(p.y, origin.y, origin.y + height)
  }

}

object Rect {

  case class RectImpl(origin: DoctusPoint, width: Double, height: Double) extends Rect

  def apply(origin: DoctusPoint, width: Double, height: Double) = RectImpl(origin, width, height)

}

