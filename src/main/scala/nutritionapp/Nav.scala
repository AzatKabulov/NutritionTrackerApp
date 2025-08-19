package nutritionapp

import scalafx.scene.{Parent, Scene}
import scalafx.stage.Stage

object Nav {
  /** Swap the scene and then apply the remembered window size rules. */
  def go(stage: Stage, root: Parent, defaultToMaximize: Boolean = true): Unit = {
    stage.scene = new Scene(root)
    WindowState.applyTo(stage, defaultToMaximize)
  }
}
