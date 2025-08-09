package nutritionapp.component

import scalafx.geometry.{Insets, Pos, Point3D}
import scalafx.scene.control.Label
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.animation.{RotateTransition, Interpolator}
import scalafx.util.Duration
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.Includes._

import nutritionapp.model.Food

class FoodCard(food: Food) extends StackPane {

  // --- Card size (5 across) ---
  prefWidth  = 210; minWidth  = 210; maxWidth  = 210
  prefHeight = 230; minHeight = 230; maxHeight = 230

  // --- Visuals ---
  style =
    """
      |-fx-background-color: white;
      |-fx-background-radius: 14;
      |-fx-border-radius: 14;
      |-fx-border-color: #E6F2EC;
      |-fx-padding: 10;
      """.stripMargin
  effect = new DropShadow {
    radius = 6
    color  = Color.rgb(13, 59, 47, 0.06)
    offsetX = 0; offsetY = 1
  }

  // --- Robust image loading (classpath `/images/...` OR filesystem fallback) ---
  private def toUrl(path: String): String = {
    val withSlash = if (path.startsWith("/")) path else "/" + path
    val fromCp = Option(getClass.getResource(withSlash)).map(_.toExternalForm)
    fromCp.getOrElse("file:" + path)
  }

  private val imageView = new ImageView {
    preserveRatio = true
    smooth = true
    cache = true
    fitWidth = 160
    fitHeight = 120
  }
  private val imgPath = Option(food.imagePath).getOrElse("")
  imageView.image = new Image(toUrl(imgPath), true)

  // --- Front side ---
  private val nameLbl = new Label(Option(food.name).getOrElse("Unnamed")) {
    font = Font.font("Segoe UI", FontWeight.Bold, 14)
    textFill = Color.web("#0D3B2F")
  }
  private val kcalInt = Math.round(Option(food.calories).getOrElse(0.0)).toInt
  private val kcalLbl = new Label(s"$kcalInt kcal") {
    font = Font.font("Segoe UI", FontWeight.Normal, 12)
    textFill = Color.web("#2F6D5F")
  }
  private val front = new VBox {
    alignment = Pos.TopCenter
    spacing = 10
    children = Seq(
      new StackPane {
        style =
          """
            |-fx-background-color: #F6FAF7;
            |-fx-background-radius: 10;
            """.stripMargin
        padding = Insets(10)
        children = imageView
        prefHeight = 140
      },
      new VBox {
        alignment = Pos.Center
        spacing = 4
        children = Seq(nameLbl, kcalLbl)
      }
    )
  }

  // --- Back side (macros) — centered + bold as requested ---
  private def fmt(x: Double, d: Int): String = {
    val v = Option(x).getOrElse(0.0)
    String.format(s"%1.${d}f", Double.box(v))
  }
  private val back = new VBox(8) {
    alignment = Pos.Center
    padding = Insets(16, 12, 16, 12)
    style =
      """
        |-fx-background-color: white;
        |-fx-background-radius: 14;
        """.stripMargin
    private def stat(text: String) = new Label(text) {
      font = Font.font("Segoe UI", FontWeight.Bold, 13)
      textFill = Color.web("#0D3B2F") // darker ink for emphasis
    }
    children = Seq(
      stat(s"Calories: ${kcalInt} kcal"),
      stat(s"Protein:  ${fmt(food.protein, 1)} g"),
      stat(s"Carbs:    ${fmt(food.carbs,   1)} g"),
      stat(s"Fats:     ${fmt(food.fats,    1)} g"),
      stat(s"Fiber:    ${fmt(food.fiber,   1)} g")
    )
  }

  // --- Flip state/anim ---
  private var showingFront = true
  private var isFlipping   = false
  rotationAxis = Point3D(0, 1, 0)  // rotate around Y

  private def flip(): Unit = {
    if (isFlipping) return
    isFlipping = true

    val first = new RotateTransition(Duration(140), this) {
      fromAngle = 0
      toAngle = 90
      interpolator = Interpolator.EaseIn
    }
    first.onFinished = _ => {
      children = if (showingFront) Seq(back) else Seq(front)
      val second = new RotateTransition(Duration(160), this) {
        fromAngle = 90
        toAngle = 0
        interpolator = Interpolator.EaseOut
      }
      second.onFinished = _ => { showingFront = !showingFront; isFlipping = false }
      second.play()
    }
    first.play()
  }

  // init with front
  children = Seq(front)

  // --- Hover + flip on click / Enter ---
  focusTraversable = true
  onMouseEntered = _ => { scaleX = 1.05; scaleY = 1.05 }
  onMouseExited  = _ => { scaleX = 1.0;  scaleY = 1.0  }
  onMouseClicked = _ => flip()
  onKeyPressed   = (e: KeyEvent) => if (e.code == KeyCode.Enter) flip()
}
