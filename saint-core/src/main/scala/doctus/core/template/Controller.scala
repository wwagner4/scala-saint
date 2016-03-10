package doctus.core.template

import doctus.core.DoctusGraphics
import doctus.core.util.DoctusPoint
import doctus.core.DoctusCanvas
import doctus.core.DoctusScheduler
import doctus.core.DoctusPointable
import doctus.core.DoctusDraggable

trait DoctusTemplate {

  def canvas: DoctusCanvas

  def width: Int = canvas.width

  def height: Int = canvas.height

  def frameRate: Option[Int] = Some(60)

  def draw(g: DoctusGraphics): Unit

  def pointableStart(pos: DoctusPoint): Unit

  def pointableStop(pos: DoctusPoint): Unit

  def draggableStart(pos: DoctusPoint): Unit

  def draggableStop(pos: DoctusPoint): Unit

  def draggableDrag(pos: DoctusPoint): Unit

}

trait DoctusController[T <: DoctusTemplate] {

  def template: T

  def sched: DoctusScheduler

  def canvas: DoctusCanvas

  def pointable: DoctusPointable

  def draggable: DoctusDraggable

  if (template.frameRate.isDefined) {
    val fr = template.frameRate.get
    require(fr > 0)
    sched.start(canvas.repaint, fr)
  }

  // TODO There could be a graphic context on all these on... methods.
  canvas.onRepaint(template.draw)

  pointable.onStart(template.pointableStart)

  pointable.onStop(template.pointableStop)

  draggable.onStart(template.draggableStart)

  draggable.onStop(template.draggableStop)

  draggable.onDrag(template.draggableDrag)

}

case class DoctusControllerDefault[T <: DoctusTemplate](
  template: T,
  sched: DoctusScheduler,
  canvas: DoctusCanvas,
  pointable: DoctusPointable,
  draggable: DoctusDraggable) extends DoctusController[T]





