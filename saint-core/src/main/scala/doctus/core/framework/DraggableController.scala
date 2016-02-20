package doctus.core.framework

import doctus.core.DoctusCanvas
import doctus.core.DoctusDraggable
import doctus.core.DoctusScheduler
import doctus.core.util.DoctusPoint

trait DoctusDraggableFramework extends BaseFramework {

  def draggableStart(pos: DoctusPoint): Unit

  def draggableStop(pos: DoctusPoint): Unit

  def draggableDrag(pos: DoctusPoint): Unit

}

trait DraggableController extends BaseController[DoctusDraggableFramework] {

  def draggable: DoctusDraggable

  // TODO There could be a graphic context on all these on... methods.
  draggable.onStart(framework.draggableStart)

  draggable.onStop(framework.draggableStop)

  draggable.onDrag(framework.draggableDrag)

}

case class DefaultDraggableController(
                                       framework: DoctusDraggableFramework,
                                       canvas: DoctusCanvas,
                                       sched: DoctusScheduler,
                                       draggable: DoctusDraggable) extends DraggableController
