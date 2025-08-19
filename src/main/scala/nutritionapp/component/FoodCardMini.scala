package nutritionapp.component

import scalafx.geometry.{Insets, Pos, Point3D}
import scalafx.scene.control.{Label, ToggleButton, ToggleGroup, Button, Tooltip}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, StackPane, VBox, Region, Priority}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.animation.{RotateTransition, Interpolator}
import scalafx.util.Duration
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.Includes._
import scalafx.scene.Node
import nutritionapp.model.Food
import javafx.scene.{Node => JfxNode}

object FoodCardMini {

  // Backwards-compatible (single-select)
  def apply(food: Food, onSelect: () => Unit): VBox =
    apply(food, compact = true, initiallySelected = false, onToggle = sel => if (sel) onSelect())

  // Backwards-compatible (toggle provided)
  def apply(food: Food, initiallySelected: Boolean, onToggle: Boolean => Unit): VBox =
    apply(food, compact = true, initiallySelected = initiallySelected, onToggle = onToggle)

  // New: compact flag to shrink for dialog windows
  def apply(food: Food, compact: Boolean, initiallySelected: Boolean, onToggle: Boolean => Unit): VBox = {

    val scale = if (compact) 0.85 else 1.0
    def sz(v: Double): Double = math.round(v * scale)

    val CardW     = sz(210); val CardH = sz(230)
    val FramePad  = sz(10);  val InnerPad = sz(12)
    val InnerW    = CardW - 2*FramePad
    val InnerH    = CardH - 2*FramePad

    val root = new VBox { prefWidth = CardW; minWidth = CardW; maxWidth = CardW }
    root.effect = new DropShadow { radius = 6; color = Color.rgb(13,59,47,0.10); offsetX = 0; offsetY = 1 }

    def frameColorFor(cat: String): String = cat match {
      case c if c.equalsIgnoreCase("Fruits")        => "#FFC9DE"
      case c if c.equalsIgnoreCase("Vegetables")    => "#B7F2C1"
      case c if c.equalsIgnoreCase("Dairy")         => "#BFDFFF"
      case c if c.equalsIgnoreCase("Fats")          => "#FFC07A"
      case c if c.equalsIgnoreCase("Carbohydrates") => "#FFEB99"
      case c if c.equalsIgnoreCase("Protein")       => "#F3A5A5"
      case _                                        => "#E8F2FF"
    }
    val frameColor = frameColorFor(Option(food.category).getOrElse(""))

    def toUrl(path: String): String = {
      val p = Option(path).getOrElse("")
      val pp = if (p.startsWith("/")) p else "/" + p
      Option(getClass.getResource(pp)).map(_.toExternalForm).getOrElse("file:" + p)
    }

    val imageView = new ImageView {
      preserveRatio = true; smooth = true; cache = true
      fitWidth = sz(150); fitHeight = sz(110)
      image = new Image(toUrl(Option(food.imagePath).getOrElse("")), true)
    }

    val UnitLookup: Map[String, (String, Double)] = Map(
      "Egg"            -> ("egg",     50.0),
      "Bread (White)"  -> ("slice",   25.0),
      "Apple"          -> ("apple",  182.0),
      "Banana"         -> ("banana", 118.0),
      "Swiss Cheese"   -> ("slice",   28.0),
      "Avocado"        -> ("avocado",150.0)
    )
    val (unitLabelOpt, unitGramsOpt) = {
      val nm = Option(food.name).getOrElse("")
      UnitLookup.get(nm).map { case (lbl, g) => (Some(lbl), Some(g)) }.getOrElse((None, None))
    }
    val hasUnit = unitGramsOpt.isDefined

    def kcalPer100: Int = math.round(Option(food.calories).getOrElse(0.0)).toInt

    val nameLbl = new Label(Option(food.name).getOrElse("Unnamed")) {
      font = Font.font("Segoe UI", FontWeight.Bold, sz(14)); textFill = Color.web("#0D3B2F")
    }
    val kcalLbl = new Label(s"${kcalPer100} kcal") {
      font = Font.font("Segoe UI", FontWeight.SemiBold, sz(11)); textFill = Color.web("#2F6D5F")
    }

    sealed trait Disp; case object Per100g extends Disp; case object PerUnit extends Disp
    var dispMode: Disp = if (hasUnit) PerUnit else Per100g

    def fmt(x: Double, d: Int): String = String.format(s"%1.${d}f", Double.box(Option(x).getOrElse(0.0)))
    def scaleMacros: Double = if (dispMode == Per100g) 1.0 else unitGramsOpt.map(_ / 100.0).getOrElse(1.0)

    def mkStat(s: String) = new Label(s) { font = Font.font("Segoe UI", FontWeight.Bold, sz(12)); textFill = Color.web("#0D3B2F") }
    val capLabel = new Label("") { font = Font.font("Segoe UI", FontWeight.SemiBold, sz(10)); textFill = Color.web("#2F6D5F") }
    val calLbl = mkStat(""); val protLbl = mkStat(""); val carbLbl = mkStat(""); val fatLbl = mkStat(""); val fibLbl = mkStat("")

    val per100Btn = new ToggleButton("100 g") { focusTraversable = false }
    val perUnitBtn = new ToggleButton(unitLabelOpt.map(lbl => s"1 $lbl").getOrElse("1 unit")) {
      focusTraversable = false; disable = !hasUnit
    }
    // smaller chips
    per100Btn.minWidth = sz(52); per100Btn.minHeight = sz(24)
    perUnitBtn.minWidth = sz(52); perUnitBtn.minHeight = sz(24)

    val UnitOnlyNames: Set[String] = Set()
    val nm = Option(food.name).getOrElse("")
    val unitOnly = UnitOnlyNames.exists(_.equalsIgnoreCase(nm))
    val showPer100 = !unitOnly
    val showPerUnit = hasUnit

    val modeGroup = new ToggleGroup()
    per100Btn.toggleGroup = modeGroup
    perUnitBtn.toggleGroup = modeGroup

    if (showPerUnit && !showPer100) { dispMode = PerUnit; perUnitBtn.setSelected(true) }
    else if (!showPerUnit && showPer100) { dispMode = Per100g; per100Btn.setSelected(true) }
    else if (dispMode == PerUnit && showPerUnit) perUnitBtn.setSelected(true)
    else per100Btn.setSelected(true)

    def btnStyle(selected: Boolean, disabled: Boolean): String = {
      val base =
        s"""
           |-fx-background-radius: ${sz(12)},${sz(12)},${sz(12)};
           |-fx-border-radius: ${sz(12)};
           |-fx-background-insets: 0,0,0;
           |-fx-border-insets: 0;
           |-fx-padding: ${sz(3)} ${sz(8)};
           |-fx-font-size: ${sz(11)}px;
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
    def applyBtnStyles(): Unit = {
      per100Btn.style = btnStyle(per100Btn.isSelected, per100Btn.disable.value)
      perUnitBtn.style = btnStyle(perUnitBtn.isSelected, perUnitBtn.disable.value)
    }

    // update top kcal line based on mode
    def updateTopKcal(): Unit = {
      val s = if (dispMode == Per100g) 1.0 else unitGramsOpt.map(_ / 100.0).getOrElse(1.0)
      val kcal = math.round(Option(food.calories).getOrElse(0.0) * s).toInt
      kcalLbl.setText(s"$kcal kcal")
    }

    def updateCaptionAndStats(): Unit = {
      if (dispMode == Per100g) capLabel.setText("Per 100 g")
      else capLabel.setText(s"Per 1 ${unitLabelOpt.get} (${fmt(unitGramsOpt.get,0)} g)")
      val s = scaleMacros
      calLbl.setText(s"Calories: ${math.round(Option(food.calories).getOrElse(0.0) * s).toInt} kcal")
      protLbl.setText(s"Protein:  ${fmt(Option(food.protein).getOrElse(0.0) * s, 1)} g")
      carbLbl.setText(s"Carbs:    ${fmt(Option(food.carbs).getOrElse(0.0)   * s, 1)} g")
      fatLbl .setText(s"Fats:     ${fmt(Option(food.fats).getOrElse(0.0)    * s, 1)} g")
      fibLbl .setText(s"Fiber:    ${fmt(Option(food.fiber).getOrElse(0.0)   * s, 1)} g")
    }
    updateCaptionAndStats(); applyBtnStyles(); updateTopKcal()

    // ensure segmented control behavior + live updates
    per100Btn.selected.onChange((_, _, sel) => {
      if (sel) { dispMode = Per100g; updateCaptionAndStats(); updateTopKcal() }
      applyBtnStyles()
    })
    perUnitBtn.selected.onChange((_, _, sel) => {
      if (sel) { dispMode = PerUnit;  updateCaptionAndStats(); updateTopKcal() }
      applyBtnStyles()
    })

    val selectBtnFront = new Button("Select") {
      maxWidth = Double.MaxValue
      style = s"-fx-font-size: ${sz(12)}px; -fx-background-color: #6DBE75; -fx-text-fill: white; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(6)} ${sz(10)};"
      tooltip = new Tooltip("Add to selection")
    }
    val selectBtnBack = new Button("Select") {
      maxWidth = Double.MaxValue
      style = s"-fx-font-size: ${sz(12)}px; -fx-background-color: #6DBE75; -fx-text-fill: white; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(6)} ${sz(10)};"
      tooltip = new Tooltip("Add to selection")
    }

    var selected = initiallySelected
    var inner: StackPane = null

    def applySelectVisual(): Unit = {
      val text = if (selected) "Selected ✓" else "Select"
      val styleSel = s"-fx-font-size: ${sz(12)}px; -fx-background-color: white; -fx-text-fill: #0D3B2F; -fx-border-color: #6DBE75; -fx-border-radius: ${sz(10)}; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(6)} ${sz(10)};"
      val styleDef = s"-fx-font-size: ${sz(12)}px; -fx-background-color: #6DBE75; -fx-text-fill: white; -fx-background-radius: ${sz(10)}; -fx-padding: ${sz(6)} ${sz(10)};"
      selectBtnFront.text = text; selectBtnBack.text = text
      selectBtnFront.style = if (selected) styleSel else styleDef
      selectBtnBack.style  = if (selected) styleSel else styleDef
      if (inner != null) {
        inner.style =
          (if (selected) s"-fx-border-color: #6DBE75; -fx-border-width: 2;" else "-fx-border-color: #E6F2EC; -fx-border-width: 1;") +
            s" -fx-background-color: white; -fx-background-radius: ${sz(14)}; -fx-border-radius: ${sz(14)}; -fx-padding: ${sz(10)};"
      }
    }
    def toggleSelect(): Unit = { selected = !selected; applySelectVisual(); onToggle(selected) }
    selectBtnFront.onAction = _ => toggleSelect()
    selectBtnBack.onAction  = _ => toggleSelect()

    val front = new VBox {
      alignment = Pos.Center; spacing = sz(6)
      prefHeight = InnerH - 2*InnerPad; minHeight = prefHeight(); maxHeight = prefHeight()
      prefWidth = InnerW - 2*InnerPad;  minWidth = prefWidth();  maxWidth = prefWidth()
      children = Seq(
        imageView,
        new VBox { alignment = Pos.Center; spacing = sz(2); children = Seq(nameLbl, kcalLbl) },
        new Region { prefHeight = sz(2); VBox.setVgrow(this, Priority.Always) },
        selectBtnFront
      )
    }

    val visibleButtons: Seq[Node] = {
      val xs = scala.collection.mutable.ArrayBuffer[Node]()
      if (showPer100) xs += per100Btn
      if (showPerUnit) xs += perUnitBtn
      xs.toSeq
    }
    val segWrap = new HBox { spacing = sz(8); alignment = Pos.Center; children = visibleButtons; prefWidth = InnerW - 2*InnerPad; minWidth = prefWidth(); maxWidth = prefWidth() }
    val showSwitch = visibleButtons.size >= 2
    segWrap.visible = showSwitch; segWrap.managed = showSwitch

    val back = new VBox(sz(6)) {
      alignment = Pos.Center
      padding = Insets(InnerPad, InnerPad, InnerPad, InnerPad)
      prefHeight = InnerH - 2*InnerPad; minHeight = prefHeight(); maxHeight = prefHeight()
      prefWidth = InnerW - 2*InnerPad;  minWidth = prefWidth();  maxWidth = prefWidth()
      children = Seq(segWrap, capLabel, calLbl, protLbl, carbLbl, fatLbl, fibLbl, new Region { VBox.setVgrow(this, Priority.Always) }, selectBtnBack)
    }

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

    root.children = Seq(frame)
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

    def isInside(target: AnyRef, of: scalafx.scene.Node): Boolean = target match {
      case n: JfxNode =>
        var cur: JfxNode = n
        val root: JfxNode = of.delegate
        while (cur != null) { if (cur eq root) return true; cur = cur.getParent }
        false
      case _ => false
    }

    root.focusTraversable = true
    root.onMouseEntered = _ => { root.scaleX = 1.05; root.scaleY = 1.05 }
    root.onMouseExited  = _ => { root.scaleX = 1.0;  root.scaleY = 1.0  }
    root.onMouseClicked = e => {
      val tgt = e.getTarget
      val blockFlip =
        isInside(tgt, selectBtnFront) || isInside(tgt, selectBtnBack) ||
          isInside(tgt, per100Btn)      || isInside(tgt, perUnitBtn)
      if (!blockFlip) flip()
    }
    root.onKeyPressed   = (e: KeyEvent) => if (e.code == KeyCode.Enter) flip()

    root
  }
}
