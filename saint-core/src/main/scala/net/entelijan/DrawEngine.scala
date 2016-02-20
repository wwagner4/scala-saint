package net.entelijan

import doctus.core._
import doctus.core.color._
import doctus.core.util.DoctusPoint

trait DrawEngine {

  def initColor: DoctusColor

  def initBrightness: Int

  def initStrokeWidth: Int

  def brightness(value: Int): Unit

  def baseColor(value: DoctusColor): Unit

  def strokeWidth_=(value: Int): Unit

  def strokeWidth: Int

  def drawInit(g: DoctusGraphics, width: Int, height: Int): Unit

  def draw(g: DoctusGraphics, from: DoctusPoint, to: DoctusPoint): Unit

}

object DrawEngineLine extends DrawEngine {

  val initColor = DoctusColorBlack
  val initBrightness = 100
  val initStrokeWidth = 20

  private case class ColorHolder(baseColor: DoctusColor, brightness: Int) {
    lazy val color: DoctusColor = {
      if (baseColor == DoctusColorBlack) baseColor
      else if (baseColor == DoctusColorWhite) baseColor
      else {
        val (r0, g0, b0) = baseColor.rgb
        val (h, s, _) = DoctusColorUtil.rgb2hsv(r0, g0, b0)
        val (r1, g1, b1) = DoctusColorUtil.hsv2rgb(h, s, brightness)
        DoctusColorRgb(r1, g1, b1)
      }
    }
  }

  private var _strokeWidth: Int = initStrokeWidth
  private var _colorHolder = ColorHolder(initColor, initBrightness)

  def brightness(value: Int): Unit = {
    _colorHolder = _colorHolder.copy(brightness = value)
  }

  def baseColor(value: DoctusColor): Unit = {
    _colorHolder = _colorHolder.copy(baseColor = value)
  }

  def strokeWidth_=(value: Int): Unit = {
    _strokeWidth = value
  }

  def strokeWidth: Int = {
    _strokeWidth
  }

  def drawInit(g: DoctusGraphics, width: Int, height: Int) {
    g.noStroke()
    g.fill(DoctusColorWhite, 255)
    g.rect(0, 0, width, height)
  }

  def draw(g: DoctusGraphics, from: DoctusPoint, to: DoctusPoint) {
    g.stroke(_colorHolder.color, 50)
    g.strokeWeight(_strokeWidth)
    g.line(from.x, from.y, to.x, to.y)
  }

}

