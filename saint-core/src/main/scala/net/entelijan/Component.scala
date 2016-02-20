package net.entelijan

import doctus.core.color.{ DoctusColorBlack, DoctusColorRgb, DoctusColorUtil, DoctusColorWhite }
import doctus.core.framework.Rect
import doctus.core.util.{ DoctusPoint, DoctusVector }
import doctus.core.{ DoctusColor, DoctusGraphics }

case object Layout {
  val textSize = 10
  val textBoxStrokeWeight = 1
  val textAlpha = 100
  val boxAlpha = 220
  val textIndent = (textSize.toDouble / 3).toInt
  val colorUnselected = DoctusColorWhite
  val colorPreselected = DoctusColorRgb(150, 150, 150)
  val colorSelected = DoctusColorRgb(200, 200, 200)
}

trait Component extends Rect {

  def draw(g: DoctusGraphics): Unit

  def draggableStart(p: DoctusPoint): Unit

  def draggableStop(p: DoctusPoint): Unit

  def draggableDrag(p: DoctusPoint): Unit

}

case class ClickButton(origin: DoctusPoint, width: Double, height: Double, txt: String, txtlen: Int) extends Component {

  private var onActiveStartOpt: Option[() => Unit] = None
  private var onActiveStopOpt: Option[() => Unit] = None

  private var active = false

  private var onClickOpt: Option[() => Unit] = None

  def onActivStart(f: () => Unit): Unit = {
    onActiveStartOpt = Some(f)
  }

  def onActivStop(f: () => Unit) = {
    onActiveStopOpt = Some(f)
  }

  def onClick(f: () => Unit): Unit = onClickOpt = Some(f)

  def draw(g: DoctusGraphics): Unit = {
    if (active) {
      g.fill(Layout.colorSelected, 50)
    } else {
      g.fill(Layout.colorUnselected, 50)
    }
    g.rect(origin, width, height)

    g.fill(DoctusColorWhite, Layout.boxAlpha)
    g.stroke(DoctusColorBlack, Layout.textAlpha)
    val rectOrigin = origin + DoctusVector(width - Layout.textSize - Layout.textIndent, 0)
    g.rect(rectOrigin, Layout.textSize + Layout.textIndent, txtlen + 5)

    g.textSize(Layout.textSize)
    g.fill(DoctusColorBlack, Layout.textAlpha)
    val txtOrigin = origin + DoctusVector(width - Layout.textIndent, txtlen)
    g.text(txt, txtOrigin, math.Pi / 2)
  }

  def draggableStart(p: DoctusPoint): Unit = {
    if (active) {
      if (isEnclosing(p)) {
        onActiveStartOpt.foreach(f => f())
      } else {
        active = false
        onActiveStopOpt.foreach(f => f())
      }
    } else {
      if (isEnclosing(p)) {
        active = true
        onActiveStartOpt.foreach(f => f())
      } else {
        onActiveStopOpt.foreach(f => f())
      }
    }
  }

  def draggableStop(p: DoctusPoint): Unit = if (isEnclosing(p)) {
    if (active && isEnclosing(p)) {
      onClickOpt foreach (f => f())
      onActiveStopOpt.foreach { f => f() }
      active = false
    }

  }

  def draggableDrag(p: DoctusPoint): Unit = {
    active = isEnclosing(p) && active
  }

}

trait RadioButton[T] extends Component {

  trait SubButton[U] {
    def draw(g: DoctusGraphics, index: Int): Unit

    def rect: Rect

    def value: U
  }

  protected var selectedIndex = 0
  private var preselectedIndex: Option[Int] = None
  private var active = false
  private var onActiveStartOpt: Option[() => Unit] = None
  private var onActiveStopOpt: Option[(Option[T]) => Unit] = None

  protected def subButtons: List[SubButton[T]]

  def onActivStart(f: () => Unit): Unit = {
    onActiveStartOpt = Some(f)
  }

  def onActivStop(f: (Option[T]) => Unit) = {
    onActiveStopOpt = Some(f)
  }

  def draw(g: DoctusGraphics): Unit = {
    subButtons.zipWithIndex.foreach {
      case (sb, i) => sb.draw(g, i)
    }
  }

  def draggableStart(p: DoctusPoint): Unit = {
    if (isEnclosing(p)) {
      preselectedIndex = calculateSelectionIndex(p)
      active = true
      onActiveStartOpt.foreach(f => f())
    }
  }

  def draggableStop(p: DoctusPoint): Unit = {
    if (active) {
      preselectedIndex = None
      active = false
      onActiveStopOpt.foreach { f =>
        calculateSelectionIndex(p) match {
          case None => f(None)
          case Some(i) =>
            selectedIndex = i
            val v = subButtons(i).value
            f(Some(v))
        }
      }
    }
  }

  def draggableDrag(p: DoctusPoint): Unit = {
    if (active) {
      preselectedIndex = calculateSelectionIndex(p)
    }
  }

  protected def isPreselected(index: Int): Boolean = {
    preselectedIndex.isDefined && preselectedIndex.get == index
  }

  protected def buttonsRect(i: Int, size: Int): Rect = {
    def buttonRectanglesHorizontal = {
      val w1 = width / size
      val w = math.ceil(w1)
      val h = height
      Rect(DoctusPoint(i * w1 + origin.x, origin.y), w, h)
    }
    def buttonRectanglesVertical = {
      val w = width
      val h1 = height / size
      val h = math.ceil(h1)
      Rect(DoctusPoint(origin.x, i * h1 + origin.y), w, h)
    }

    if (width > height) buttonRectanglesHorizontal
    else buttonRectanglesVertical
  }

  private def calculateSelectionIndex(p: DoctusPoint): Option[Int] = {
    val rects = subButtons.map(_.rect)
    val sel = rects.zipWithIndex.filter { case (r, i) => r.isEnclosing(p) }
    sel match {
      case Nil         => None
      case (_, i) :: _ => Some(i)
    }
  }
}

case class RadioButtonColorCol(origin: DoctusPoint, width: Double, height: Double) extends RadioButton[DoctusColor] {

  override def draw(g: DoctusGraphics): Unit = {
    val txtLen = 40
    val txt = "color"
    super.draw(g)
    g.fill(DoctusColorWhite, Layout.textAlpha)
    g.stroke(DoctusColorBlack, Layout.textAlpha)
    g.rect(origin, txtLen, Layout.textSize + Layout.textIndent)

    g.textSize(Layout.textSize)
    g.fill(DoctusColorBlack, Layout.textAlpha)
    val txtOrigin = origin + DoctusVector(Layout.textIndent, Layout.textSize)
    g.text(txt, txtOrigin, 0)
  }

  case class ColorButton(color: DoctusColor, rect: Rect) extends SubButton[DoctusColor] {

    private val gray = DoctusColorRgb(150, 150, 150)

    def value = color

    def draw(g: DoctusGraphics, index: Int): Unit = {
      g.stroke(DoctusColorBlack, Layout.textAlpha)
      g.strokeWeight(Layout.textBoxStrokeWeight)
      g.fill(color, Layout.textAlpha)
      g.rect(rect.origin, rect.width, rect.height)
      if (isPreselected(index)) {
        // Nothing to do
      } else if (index != selectedIndex) {
        g.fill(gray, 150)
        g.rect(rect.origin, rect.width, rect.height)
      }
    }

  }

  lazy val subButtons: List[SubButton[DoctusColor]] = {
    val n = 22
    val step = (360.0 / n).toInt
    val colorList = (0 to (360 - step, step)).toList.map { h =>
      val (r, g, b) = DoctusColorUtil.hsv2rgb(h, 100, 100)
      DoctusColorRgb(r, g, b)
    }
    colorList.zipWithIndex.map {
      case (c, i) =>
        val rect = buttonsRect(i, colorList.size)
        ColorButton(c, rect)
    }
  }

}

case class RadioButtonColorBW(origin: DoctusPoint, width: Double, height: Double) extends RadioButton[DoctusColor] {

  override def draw(g: DoctusGraphics): Unit = {
    super.draw(g)
    if (width > height) {
      g.fill(DoctusColorWhite, Layout.boxAlpha)
      g.stroke(DoctusColorBlack, Layout.textAlpha)
      val rectOrigin = origin + DoctusVector(width - Layout.textSize - Layout.textIndent, 0)
      g.rect(rectOrigin, Layout.textSize + Layout.textIndent, 65)

      g.textSize(Layout.textSize)
      g.fill(DoctusColorBlack, Layout.textAlpha)
      val txtOrigin = origin + DoctusVector(width - 3, 60)
      g.text("black/white", txtOrigin, math.Pi / 2)
    }
  }

  case class ColorButton(color: DoctusColor, rect: Rect) extends SubButton[DoctusColor] {

    def value = color

    def drawText(g: DoctusGraphics): Unit = {
      val txt = if (color == DoctusColorBlack) "b" else "w"
      g.textSize(Layout.textSize)
      g.fill(DoctusColorBlack, Layout.textAlpha)
      val txtOrigin = rect.origin + DoctusVector(Layout.textIndent, Layout.textSize)
      g.text(txt, txtOrigin, 0)
    }

    def draw(g: DoctusGraphics, index: Int): Unit = {
      g.stroke(DoctusColorBlack, Layout.textAlpha)
      if (isPreselected(index)) {
        g.fill(Layout.colorPreselected, Layout.textAlpha)
        g.rect(rect.origin, rect.width, rect.height)
        drawText(g)
      } else if (index == selectedIndex) {
        g.fill(Layout.colorSelected, Layout.textAlpha)
        g.rect(rect.origin, rect.width, rect.height)
        drawText(g)
      } else {
        g.fill(Layout.colorUnselected, Layout.textAlpha)
        g.rect(rect.origin, rect.width, rect.height)
        drawText(g)
      }
    }

  }

  lazy val subButtons: List[SubButton[DoctusColor]] = {
    val rb = buttonsRect(0, 2)
    val rw = buttonsRect(1, 2)
    List(ColorButton(DoctusColorBlack, rb), ColorButton(DoctusColorWhite, rw))
  }

}

case class RadioButtonGenericConfig[T](txt: String, txtLen: Int, values: List[T], initialSelIndex: Int)

case class RadioButtonGeneric[T](origin: DoctusPoint, width: Double, height: Double, conf: RadioButtonGenericConfig[T]) extends RadioButton[T] {

  override def draw(g: DoctusGraphics): Unit = {
    super.draw(g)
    g.fill(DoctusColorWhite, Layout.boxAlpha)
    g.stroke(DoctusColorBlack, Layout.textAlpha)
    g.textSize(Layout.textSize)
    if (width >= height) {
      g.rect(origin, conf.txtLen, Layout.textSize + Layout.textIndent)
      g.fill(DoctusColorBlack, Layout.textAlpha)
      val txtOrigin = origin + DoctusVector(Layout.textIndent, Layout.textSize)
      g.text(conf.txt, txtOrigin, 0)
    } else {
      val rectOrigin = origin + DoctusVector(width - Layout.textSize - Layout.textIndent, 0)
      g.rect(rectOrigin, Layout.textSize + Layout.textIndent, conf.txtLen + 5)
      g.fill(DoctusColorBlack, Layout.textAlpha)
      val txtOrigin = origin + DoctusVector(width - Layout.textIndent, conf.txtLen)
      g.text(conf.txt, txtOrigin, math.Pi / 2)
    }
  }

  selectedIndex = conf.initialSelIndex

  lazy val subButtons: List[SubButton[T]] = {
    conf.values.zipWithIndex map {
      case (v, i) =>
        val rect = buttonsRect(i, conf.values.size)
        GenericButton(v, rect)
    }
  }

  case class GenericButton(value: T, rect: Rect) extends SubButton[T] {

    private def creaGray(value: Int): DoctusColor = {
      DoctusColorRgb(value, value, value)
    }

    def drawText(g: DoctusGraphics): Unit = {
      val txt = value match {
        case v: DisplayTextAware => v.displayText
        case _ => value.toString
      }
      g.textSize(Layout.textSize)
      g.fill(DoctusColorBlack, Layout.textAlpha)
      val txtOrigin = rect.origin + DoctusVector(Layout.textIndent, rect.height - Layout.textIndent)
      g.text(txt, txtOrigin, 0)
    }

    def draw(g: DoctusGraphics, index: Int): Unit = {
      g.stroke(DoctusColorBlack, Layout.textAlpha)
      if (isPreselected(index)) {
        g.fill(Layout.colorPreselected, Layout.textAlpha)
        g.rect(rect.origin, rect.width, rect.height)
        drawText(g)
      } else if (index == selectedIndex) {
        g.fill(Layout.colorSelected, Layout.textAlpha)
        g.rect(rect.origin, rect.width, rect.height)
        drawText(g)
      } else {
        g.fill(Layout.colorUnselected, Layout.textAlpha)
        g.rect(rect.origin, rect.width, rect.height)
        drawText(g)
      }
    }

  }

}