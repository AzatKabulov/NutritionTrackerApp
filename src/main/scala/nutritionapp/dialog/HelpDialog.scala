// src/main/scala/nutritionapp/dialog/HelpDialog.scala
package nutritionapp.dialog

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.{Modality, Stage, StageStyle}

object HelpDialog {

  private val LightVars =
    """-bg: rgba(0,0,0,0.35);
      |-card: #FFFFFF;
      |-ink: #0D3B2F;
      |-subtle: #2F6D5F;
      |-primary: #6DBE75;
      |-border: #D9EEE4;""".stripMargin

  private val DarkVars =
    """-bg: rgba(0,0,0,0.45);
      |-card: #121C19;
      |-ink: #EAF7F0;
      |-subtle: #B6E0CE;
      |-primary: #6BD190;
      |-border: #244A3F;""".stripMargin

  private val CardStyle =
    """-fx-background-color: -card;
      |-fx-background-radius: 24;
      |-fx-border-color: -border;
      |-fx-border-radius: 24;
      |-fx-padding: 24;
      |-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.14), 28, 0.24, 0, 12);""".stripMargin

  private val PrimaryBtn =
    """-fx-font-size: 14px;
      |-fx-font-weight: 700;
      |-fx-background-radius: 14;
      |-fx-padding: 10 18 10 18;
      |-fx-pref-width: 120;
      |-fx-cursor: hand;
      |-fx-background-color: -primary;
      |-fx-text-fill: white;""".stripMargin

  // Load 24x24 icon from resources; fallback to a bullet label
  private def loadIcon(name: String): Node = {
    val candidates = Seq(
      s"/icons/$name.png",
      s"/images/$name.png",
      s"/img/$name.png",
      s"/$name.png"
    )
    val urlOpt = candidates.view.map(getClass.getResource).find(_ != null)
    urlOpt match {
      case Some(url) =>
        new ImageView(new Image(url.toExternalForm)) {
          smooth = true
          preserveRatio = true
          fitWidth = 24
          fitHeight = 24
          opacity = 0.96
        }
      case None =>
        new Label("•") {
          font = Font.font(18)
          style = "-fx-text-fill: -ink;"
        }
    }
  }

  private def row(iconName: String, text: String): HBox = new HBox(12) {
    alignment = Pos.TopLeft
    children = Seq(
      loadIcon(iconName),
      new Label(text) {
        font = Font.font("Segoe UI", 14)
        wrapText = true
        maxWidth = 520
        style = "-fx-text-fill: -ink;"
      }
    )
  }

  def show(ownerStage: Stage, dark: Boolean): Unit = {
    val dlg = new Stage {
      initOwner(ownerStage)
      initStyle(StageStyle.Transparent)
      initModality(Modality.ApplicationModal)
      title = "How to Use"
    }

    val header = new HBox(10) {
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Quick Guide") {
          font = Font.font("Segoe UI", FontWeight.Bold, 22)
          style = "-fx-text-fill: -ink;"
        }
      )
    }

    // Use your dashboard icon names; ensure the PNGs exist (see below)
    val list = new VBox(14) {
      alignment = Pos.TopLeft
      children = Seq(
        row("planner",  "Planner — add foods & meals by date/time, and view your day at a glance."),
        row("food",     "Food — manage single foods with macros and serving sizes."),
        row("meals",    "Meals — create combos of foods; totals are calculated automatically."),
        row("profile",  "Profile — set height, weight, goals; target calories update your planner."),
        row("settings", "Settings — logout, theme toggle, and this help.")
      )
    }

    val closeBtn = new Button("Got it") {
      style = PrimaryBtn
      defaultButton = true
    }
    closeBtn.onAction = _ => dlg.close()

    val body = new VBox(16) {
      children = Seq(
        header,
        new Separator { style = "-fx-opacity: 0.35;" },
        list
      )
    }

    val card = new VBox(18) {
      style = CardStyle
      padding = Insets(20)
      minWidth = 640; prefWidth = 640; maxWidth = 640
      children = Seq(body, new HBox { alignment = Pos.CenterRight; children = Seq(closeBtn) })
    }

    val blocker = new StackPane {
      alignment = Pos.Center
      children = Seq(card)
    }
    blocker.onMouseClicked = me => if (me.getTarget eq blocker) dlg.close()

    val overlay = new StackPane {
      style = (if (dark) DarkVars else LightVars) + "; -fx-background-color: -bg;"
      padding = Insets(24)
      children = Seq(blocker)
    }

    dlg.scene = new Scene(overlay) {
      fill = Color.Transparent
      onKeyPressed = ke => if (ke.getCode == KeyCode.Escape) dlg.close()
    }

    dlg.centerOnScreen()
    dlg.showAndWait()
  }
}
