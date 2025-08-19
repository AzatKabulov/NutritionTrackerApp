package nutritionapp

import scalafx.application.Platform
import scalafx.stage.{Screen, Stage}

object WindowState {
  case class Bounds(x: Double, y: Double, w: Double, h: Double)

  private var normal: Option[Bounds] = None       // last non-maximized size/pos
  private var isMaximized: Boolean = true         // start maximized by default

  def init(stage: Stage): Unit = {
    stage.maximized.onChange((_, _, now) => {
      isMaximized = now
      if (!now) capture(stage)
    })
    def onGeom(): Unit = if (!stage.maximized.value) capture(stage)
    stage.width.onChange((_, _, _)  => onGeom())
    stage.height.onChange((_, _, _) => onGeom())
    stage.x.onChange((_, _, _)      => onGeom())
    stage.y.onChange((_, _, _)      => onGeom())
  }

  private def capture(stage: Stage): Unit =
    normal = Some(Bounds(stage.x.value, stage.y.value, stage.width.value, stage.height.value))

  /** Apply the remembered state to this stage.
   * If we’ve never sized the window before, it opens maximized.
   */
  def applyTo(stage: Stage, defaultToMaximize: Boolean = true): Unit = {
    val vb = Screen.primary.visualBounds
    (normal, isMaximized) match {
      case (Some(b), false) =>
        val w = math.min(b.w, vb.width)
        val h = math.min(b.h, vb.height)
        val x = math.max(vb.minX, math.min(b.x, vb.minX + vb.width  - w))
        val y = math.max(vb.minY, math.min(b.y, vb.minY + vb.height - h))
        stage.fullScreen = false
        stage.maximized = false
        stage.x = x; stage.y = y; stage.width = w; stage.height = h
      case _ =>
        if (defaultToMaximize) {
          stage.fullScreen = false
          stage.x = vb.minX; stage.y = vb.minY
          stage.width = vb.width; stage.height = vb.height
          stage.maximized = true
          Platform.runLater { stage.maximized = true } // Windows quirk after scene swap
        }
    }
  }
}
