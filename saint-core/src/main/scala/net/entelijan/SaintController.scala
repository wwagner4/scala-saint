package net.entelijan

import doctus.core.color.DoctusColorWhite
import doctus.core.framework.DoctusDraggableFramework
import doctus.core.util.DoctusPoint
import doctus.core.{ DoctusCanvas, DoctusColor, DoctusGraphics }
import doctus.math.Affine
import scala.concurrent.Future
import doctus.core.color.DoctusColorRgb
import doctus.core.color.DoctusColorBlack

case class SaintTransport(id: String, recordables: Seq[Recordable])

sealed trait Recordable

case class REC_Draw(fromx: Double, fromy: Double, tox: Double, toy: Double) extends Recordable

// Needed to distinguish if a draw-directive was already recorded or not
case class REC_DrawUnrecorded(fromx: Double, fromy: Double, tox: Double, toy: Double) extends Recordable

case class REC_Color(r: Int, g: Int, b: Int) extends Recordable

case class REC_Brightness(value: Int) extends Recordable

case class REC_StrokeWidth(value: Double) extends Recordable

case object REC_ColorBlack extends Recordable

case object REC_ColorWhite extends Recordable

case object REC_Cleanup extends Recordable

trait DisplayTextAware {
  def displayText: String
}

sealed trait ControllerMode extends DisplayTextAware

case object CM_Draw extends ControllerMode {
  val displayText = "//"
}

case object CM_Translate extends ControllerMode {
  val displayText = "<-|->"
}

case object CM_ZoomIn extends ControllerMode {
  val displayText = "(+)"
}

case object CM_ZoomOut extends ControllerMode {
  val displayText = "(-)"
}

trait RecorderReloader {
  def record(id: String, recordable: Recordable): Unit
  def reload(id: String, consumer: RecordableConsumer): Future[Unit]
}

trait RecordableConsumer {
  def consume(recordable: Recordable): Unit
}

sealed trait Editmode
case object EM_New extends Editmode
case class EM_Existing(id: String) extends Editmode

trait SAffine {

  def transformRecord(r: Recordable): Recordable

  def transformReload(r: Recordable): Recordable

}

case class SaintAffine(xoff: Double, yoff: Double, scale: Double) extends SAffine {
  private val _scale = 1.0 / scale
  private val min = Affine.matrix(-xoff, -yoff, _scale)
  private val mout = min.inverse

  private def in(p: DoctusPoint): DoctusPoint = Affine.transforme(p, min)

  private def out(p: DoctusPoint): DoctusPoint = Affine.transforme(p, mout)

  private def win(w: Double): Double = w * _scale

  private def wout(w: Double): Double = w / _scale

  def transformRecord(r: Recordable): Recordable = r match {
    case REC_Draw(fromx: Double, fromy: Double, tox: Double, toy: Double) =>
      val from = out(DoctusPoint(fromx, fromy))
      val to = out(DoctusPoint(tox, toy))
      REC_Draw(from.x, from.y, to.x, to.y)
    case REC_DrawUnrecorded(fromx, fromy, tox, toy) => throw new IllegalStateException("REC_DrawUnrecorded should never occurre here")
    case REC_Color(r: Int, g: Int, b: Int)          => REC_Color(r: Int, g: Int, b: Int)
    case REC_Brightness(value: Int)                 => REC_Brightness(value)
    case REC_StrokeWidth(value: Double)             => REC_StrokeWidth(wout(value))
    case REC_ColorBlack                             => REC_ColorBlack
    case REC_ColorWhite                             => REC_ColorWhite
    case REC_Cleanup                                => REC_Cleanup
  }

  def transformReload(r: Recordable): Recordable = r match {
    case REC_Draw(fromx: Double, fromy: Double, tox: Double, toy: Double) =>
      val from = in(DoctusPoint(fromx, fromy))
      val to = in(DoctusPoint(tox, toy))
      REC_Draw(from.x, from.y, to.x, to.y)
    case REC_DrawUnrecorded(fromx, fromy, tox, toy) => throw new IllegalStateException("REC_DrawUnrecorded should never occurre here")
    case REC_Color(r: Int, g: Int, b: Int)          => REC_Color(r: Int, g: Int, b: Int)
    case REC_Brightness(value: Int)                 => REC_Brightness(value)
    case REC_StrokeWidth(value: Double)             => REC_StrokeWidth(win(value))
    case REC_ColorBlack                             => REC_ColorBlack
    case REC_ColorWhite                             => REC_ColorWhite
    case REC_Cleanup                                => REC_Cleanup
  }
}

case class SaintDraggableFramework(editmode: Editmode, canvas: DoctusCanvas, recRel: RecorderReloader) extends DoctusDraggableFramework with RecordableConsumer {

  import scala.concurrent.ExecutionContext.Implicits.global
  
  override def frameRate = Some(20)

  var saffine: SaintAffine = SaintAffine(0, 0, 1)
  var recordablesBuffer = List.empty[Recordable]

  var prevPoint: Option[DoctusPoint] = None
  var startPoint: Option[DoctusPoint] = None

  var reactingToUserInput = true
  var init = true
  var mode: ControllerMode = CM_Draw

  val engine: DrawEngine = DrawEngineLine

  def consume(r: Recordable): Unit = {
    val recTransformed = r match {
      case _: REC_DrawUnrecorded => r
      case _ => saffine.transformReload(r)
    }
    recordablesBuffer ::= recTransformed
  }

  val id = editmode match {
    case EM_New =>
      val newid = System.currentTimeMillis().toString
      recRel.record(newid, recColor(engine.initColor))
      recRel.record(newid, REC_Brightness(engine.initBrightness))
      recRel.record(newid, saffine.transformRecord(REC_StrokeWidth(engine.initStrokeWidth.toDouble)))
      newid

    case EM_Existing(_id) =>
      recRel.reload(_id, this)
      _id
  }

  val strokeWidthConf = RadioButtonGenericConfig("stroke weight", 70, List(2, 5, 7, 10, 15, 20, 50, 75, 100, 200, 400, 800), 5)
  val strokeWidthComp = RadioButtonGeneric(DoctusPoint(5, 5), 40, 360, strokeWidthConf)
  strokeWidthComp.onActivStart(() => reactingToUserInput = false)
  strokeWidthComp.onActivStop((sw: Option[Int]) => {
    sw.foreach { value =>
      engine.strokeWidth = value
      recRel.record(id, saffine.transformRecord(REC_StrokeWidth(value.toDouble)))
    }
    reactingToUserInput = true
  })

  val colorComp = RadioButtonColorCol(DoctusPoint(50, 5), 440, 40)
  colorComp.onActivStart(() => reactingToUserInput = false)
  colorComp.onActivStop((optVal: Option[DoctusColor]) => {
    optVal.foreach { col =>
      engine.baseColor(col)
      recRel.record(id, recColor(col))
    }
    reactingToUserInput = true
  })

  val colorBWComp = RadioButtonColorBW(DoctusPoint(50, 95), 40, 100)
  colorBWComp.onActivStart(() => reactingToUserInput = false)
  colorBWComp.onActivStop((optVal: Option[DoctusColor]) => {
    optVal.foreach { col =>
      engine.baseColor(col)
      recRel.record(id, recColor(col))
    }
    reactingToUserInput = true
  })

  val reloadComp = ClickButton(DoctusPoint(50, 200), 40, 60, "reload", 40)
  reloadComp.onActivStart(() => reactingToUserInput = false)
  reloadComp.onClick { () =>
    consume(REC_Cleanup)
    recRel.reload(id, this) onComplete {
      case _ => reactingToUserInput = true
    }
  }

  val modeConf = RadioButtonGenericConfig("mode", 40, List(CM_Draw, CM_Translate, CM_ZoomIn, CM_ZoomOut), 0)
  val modeComp = RadioButtonGeneric(DoctusPoint(50, 265), 40, 120, modeConf)
  modeComp.onActivStart(() => reactingToUserInput = false)
  modeComp.onActivStop((optValue: Option[ControllerMode]) => {
    optValue.foreach { value =>
      mode = value
    }
    reactingToUserInput = true
  })

  val brightnessConf = RadioButtonGenericConfig("brightness", 60, List(20, 40, 60, 80, 100), 4)
  val brightnessComp = RadioButtonGeneric(DoctusPoint(50, 50), 250, 40, brightnessConf)
  brightnessComp.onActivStart(() => reactingToUserInput = false)
  brightnessComp.onActivStop((optValue: Option[Int]) => {
    optValue.foreach { value =>
      engine.brightness(value)
      recRel.record(id, REC_Brightness(value))
    }
    reactingToUserInput = true
  })

  val components: List[Component] = List(colorComp, strokeWidthComp, brightnessComp, colorBWComp, reloadComp, modeComp)

  def draggableDrag(pos: DoctusPoint): Unit = {
    components.foreach {
      _.draggableDrag(pos)
    }
    if (prevPoint.isEmpty) {
      prevPoint = Some(pos)
    } else {
      val from = prevPoint.get
      val to = pos
      if (reactingToUserInput) {
        mode match {
          case CM_Draw =>
            val rec = REC_DrawUnrecorded(from.x, from.y, to.x, to.y)
            consume(rec)
          case _ => // Nothing to do
        }
      }
      prevPoint = Some(pos)
    }
  }

  def draggableStart(pos: DoctusPoint): Unit = {
    components.foreach {
      _.draggableStart(pos)
    }
    startPoint = Some(pos)
  }

  def draggableStop(pos: DoctusPoint): Unit = {
    if (reactingToUserInput) {
      val from = startPoint.get
      val to = pos
      mode match {
        case CM_Translate => translate(from, to)
        case CM_ZoomIn    => zoomIn(from, to, canvas)
        case CM_ZoomOut   => zoomOut(from, to, canvas)
        case _            => // Nothing to do
      }
    }
    components.foreach {
      _.draggableStop(pos)
    }
    prevPoint = None
    startPoint = None
  }

  def draw(g: DoctusGraphics) {

    if (init) {
      g.noStroke()
      g.fill(DoctusColorWhite, 255)
      g.rect(0, 0, canvas.width, canvas.height)
      init = false
    }
    if (reactingToUserInput) {
      mode match {
        case CM_Draw      => drawGraphic(g)
        case CM_Translate => drawGraphic(g)
        case CM_ZoomIn    => drawGraphic(g)
        case CM_ZoomOut   => drawGraphic(g)
        case _            => // Nothing to do
      }
    }
    components.foreach { _.draw(g) }
  }

  private def translate(from: DoctusPoint, to: DoctusPoint) = {
    val off = from - to
    val xoff1 = saffine.xoff + off.x * saffine.scale
    val yoff1 = saffine.yoff + off.y * saffine.scale
    saffine = saffine.copy(xoff = xoff1, yoff = yoff1)

    consume(REC_Cleanup)
    recRel.reload(id, this)
  }

  private def zoomIn(from: DoctusPoint, to: DoctusPoint, canvas: DoctusCanvas) = {
    val f = 0.7
    val s1 = saffine.scale * f

    val w = canvas.width
    val b = (w - w * f) * s1 * 0.5
    val dx = saffine.xoff + b
    val dy = saffine.yoff + (canvas.height - canvas.height * f) * s1 * 0.5

    saffine = saffine.copy(xoff = dx, yoff = dy, scale = s1)

    engine.strokeWidth = (engine.strokeWidth * s1).round.toInt

    consume(REC_Cleanup)
    recRel.reload(id, this)
  }

  private def zoomOut(from: DoctusPoint, to: DoctusPoint, canvas: DoctusCanvas) = {
    val f = 1.3
    val s1 = saffine.scale * f

    val w = canvas.width
    val b = (w * f - w) * s1 * 0.5
    val dx = saffine.xoff - b
    val dy = saffine.yoff - (canvas.height * f - canvas.height) * s1 * 0.5

    engine.strokeWidth = (engine.strokeWidth * s1).round.toInt

    saffine = saffine.copy(xoff = dx, yoff = dy, scale = s1)
    consume(REC_Cleanup)
    recRel.reload(id, this)
  }

  private def drawGraphic(g: DoctusGraphics): Unit = {
    val recs = recordablesBuffer.reverse
    recordablesBuffer = List.empty[Recordable]
    recs.foreach {
      case REC_Cleanup =>
        engine.drawInit(g, canvas.width, canvas.height)

      case REC_DrawUnrecorded(fx, fy, tx, ty) =>
        engine.draw(g, DoctusPoint(fx, fy), DoctusPoint(tx, ty))
        val drawTransformed = saffine.transformRecord(REC_Draw(fx, fy, tx, ty))
        recRel.record(id, drawTransformed)

      case _rec =>
        DrawUtil.drawRecordable(_rec, canvas, g, engine)
    }
  }

  private def recColor(col: DoctusColor): Recordable = col.rgb match {
    case (0, 0, 0)       => REC_ColorBlack
    case (255, 255, 255) => REC_ColorWhite
    case (r, g, b)       => REC_Color(r, g, b)
  }

}

