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

  def onDraw(g: DoctusGraphics): Unit

  def onPointableStart(pos: DoctusPoint): Unit

  def onPointableStop(pos: DoctusPoint): Unit

  def onDraggableStart(pos: DoctusPoint): Unit

  def onDraggableStop(pos: DoctusPoint): Unit

  def onDraggableDrag(pos: DoctusPoint): Unit

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

  canvas.onRepaint(template.onDraw)

  // TODO There could be a graphic context on all these on... methods. ???
  pointable.onStart(template.onPointableStart)

  pointable.onStop(template.onPointableStop)

  draggable.onStart(template.onDraggableStart)

  draggable.onStop(template.onDraggableStop)

  draggable.onDrag(template.onDraggableDrag)

}

case class DoctusControllerDefault[T <: DoctusTemplate](
  template: T,
  sched: DoctusScheduler,
  canvas: DoctusCanvas,
  pointable: DoctusPointable,
  draggable: DoctusDraggable) extends DoctusController[T]





