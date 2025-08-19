package nutritionapp.component

import nutritionapp.model.Meal
import scalafx.geometry.{Insets, Pos, Point3D}
import scalafx.scene.control.{Label, Button, Tooltip}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, StackPane, VBox, Region, Priority}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.animation.{RotateTransition, Interpolator}
import scalafx.util.Duration
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.Includes._

object MealCardMini {

  private val INK     = "#0D3B2F"
  private val SUBTLE  = "#2F6D5F"
  private val PRIMARY = "#6DBE75" // same green as FoodCardMini

  // Backwards-compatible (single-select)
  def apply(meal: Meal, onSelect: () => Unit): VBox =
    apply(meal, compact = true, initiallySelected = false, onToggle = sel => if (sel) onSelect())

  // Backwards-compatible (toggle API)
  def apply(meal: Meal, initiallySelected: Boolean, onToggle: Boolean => Unit): VBox =
    apply(meal, compact = true, initiallySelected = initiallySelected, onToggle = onToggle)

  // Same footprint as FoodCardMini: 210x230 card, frame, flip-on-click
  def apply(meal: Meal, compact: Boolean, initiallySelected: Boolean, onToggle: Boolean => Unit): VBox = {

    val scale = if (compact) 0.85 else 1.0
    def sz(v: Double): Double = math.round(v * scale)

    val CardW     = sz(210); val CardH = sz(230)
    val FramePad  = sz(10);  val InnerPad = sz(12)
    val InnerW    = CardW - 2*FramePad
    val InnerH    = CardH - 2*FramePad

    val root = new VBox { prefWidth = CardW; minWidth = CardW; maxWidth = CardW }
    root.effect = new DropShadow { radius = 6; color = Color.rgb(13,59,47,0.10); offsetX = 0; offsetY = 1 }

    def frameColorFor(cat: String): String = cat.toLowerCase match {
      case "breakfast" => "#B7F2C1"
      case "lunch"     => "#BFDFFF"
      case "dinner"    => "#FFC9DE"
      case "snack"     => "#FFEB99"
      case _           => "#E6F2EC"
    }
    val frameColor = frameColorFor(Option(meal.category).getOrElse(""))

    def toUrl(path: String): String = {
      val p = Option(path).getOrElse("")
      val pp = if (p.startsWith("/")) p else "/" + p
      Option(getClass.getResource(pp)).map(_.toExternalForm).getOrElse("file:" + p)
    }

    val imageView = new ImageView {
      preserveRatio = true; smooth = true; cache = true
      fitWidth = sz(150); fitHeight = sz(110)
      image = new Image(toUrl(Option(meal.imagePath).getOrElse("")), true)
    }

    val nameLbl = new Label(Option(meal.name).getOrElse("Unnamed")) {
      font = Font.font("Segoe UI", FontWeight.Bold, sz(14))
      textFill = Color.web(INK)
      wrapText = true
    }
    val kcalLbl = new Label(s"${meal.totalCalories.toInt} kcal") {
      font = Font.font("Segoe UI", FontWeight.SemiBold, sz(11))
      textFill = Color.web(SUBTLE)
    }

    val selectBtnFront = new Button("Select") {
      maxWidth = Double.MaxValue
      style = s"-fx-background-color: $PRIMARY; -fx-text-fill: white; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(7)} ${sz(10)};"
      tooltip = new Tooltip("Add to selection")
    }
    val selectBtnBack = new Button("Select") {
      maxWidth = Double.MaxValue
      style = s"-fx-background-color: $PRIMARY; -fx-text-fill: white; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(7)} ${sz(10)};"
      tooltip = new Tooltip("Add to selection")
    }

    var selected = initiallySelected
    var inner: StackPane = null

    def applySelectVisual(): Unit = {
      val text = if (selected) "Selected ✓" else "Select"
      val styleSel = s"-fx-background-color: white; -fx-text-fill: #0D3B2F; -fx-border-color: $PRIMARY; -fx-border-radius: ${sz(10)}; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(7)} ${sz(10)};"
      val styleDef = s"-fx-background-color: $PRIMARY; -fx-text-fill: white; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(7)} ${sz(10)};"
      selectBtnFront.text = text; selectBtnBack.text = text
      selectBtnFront.style = if (selected) styleSel else styleDef
      selectBtnBack.style  = if (selected) styleSel else styleDef
      if (inner != null) {
        inner.style =
          (if (selected) s"-fx-border-color: $PRIMARY; -fx-border-width: 2;" else "-fx-border-color: #E6F2EC; -fx-border-width: 1;") +
            s" -fx-background-color: white; -fx-background-radius: ${sz(14)}; -fx-border-radius: ${sz(14)}; -fx-padding: ${sz(10)};"
      }
    }
    def toggleSelect(): Unit = { selected = !selected; applySelectVisual(); onToggle(selected) }
    selectBtnFront.onAction = _ => toggleSelect()
    selectBtnBack.onAction  = _ => toggleSelect()

    // FRONT — exactly like FoodCardMini (image + name + calories + Select)
    val front = new VBox {
      alignment = Pos.Center; spacing = sz(6)
      prefHeight = InnerH - 2*InnerPad; minHeight = prefHeight(); maxHeight = prefHeight()
      prefWidth  = InnerW - 2*InnerPad;  minWidth  = prefWidth();  maxWidth  = prefWidth()
      children = Seq(
        imageView,
        new VBox { alignment = Pos.Center; spacing = sz(2); children = Seq(nameLbl, kcalLbl) },
        new Region { prefHeight = sz(2); VBox.setVgrow(this, Priority.Always) },
        selectBtnFront
      )
    }

    // BACK — “Per serving” breakdown
    def fmt(x: Double, d: Int): String = String.format(s"%1.${d}f", Double.box(x))
    val capLabel = new Label("Per serving") {
      font = Font.font("Segoe UI", FontWeight.SemiBold, sz(10)); textFill = Color.web(SUBTLE)
    }
    def mkStat(s: String) = new Label(s) { font = Font.font("Segoe UI", FontWeight.Bold, sz(12)); textFill = Color.web(INK) }
    val calLbl = mkStat(s"Calories: ${meal.totalCalories.toInt} kcal")
    val protLbl = mkStat(s"Protein:  ${fmt(meal.totalProtein, 1)} g")
    val carbLbl = mkStat(s"Carbs:    ${fmt(meal.totalCarbs, 1)} g")
    val fatLbl  = mkStat(s"Fats:     ${fmt(meal.totalFats, 1)} g")

    val back = new VBox(sz(6)) {
      alignment = Pos.Center
      padding = Insets(InnerPad, InnerPad, InnerPad, InnerPad)
      prefHeight = InnerH - 2*InnerPad; minHeight = prefHeight(); maxHeight = prefHeight()
      prefWidth  = InnerW - 2*InnerPad;  minWidth  = prefWidth();  maxWidth  = prefWidth()
      children = Seq(capLabel, calLbl, protLbl, carbLbl, fatLbl, new Region { VBox.setVgrow(this, Priority.Always) }, selectBtnBack)
    }

    // Card structure + flip
    inner = new StackPane {
      children = Seq(front)
      prefHeight = InnerH; minHeight = InnerH; maxHeight = InnerH
      prefWidth  = InnerW; minWidth  = InnerW; maxWidth  = InnerW
    }
    val frame = new StackPane {
      style = s"-fx-background-color: $frameColor; -fx-background-radius: ${sz(18)};"
      padding = Insets(FramePad)
      children = inner
    }
    val card = new VBox { prefWidth = CardW; children = Seq(frame) }

    applySelectVisual()

    var showingFront = true; var isFlipping = false
    inner.rotationAxis = Point3D(0, 1, 0)
    def flip(): Unit = {
      if (isFlipping) return; isFlipping = true
      val first = new RotateTransition(Duration(140 * scale), inner) { fromAngle = 0; toAngle = 90; interpolator = Interpolator.EaseIn }
      first.onFinished = _ => {
        inner.children = if (showingFront) Seq(back) else Seq(front)
        val second = new RotateTransition(Duration(160 * scale), inner) { fromAngle = 90; toAngle = 0; interpolator = Interpolator.EaseOut }
        second.onFinished = _ => { showingFront = !showingFront; isFlipping = false }; second.play()
      }
      first.play()
    }

    card.focusTraversable = true
    card.onMouseEntered = _ => { card.scaleX = 1.05; card.scaleY = 1.05 }
    card.onMouseExited  = _ => { card.scaleX = 1.0;  card.scaleY = 1.0  }
    card.onMouseClicked = e => if (e.getTarget != selectBtnFront.delegate && e.getTarget != selectBtnBack.delegate) flip()
    card.onKeyPressed   = (e: KeyEvent) => if (e.code == KeyCode.Enter) flip()

    card
  }
}
