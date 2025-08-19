package nutritionapp.component

import scalafx.geometry.{Insets, Pos, Point3D}
import scalafx.scene.control.{Button, Label, ChoiceDialog}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.animation.{Interpolator, RotateTransition}
import scalafx.util.Duration
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.Includes._
import nutritionapp.model.Meal

/** Meal card with "Add to Planner" that asks for a meal slot (Breakfast/Lunch/etc)
 * and then calls the provided callback exactly once (no event bus here).
 */
class MealCard(
                meal: Meal,
                onAddToPlanner: (Meal, String) => Unit = (_, _) => (),
                categories: Seq[String] = Seq("Breakfast", "Lunch", "Dinner", "Snack")
              ) extends StackPane {

  // --- sizing (match FoodCard) ---
  private val CardW     = 210.0
  private val CardH     = 230.0
  private val FramePad  = 10.0
  private val InnerPad  = 12.0
  private val InnerW    = CardW - 2 * FramePad
  private val InnerH    = CardH - 2 * FramePad
  private val ContentH  = InnerH - 2 * InnerPad

  prefWidth  = CardW; minWidth = CardW; maxWidth = CardW
  prefHeight = CardH; minHeight = CardH; maxHeight = CardH

  effect = new DropShadow { radius = 6; color = Color.rgb(13,59,47,0.10); offsetX = 0; offsetY = 1 }

  // --- frame color by meal category (aligned with pills) ---
  private def frameColorFor(cat: String): String = cat match {
    case c if c.equalsIgnoreCase("Breakfast") => "#FFEB99" // yellow
    case c if c.equalsIgnoreCase("Lunch")     => "#B7F2C1" // green
    case c if c.equalsIgnoreCase("Dinner")    => "#F3A5A5" // red
    case c if c.equalsIgnoreCase("Snack")     => "#BFDFFF" // blue
    case _                                    => "#E8F2FF"
  }
  private val frameColor = frameColorFor(Option(meal.category).getOrElse(""))

  // --- image loader ---
  private def toUrl(path: String): String = {
    val p  = Option(path).getOrElse("")
    val pp = if (p.startsWith("/")) p else "/" + p
    Option(getClass.getResource(pp)).map(_.toExternalForm).getOrElse("file:" + p)
  }
  private val imageView = new ImageView {
    preserveRatio = true; smooth = true; cache = true
    fitWidth = 150; fitHeight = 120
    image = Option(meal.imagePath).map(toUrl).map(new Image(_, true)).getOrElse(null)
  }

  // --- front face (image + name + kcal) ---
  private def kcalInt: Int = math.round(Option(meal.calories).getOrElse(0.0)).toInt
  private def fmt1(x: Double): String = f"${Option(x).getOrElse(0.0)}%.1f"

  private val nameLbl = new Label(Option(meal.name).getOrElse("Untitled Meal")) {
    font = Font.font("Segoe UI", FontWeight.Bold, 14); textFill = Color.web("#0D3B2F")
  }
  private val kcalLbl = new Label(s"$kcalInt kcal") {
    font = Font.font("Segoe UI", FontWeight.SemiBold, 12); textFill = Color.web("#2F6D5F")
  }

  private val front = new VBox {
    alignment = Pos.Center; spacing = 8
    prefHeight = ContentH; minHeight = ContentH; maxHeight = ContentH
    prefWidth  = InnerW - 2 * InnerPad; minWidth = prefWidth(); maxWidth = prefWidth()
    children = Seq(
      imageView,
      new VBox { alignment = Pos.Center; spacing = 2; children = Seq(nameLbl, kcalLbl) }
    )
  }

  // --- back face (caption + stats panel + CTA) ---
  private val capLabel = new Label("Per 1 serving") {
    font = Font.font("Segoe UI", FontWeight.SemiBold, 11)
    textFill = Color.web("#2F6D5F")
  }

  private def statLine(label: String, value: String) =
    new HBox(8,
      new Label(label) { font = Font.font("Segoe UI", FontWeight.SemiBold, 12); textFill = Color.web("#0D3B2F") },
      new Label(value) { font = Font.font("Segoe UI", FontWeight.Normal,   12); textFill = Color.web("#2F6D5F") }
    ) { alignment = Pos.CenterLeft }

  private val macrosPanel = new VBox(4,
    statLine("Calories:", s"$kcalInt kcal"),
    statLine("Protein:",  s"${fmt1(meal.protein)} g"),
    statLine("Carbs:",    s"${fmt1(meal.carbs)} g"),
    statLine("Fats:",     s"${fmt1(meal.fats)} g")
  ) {
    alignment = Pos.CenterLeft
    style = "-fx-background-color: rgba(13,59,47,0.04); -fx-background-radius: 10; -fx-padding: 8 10;"
  }

  private def askMealAndAdd(): Unit = {
    val opts = if (categories.nonEmpty) categories else Seq("Breakfast", "Lunch", "Dinner", "Snack")
    val dlg = new ChoiceDialog[String](opts.head, opts) {
      title = "Add to Planner"
      headerText = s"Add '${Option(meal.name).getOrElse("this meal")}' to which meal?"
      contentText = "Choose a meal:"
    }
    dlg.showAndWait() match {
      case Some(slot) => onAddToPlanner(meal, slot) // single, deliberate callback
      case None => ()
    }
  }

  private val addBtn = new Button("＋ Add to Planner") {
    maxWidth = Double.MaxValue
    style = "-fx-background-color: #6DBE75; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 7 10; -fx-font-weight: bold;"
    onAction = _ => askMealAndAdd()
  }

  private val back = new VBox(8) {
    alignment = Pos.TopCenter
    padding = Insets(InnerPad, InnerPad, InnerPad, InnerPad)
    prefHeight = ContentH; minHeight = ContentH; maxHeight = ContentH
    prefWidth  = InnerW - 2 * InnerPad; minWidth = prefWidth(); maxWidth = prefWidth()
    children = Seq(
      capLabel,
      macrosPanel,
      addBtn
    )
  }

  // --- inner container & colored frame ---
  private val inner = new StackPane {
    style = "-fx-background-color: white; -fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: #E6F2EC; -fx-padding: 12;"
    children = Seq(front)
    prefHeight = InnerH; minHeight = InnerH; maxHeight = InnerH
    prefWidth  = InnerW; minWidth  = InnerW; maxWidth  = InnerW
  }
  private val frame = new StackPane {
    style = s"-fx-background-color: $frameColor; -fx-background-radius: 18;"
    padding = Insets(FramePad)
    children = inner
  }
  children = Seq(frame)

  // --- flip animation (same as FoodCard) ---
  private var showingFront = true
  private var isFlipping   = false
  inner.rotationAxis = Point3D(0, 1, 0)

  private def flip(): Unit = {
    if (isFlipping) return
    isFlipping = true
    val first = new RotateTransition(Duration(140), inner) {
      fromAngle = 0; toAngle = 90; interpolator = Interpolator.EaseIn
    }
    first.onFinished = _ => {
      inner.children = if (showingFront) Seq(back) else Seq(front)
      val second = new RotateTransition(Duration(160), inner) {
        fromAngle = 90; toAngle = 0; interpolator = Interpolator.EaseOut
      }
      second.onFinished = _ => { showingFront = !showingFront; isFlipping = false }
      second.play()
    }
    first.play()
  }

  // Guard clicks on CTA so it doesn’t cause a flip
  private val guardedJfxNodes: Seq[javafx.scene.Node] = Seq(addBtn.delegate)
  private def clickInsideGuarded(target: Any): Boolean = target match {
    case n: javafx.scene.Node =>
      var cur: javafx.scene.Node = n
      while (cur != null) {
        if (guardedJfxNodes.exists(_ eq cur)) return true
        cur = cur.getParent
      }
      false
    case _ => false
  }

  // hover + input handling
  focusTraversable = true
  onMouseEntered = _ => { scaleX = 1.05; scaleY = 1.05 }
  onMouseExited  = _ => { scaleX = 1.0;  scaleY = 1.0  }
  onMouseClicked = e => if (!clickInsideGuarded(e.getTarget)) flip()
  onKeyPressed   = (e: KeyEvent) => if (e.code == KeyCode.Enter) flip()
}
