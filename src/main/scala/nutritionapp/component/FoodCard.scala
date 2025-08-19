package nutritionapp.component

import scalafx.geometry.{Insets, Pos, Point3D}
import scalafx.scene.control.{Label, ToggleButton, ToggleGroup, Button, ChoiceDialog}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.animation.{RotateTransition, Interpolator}
import scalafx.util.Duration
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.Includes._
import nutritionapp.model.Food

// Let parents listen even if no callback is passed
class AddToPlannerEvent(val f: Food, val meal: String)
  extends javafx.event.Event(AddToPlannerEvent.EventType)
object AddToPlannerEvent {
  val EventType = new javafx.event.EventType[javafx.event.Event]("ADD_TO_PLANNER")
}

/** onAddToPlanner gets called with (food, selectedMeal) after the user picks a meal.
 * categories controls the options shown in the picker.
 */
class FoodCard(
                food: Food,
                onAddToPlanner: (Food, String) => Unit = (_, _) => (),
                categories: Seq[String] = Seq("Breakfast", "Lunch", "Dinner", "Snack")
              ) extends StackPane {

  // ✅ Use category-colored outer frame
  private val UseCategoryFrame = true

  // --- sizing ---
  private val CardW = 210.0
  private val CardH = 230.0
  private val FramePad = 10.0
  private val InnerPad = 12.0
  private val InnerW = CardW - 2 * FramePad
  private val InnerH = CardH - 2 * FramePad
  private val ContentH = InnerH - 2 * InnerPad

  prefWidth = CardW; minWidth = CardW; maxWidth = CardW
  prefHeight = CardH; minHeight = CardH; maxHeight = CardH
  effect = new DropShadow { radius = 6; color = Color.rgb(13, 59, 47, 0.10); offsetX = 0; offsetY = 1 }

  // --- frame color by category ---
  private def frameColorFor(cat: String): String = cat match {
    case c if c.equalsIgnoreCase("Fruits")        => "#FFC9DE" // pink
    case c if c.equalsIgnoreCase("Vegetables")    => "#B7F2C1" // green
    case c if c.equalsIgnoreCase("Dairy")         => "#BFDFFF" // blue
    case c if c.equalsIgnoreCase("Fats")          => "#FFC07A" // orange
    case c if c.equalsIgnoreCase("Carbohydrates") => "#FFEB99" // yellow
    case c if c.equalsIgnoreCase("Protein")       => "#F3A5A5" // red-ish
    case _                                        => "#E8F2FF" // default light blue
  }
  private val frameColor = frameColorFor(Option(food.category).getOrElse(""))

  // --- image ---
  private def toUrl(path: String): String = {
    val p = Option(path).getOrElse("")
    val pp = if (p.startsWith("/")) p else "/" + p
    Option(getClass.getResource(pp)).map(_.toExternalForm).getOrElse("file:" + p)
  }
  private val imageView = new ImageView {
    preserveRatio = true; smooth = true; cache = true
    fitWidth = 150; fitHeight = 120
  }
  imageView.image = new Image(toUrl(Option(food.imagePath).getOrElse("")), true)

  // --- per-unit metadata for a few items ---
  private val UnitLookup: Map[String, (String, Double)] = Map(
    "Egg"            -> ("egg",     50.0),
    "Bread (White)"  -> ("slice",   25.0),
    "Apple"          -> ("apple",  182.0),
    "Banana"         -> ("banana", 118.0),
    "Swiss Cheese"   -> ("slice",   28.0),
    "Avocado"        -> ("avocado",150.0)
  )
  private val (unitLabelOpt, unitGramsOpt) = {
    val nm = Option(food.name).getOrElse("")
    UnitLookup.get(nm).map { case (lbl, g) => (Some(lbl), Some(g)) }.getOrElse((None, None))
  }
  private val hasUnit = unitGramsOpt.isDefined

  // --- front face (image + name + kcal) ---
  private def kcalPer100: Int = math.round(Option(food.calories).getOrElse(0.0)).toInt
  private def kcalPerUnit: Int = math.round(Option(food.calories).getOrElse(0.0) * unitGramsOpt.map(_ / 100.0).getOrElse(1.0)).toInt

  private val nameLbl = new Label(Option(food.name).getOrElse("Unnamed")) {
    font = Font.font("Segoe UI", FontWeight.Bold, 14); textFill = Color.web("#0D3B2F")
  }
  private val kcalLbl = new Label(s"${if (hasUnit) kcalPerUnit else kcalPer100} kcal") {
    font = Font.font("Segoe UI", FontWeight.SemiBold, 12); textFill = Color.web("#2F6D5F")
  }
  private val front = new VBox {
    alignment = Pos.Center; spacing = 8
    prefHeight = ContentH; minHeight = ContentH; maxHeight = ContentH
    prefWidth = InnerW - 2 * InnerPad; minWidth = prefWidth(); maxWidth = prefWidth()
    children = Seq(imageView, new VBox { alignment = Pos.Center; spacing = 2; children = Seq(nameLbl, kcalLbl) })
  }

  // --- back face (stats + unit switch + add button) ---
  private sealed trait Disp; private case object Per100g extends Disp; private case object PerUnit extends Disp
  private var dispMode: Disp = if (hasUnit) PerUnit else Per100g
  private def fmt(x: Double, d: Int): String = String.format(s"%1.${d}f", Double.box(Option(x).getOrElse(0.0)))
  private def scale: Double = if (dispMode == Per100g) 1.0 else unitGramsOpt.map(_ / 100.0).getOrElse(1.0)

  private val per100Btn = new ToggleButton("100 g") { focusTraversable = false }
  private val perUnitBtn = new ToggleButton(unitLabelOpt.map(lbl => s"1 $lbl").getOrElse("1 unit")) {
    focusTraversable = false; disable = !hasUnit
  }
  per100Btn.minWidth = 68; per100Btn.minHeight = 28
  perUnitBtn.minWidth = 68; perUnitBtn.minHeight = 28

  private val modeGroup = new ToggleGroup()
  per100Btn.toggleGroup = modeGroup
  perUnitBtn.toggleGroup = modeGroup
  if (hasUnit) perUnitBtn.setSelected(true) else per100Btn.setSelected(true)

  private def btnStyle(selected: Boolean, disabled: Boolean): String = {
    val base =
      """
        |-fx-background-radius: 12,12,12;
        |-fx-border-radius: 12;
        |-fx-background-insets: 0,0,0;
        |-fx-border-insets: 0;
        |-fx-padding: 4 10;
        |-fx-font-size: 12px;
        |-fx-border-width: 1;
        |-fx-cursor: hand;
        """.stripMargin
    if (disabled)
      base + "-fx-background-color: transparent, transparent, transparent; -fx-text-fill: #A0B7AE; -fx-border-color: transparent; -fx-opacity: 0.65;"
    else if (selected)
      base + s"-fx-background-color: $frameColor, $frameColor, $frameColor; -fx-text-fill: #0D3B2F; -fx-border-color: derive($frameColor, -20%);"
    else
      base + "-fx-background-color: white, white, white; -fx-text-fill: #0D3B2F; -fx-border-color: #E2E8F0;"
  }
  private def applyBtnStyles(): Unit = {
    per100Btn.style = btnStyle(per100Btn.isSelected, per100Btn.disable.value)
    perUnitBtn.style = btnStyle(perUnitBtn.isSelected, perUnitBtn.disable.value)
  }

  private val capLabel = new Label("") { font = Font.font("Segoe UI", FontWeight.SemiBold, 11); textFill = Color.web("#2F6D5F") }
  private def statLine(name: String, v: => String) =
    new HBox(8,
      new Label(name) { font = Font.font("Segoe UI", FontWeight.SemiBold, 12); textFill = Color.web("#0D3B2F") },
      new Label(v)    { font = Font.font("Segoe UI", FontWeight.Normal, 12); textFill = Color.web("#2F6D5F") }
    ) { alignment = Pos.CenterLeft }

  private val caloriesL = statLine("Calories:", "")
  private val proteinL  = statLine("Protein:",  "")
  private val carbsL    = statLine("Carbs:",    "")
  private val fatsL     = statLine("Fats:",     "")
  private val fiberL    = statLine("Fiber:",    "")

  private def updateStats(): Unit = {
    capLabel.setText(if (dispMode == Per100g) "Per 100 g" else s"Per 1 ${unitLabelOpt.get} (${fmt(unitGramsOpt.get,0)} g)")
    val s = scale
    caloriesL.getChildren.get(1).asInstanceOf[javafx.scene.control.Label].setText(s"${math.round(Option(food.calories).getOrElse(0.0) * s).toInt} kcal")
    proteinL .getChildren.get(1).asInstanceOf[javafx.scene.control.Label].setText(s"${fmt(Option(food.protein).getOrElse(0.0) * s, 1)} g")
    carbsL   .getChildren.get(1).asInstanceOf[javafx.scene.control.Label].setText(s"${fmt(Option(food.carbs).getOrElse(0.0)   * s, 1)} g")
    fatsL    .getChildren.get(1).asInstanceOf[javafx.scene.control.Label].setText(s"${fmt(Option(food.fats).getOrElse(0.0)    * s, 1)} g")
    fiberL   .getChildren.get(1).asInstanceOf[javafx.scene.control.Label].setText(s"${fmt(Option(food.fiber).getOrElse(0.0)   * s, 1)} g")
  }

  per100Btn.onAction = _ => { if (!per100Btn.isSelected) per100Btn.setSelected(true); dispMode = Per100g; updateStats(); applyBtnStyles() }
  perUnitBtn.onAction = _ => if (!perUnitBtn.disable.value) { if (!perUnitBtn.isSelected) perUnitBtn.setSelected(true); dispMode = PerUnit; updateStats(); applyBtnStyles() }
  per100Btn.selected.onChange((_,_,b) => if (b) { dispMode = Per100g; updateStats(); applyBtnStyles() })
  perUnitBtn.selected.onChange((_,_,b) => if (b && !perUnitBtn.disable.value) { dispMode = PerUnit; updateStats(); applyBtnStyles() })

  // --- meal picker + add handler (fires callback AND event) ---
  private def askMealAndAdd(): Unit = {
    val opts = if (categories.nonEmpty) categories else Seq("Breakfast", "Lunch", "Dinner", "Snack")
    val dlg = new ChoiceDialog[String](opts.head, opts) {
      title = "Add to Planner"
      headerText = s"Add ${Option(food.name).getOrElse("this item")} to which meal?"
      contentText = "Choose a meal:"
    }
    dlg.showAndWait() match {
      case Some(meal) =>
        onAddToPlanner(food, meal)                                  // callback path
        this.delegate.fireEvent(new AddToPlannerEvent(food, meal))  // event path
      case None => ()
    }
  }

  private val addBtn = new Button("＋ Add to Planner") {
    maxWidth = Double.MaxValue
    style = "-fx-background-color: #6DBE75; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 7 10; -fx-font-weight: bold;"
    onAction = _ => askMealAndAdd()
  }

  private val segWrap = new HBox {
    spacing = 10; alignment = Pos.Center; children = if (hasUnit) Seq(per100Btn, perUnitBtn) else Seq(per100Btn)
  }

  private val back = new VBox(8) {
    alignment = Pos.TopCenter
    padding = Insets(InnerPad, InnerPad, InnerPad, InnerPad)
    prefHeight = ContentH; minHeight = ContentH; maxHeight = ContentH
    prefWidth  = InnerW - 2 * InnerPad;  minWidth  = prefWidth();  maxWidth  = prefWidth()
    children = Seq(
      segWrap,
      capLabel,
      new VBox(4, caloriesL, proteinL, carbsL, fatsL, fiberL) {
        alignment = Pos.CenterLeft
        style = "-fx-background-color: rgba(13,59,47,0.04); -fx-background-radius: 10; -fx-padding: 8 10;"
      },
      addBtn
    )
  }

  updateStats(); applyBtnStyles()

  private val inner = new StackPane {
    style = "-fx-background-color: white; -fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: #E6F2EC; -fx-padding: 12;"
    children = Seq(front)
    prefHeight = InnerH; minHeight = InnerH; maxHeight = InnerH
    prefWidth  = InnerW; minWidth  = InnerW; maxWidth  = InnerW
  }
  private val frame = new StackPane {
    // Colored pastel frame back on
    style =
      if (UseCategoryFrame)
        s"-fx-background-color: $frameColor; -fx-background-radius: 18;"
      else
        "-fx-background-color: white; -fx-background-radius: 18; -fx-border-color: #E6F2EC; -fx-border-radius: 18;"
    padding = Insets(FramePad)
    children = inner
  }
  children = Seq(frame)

  // --- flip logic with guarded controls fix ---
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

  private val guardedJfxNodes: Seq[javafx.scene.Node] =
    Seq(per100Btn.delegate, perUnitBtn.delegate, addBtn.delegate)

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

  focusTraversable = true
  onMouseEntered = _ => { scaleX = 1.05; scaleY = 1.05 }
  onMouseExited  = _ => { scaleX = 1.0;  scaleY = 1.0 }
  onMouseClicked = e => if (!clickInsideGuarded(e.getTarget)) flip()
  onKeyPressed   = (e: KeyEvent) => if (e.code == KeyCode.Enter) flip()
}
