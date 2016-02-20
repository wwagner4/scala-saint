package net.entelijan

import doctus.core.util.DoctusPoint
import doctus.core.DoctusCanvas
import doctus.core.DoctusGraphics
import doctus.core.color.DoctusColorRgb
import doctus.core.color.DoctusColorWhite
import doctus.core.color.DoctusColorBlack

object DrawUtil {
  def drawRecordable(recordable: Recordable, canvas: DoctusCanvas, graphics: DoctusGraphics, engine: DrawEngine): Unit = recordable match {
    case REC_Draw(fromx, fromy, tox, toy) =>
      engine.draw(graphics, DoctusPoint(fromx, fromy), DoctusPoint(tox, toy))

    case REC_Color(r, g, b) =>
      engine.baseColor(DoctusColorRgb(r, g, b))

    case REC_ColorWhite =>
      engine.baseColor(DoctusColorWhite)

    case REC_ColorBlack =>
      engine.baseColor(DoctusColorBlack)

    case REC_Brightness(b) =>
      engine.brightness(b)

    case REC_StrokeWidth(sw) =>
      engine.strokeWidth = sw.round.toInt

    case REC_Cleanup =>
      throw new IllegalStateException("Cleanup should not be called here")

    case REC_DrawUnrecorded(fromx, fromy, tox, toy) => throw new IllegalStateException("RecDrawUnrecorded should never occurre here")

  }
}