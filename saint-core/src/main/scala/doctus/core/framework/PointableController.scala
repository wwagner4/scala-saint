package doctus.core.framework

import doctus.core.util.DoctusPoint
import doctus.core.DoctusCanvas
import doctus.core.DoctusGraphics
import doctus.core.DoctusPointable
import doctus.core.DoctusScheduler

trait BaseFramework {

  def draw(g: DoctusGraphics)

  def width: Int = canvas.width

  def height: Int = canvas.height

  def canvas: DoctusCanvas

  def frameRate: Option[Int] = Some(60)
}

trait DoctusPointableFramework extends BaseFramework {

  def pointableStart(pos: DoctusPoint): Unit

  def pointableStop(pos: DoctusPoint): Unit

}

trait BaseController[T <: BaseFramework] {

  def framework: T

  def canvas: DoctusCanvas

  def sched: DoctusScheduler

  canvas.onRepaint(framework.draw)

  if (framework.frameRate.isDefined) {
    val fr = framework.frameRate.get
    require(fr > 0)
    sched.start(canvas.repaint, fr)
  }

}


trait PointableController extends BaseController[DoctusPointableFramework] {

  def pointable: DoctusPointable

  pointable.onStart(framework.pointableStart)

  pointable.onStop(framework.pointableStop)

}

case class DefaultPointableController(
                                       framework: DoctusPointableFramework,
                                       canvas: DoctusCanvas,
                                       sched: DoctusScheduler,
                                       pointable: DoctusPointable) extends PointableController
