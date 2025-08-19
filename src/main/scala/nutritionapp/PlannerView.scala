package nutritionapp

import nutritionapp.model._
import nutritionapp.dialog.{AddFoodDialog, AddMealDialog}

import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text.{Font, FontWeight}
import scalafx.scene.paint.Color
import scalafx.geometry._
import scalafx.stage.{Stage, Modality}
import scalafx.Includes._
import scalafx.scene.canvas.Canvas
import scalafx.scene.shape.ArcType
import scalafx.scene.Scene

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.nio.file.{Files, Paths, Path}
import java.nio.charset.StandardCharsets

import nutritionapp.AuthManager
import ujson._

object PlannerView {

  private val INK         = "#0D3B2F"
  private val SUBTLE_INK  = "#2F6D5F"
  private val PRIMARY     = "#6DBE75"
  private val AMBER       = "#FFC857"
  private val BG_GRADIENT = "-fx-background-color: linear-gradient(to bottom right, #F3FBF6, #EAF6EF);"

  // (Old ratio constants kept for reference only)
  private val RATIO_P = 0.30
  private val RATIO_C = 0.50
  private val RATIO_F = 0.20

  private var currentDate: LocalDate = LocalDate.now()
  private val dateLabel  = new Label()
  private val datePicker = new DatePicker(currentDate)

  private val leftColumn  = new VBox(16)
  private val rightColumn = new VBox(16) { alignment = Pos.TopCenter; maxWidth = 800 }
  private val totalLabel  = new Label()

  private var saveNoteBtn: Button    = new Button("Save note")
  private var noteTextArea: TextArea = new TextArea()

  private var currentUser: User = _

  private case class Row(name: String, kcal: Double, p: Double, c: Double, f: Double)

  def show(stage: Stage, user: User): Unit =
    Nav.go(stage, create(stage, user))

  def create(stage: Stage, user: User): VBox = {
    currentUser = user
    MealPlanner.setCurrentUser(user.email)

    val header: Region = buildHeader(stage)
    val grid           = buildTwoColumnGrid()

    val scroller = new ScrollPane {
      content = grid
      fitToWidth  = true
      fitToHeight = true
      hbarPolicy  = ScrollPane.ScrollBarPolicy.NEVER
      vbarPolicy  = ScrollPane.ScrollBarPolicy.AS_NEEDED
      style = "-fx-background-color: transparent;"
      VBox.setVgrow(this, Priority.Always)
    }

    val root = new VBox(18) {
      padding   = Insets(16, 16, 8, 16)
      alignment = Pos.TopCenter
      style     = BG_GRADIENT
      children  = Seq(header, scroller, stickyTotalBar(totalLabel))
    }

    refreshPlanner(currentDate, stage)
    EventBus.onPlannerUpdated = () => refreshPlanner(currentDate, stage)
    root
  }

  // ---------- HEADER ----------
  private def buildHeader(stage: Stage): Region = {
    val backBtn = new Button("⬅ Back to Dashboard") {
      style =
        s"""
          -fx-background-radius: 10;
          -fx-background-color: rgba(109,190,117,0.12);
          -fx-text-fill: $INK;
          -fx-font-weight: bold;
          -fx-padding: 8 14 8 14;
          -fx-border-color: rgba(109,190,117,0.35);
          -fx-border-radius: 10;
        """
      onAction = _ => DashboardView.show(stage, currentUser)
    }

    def chip(txt: String) = new Button(txt) {
      style =
        s"""
           -fx-background-color: white;
           -fx-text-fill: $SUBTLE_INK;
           -fx-border-color: rgba(0,0,0,0.08);
           -fx-background-radius: 10; -fx-border-radius: 10;
           -fx-padding: 6 10 6 10; -fx-font-size: 12;
         """
    }

    val prevButton = chip("◀")
    val nextButton = chip("▶")
    val todayLbl   = new Label("Today") { textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 12;" }
    val todayBtn   = chip("Today")

    prevButton.onAction = _ => refreshPlanner(currentDate.minusDays(1), stage)
    nextButton.onAction = _ => refreshPlanner(currentDate.plusDays(1), stage)
    todayBtn.onAction   = _ => refreshPlanner(LocalDate.now(), stage)

    datePicker.onAction = _ => {
      val selected = datePicker.value.value
      if (selected != null) refreshPlanner(selected, stage)
    }

    dateLabel.font = Font.font("Segoe UI", FontWeight.SemiBold, 16)
    val dateNavBar = new HBox(8, prevButton, dateLabel, nextButton, datePicker, todayLbl, todayBtn) {
      alignment = Pos.Center
    }

    new BorderPane {
      padding = Insets(0, 0, 6, 0)
      left   = new HBox { alignment = Pos.CenterLeft; children = backBtn }
      center = dateNavBar
    }
  }

  // ---------- REFRESH ----------
  private def refreshPlanner(date: LocalDate, stage: Stage): Unit = {
    MealPlanner.setCurrentUser(currentUser.email)

    currentDate      = date
    dateLabel.text   = formatDate(date)
    datePicker.value = date

    MealPlanner.loadFromFileForDate(date)
    val items   = MealPlanner.getItemsForDate(date)
    val grouped = items.groupBy(_.mealType)

    val totalK = items.map(_.calories).sum
    val totalP = items.map(_.protein).sum
    val totalC = items.map(_.carbs).sum
    val totalF = items.map(_.fats).sum

    val advice     = estimateAdvice(currentUser)
    val suggestedK = advice.targetKcal
    val targetKcal = currentUser.targetCalories.getOrElse(suggestedK)
    val remaining  = math.max(0.0, targetKcal - totalK)
    val donutFrac  = clamp(totalK / math.max(1.0, targetKcal))

    // --- Goal-aware macro targets (protein/fat per-kg; carbs fill remainder) ---
    val (pTarget, cTarget, fTarget) = macroTargetsForGoal(targetKcal, currentUser)

    leftColumn.children.clear()
    leftColumn.children ++= Seq(
      caloriesCard(
        remaining  = remaining,
        frac       = donutFrac,
        targetKcal = targetKcal.toInt,
        advice     = advice,
        onSetTarget = (newTarget: Int) => {
          val clamped = clampKcal(newTarget)
          val updated = currentUser.copy(targetCalories = Some(clamped))
          if (saveUser(updated)) currentUser = updated
          refreshPlanner(currentDate, stage)
        }
      ),
      macroBreakdownCard(totalP, totalC, totalF, pTarget, cTarget, fTarget),
      notesCard(currentDate)
    )

    rightColumn.children.clear()
    val mealOrder = Seq("Breakfast", "Lunch", "Dinner", "Snack")
    mealOrder.foreach { meal =>
      val mealItems = grouped.getOrElse(meal, Seq())
      val rows      = mealItems.map(i => Row(i.name, i.calories, i.protein, i.carbs, i.fats))
      val kcal      = rows.map(_.kcal).sum
      val pSum      = rows.map(_.p).sum
      val cSum      = rows.map(_.c).sum
      val fSum      = rows.map(_.f).sum

      rightColumn.children += mealCard(
        title = meal,
        rows  = rows,
        totals = (kcal, pSum, cSum, fSum),
        onRemoveIndex = (idx: Int) => {
          val orig = mealItems(idx)
          MealPlanner.removeItemForDate(currentDate, orig)
          MealPlanner.saveToFileForDate(currentDate)
          refreshPlanner(currentDate, stage)
        },
        onAddFood = () => showAddFoodDialog(meal, stage),
        onAddMeal = () => showAddMealDialog(meal, stage)
      )
    }

    totalLabel.text = f"Daily Total: $totalK%.0f kcal  •  $totalP%.1f P  •  $totalC%.1f C  •  $totalF%.1f F"
  }

  // ---------- GRID ----------
  private def buildTwoColumnGrid(): GridPane = {
    val grid = new GridPane {
      hgap = 18; vgap = 18
      padding   = Insets(8, 0, 16, 0)
      alignment = Pos.TopCenter
      columnConstraints = Seq(
        new ColumnConstraints { prefWidth = 600; minWidth = 520; maxWidth = 700 },
        new ColumnConstraints { prefWidth = 820; minWidth = 600; maxWidth = 1000; hgrow = Priority.Never }
      )
    }
    grid.add(leftColumn, 0, 0)
    grid.add(rightColumn, 1, 0)
    grid
  }

  // ---------- CARD BUILDERS ----------
  private def card(title: String = "", bg: String = "white")(content: => Region): StackPane = {
    val titleNode =
      if (title.isEmpty) new Region
      else new Label(title) {
        font = Font.font("Segoe UI", FontWeight.Bold, 16)
        textFill = Color.web(INK)
      }

    val box = new VBox(12) {
      padding = Insets(18)
      children = if (title.isEmpty) Seq(content) else Seq(titleNode, content)
    }

    new StackPane {
      style =
        s"""-fx-background-color: $bg;
           |-fx-background-radius: 16;
           |-fx-border-radius: 16;
           |-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);""".stripMargin
      children = box
      maxWidth = 600
    }
  }

  private def chipStyle(bgAlpha: Double = 0.12, borderAlpha: Double = 0.35): String =
    s"-fx-background-color: rgba(109,190,117,$bgAlpha);" +
      s"-fx-text-fill: $SUBTLE_INK;" +
      s"-fx-background-radius: 12;" +
      s"-fx-padding: 6 12;" +
      s"-fx-border-color: rgba(109,190,117,$borderAlpha);" +
      s"-fx-border-radius: 12;"

  private def caloriesCard(
                            remaining: Double,
                            frac: Double,
                            targetKcal: Int,
                            advice: CalorieEstimator.Advice,
                            onSetTarget: Int => Unit
                          ): StackPane = card() {
    val donut = donutRing(frac, 104.0, PRIMARY)

    val bigNumber = new Label(f"${remaining}%.0f kcal") {
      font = Font.font("Segoe UI", FontWeight.Bold, 30)
      textFill = Color.web(INK)
    }

    val sub = new Label(s"remaining (goal $targetKcal kcal)") {
      textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 12;"
    }

    val sug10 = CalorieEstimator.roundUp10(advice.targetKcal)
    val suggestion = new Label(s"Suggested: ${sug10} kcal") {
      textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 12;"; wrapText = true
    }

    def chip(text: String, delta: Int) = new Button(text) {
      style = chipStyle()
      onAction = _ => onSetTarget(targetKcal + delta)
    }
    val stepper = new HBox(8, chip("–100", -100), chip("+100", +100)) { alignment = Pos.CenterLeft }

    val headline = new HBox(12, bigNumber, stepper) { alignment = Pos.CenterLeft }
    val left     = new VBox(4, headline, sub, suggestion)

    val topRow = new HBox(16,
      new StackPane { prefWidth = 104; prefHeight = 104; children = donut },
      left
    ) { alignment = Pos.CenterLeft }

    val tip = new Label("Tip: Click Add on a meal to record foods") {
      textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 11;"
    }

    new VBox(12, topRow, tip)
  }

  private def macroBreakdownCard(p: Double, c: Double, f: Double,
                                 pTarget: Double, cTarget: Double, fTarget: Double): StackPane =
    card("Nutrition breakdown") {
      val pRow = macroRow("Protein", p, pTarget)
      val cRow = macroRow("Carbs",   c, cTarget)
      val fRow = macroRow("Fats",    f, fTarget)
      new VBox(10, pRow, cRow, fRow)
    }

  private def macroRow(label: String, grams: Double, target: Double): VBox = {
    val head = new HBox {
      alignment = Pos.CenterLeft; spacing = 8
      children = Seq(
        new Label(label) { textFill = Color.web(INK); style = "-fx-font-size: 13; -fx-font-weight: 600;" },
        new Region { HBox.setHgrow(this, Priority.Always) },
        new Label(f"${grams}%.1f g / ${target}%.0f g") { textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 13;" }
      )
    }
    val bar = new ProgressBar {
      progress = clamp(grams / math.max(1.0, target))
      maxWidth = Double.MaxValue
      style = s"-fx-accent: $PRIMARY; -fx-background-insets: 0; -fx-background-radius: 8; -fx-padding: 3;"
    }
    new VBox(4, head, bar)
  }

  // ---------- NOTES ----------
  private def notePath(date: LocalDate): Path = {
    val base = MealPlanner.userNotesDir.toString
    val p = Paths.get(base)
    if (!Files.exists(p)) Files.createDirectories(p)
    p.resolve(s"${date.toString}.txt")
  }

  private def loadNote(date: LocalDate): String = {
    try {
      val p = notePath(date)
      if (Files.exists(p)) new String(Files.readAllBytes(p), StandardCharsets.UTF_8) else ""
    } catch { case _: Throwable => "" }
  }

  private def saveNote(date: LocalDate, text: String): Unit = {
    try {
      val p = notePath(date)
      Files.write(p, text.getBytes(StandardCharsets.UTF_8))
    } catch { case _: Throwable => () }
  }

  private def resetSaveBtn(): Unit = {
    saveNoteBtn.text    = "Save note"
    saveNoteBtn.disable = false
    saveNoteBtn.style   = s"-fx-background-color: $AMBER; -fx-text-fill: $INK; -fx-background-radius: 10; -fx-padding: 8 14 8 14;"
  }

  private def setSavedBtn(): Unit = {
    saveNoteBtn.text  = "Saved ✓"
    saveNoteBtn.style = s"-fx-background-color: $PRIMARY; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 14 8 14;"
  }

  private def notesCard(date: LocalDate): StackPane = {
    noteTextArea = new TextArea {
      promptText   = "Notes for the day…"
      prefRowCount = 6
      wrapText     = true
      text         = loadNote(date)
      style        = "-fx-font-size: 13;"
    }
    saveNoteBtn = new Button("Save note")
    resetSaveBtn()
    noteTextArea.text.onChange { (_, _, _) => resetSaveBtn() }
    saveNoteBtn.onAction = _ => { saveNote(date, noteTextArea.text.value); setSavedBtn() }

    card("Notes", bg = "#FFF7D6") {
      new VBox(10,
        noteTextArea,
        new HBox { alignment = Pos.CenterRight; children = Seq(saveNoteBtn) }
      )
    }
  }

  private def mealCard(
                        title: String,
                        rows: Seq[Row],
                        totals: (Double, Double, Double, Double),
                        onRemoveIndex: Int => Unit,
                        onAddFood: () => Unit,
                        onAddMeal: () => Unit
                      ): StackPane = {
    val (kcal, pSum, cSum, fSum) = totals

    val header = new HBox {
      alignment = Pos.CenterLeft; spacing = 10
      children = Seq(
        new Label(s"🍽  $title") {
          font = Font.font("Segoe UI", FontWeight.Bold, 18)
          textFill = Color.web(INK)
        },
        new Region { HBox.setHgrow(this, Priority.Always) },
        new Label(f"$kcal%.0f kcal • $pSum%.0fP • $cSum%.0fC • $fSum%.0fF") {
          textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 12;"
        }
      )
    }

    val body: Region =
      if (rows.isEmpty) new Label("No items yet — add your first food") {
        textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 12;"
      }
      else new VBox(8, rows.zipWithIndex.map { case (row, idx) =>
        new HBox(8) {
          alignment = Pos.CenterLeft
          children = Seq(
            new Label(row.name) { style = "-fx-font-size: 13;" },
            new Region { HBox.setHgrow(this, Priority.Always) },
            new Label(f"${row.kcal}%.0f kcal • ${row.p}%.0fP • ${row.c}%.0fC • ${row.f}%.0fF") {
              textFill = Color.web(SUBTLE_INK); style = "-fx-font-size: 12;"
            },
            new Button("🗑") {
              tooltip = new Tooltip("Remove item")
              style = "-fx-background-color: #ffefef; -fx-border-color: #e07a7a; -fx-background-radius: 8; -fx-border-radius: 8;"
              onAction = _ => onRemoveIndex(idx)
            }
          )
        }
      }:_*)

    val actions = new HBox(10,
      new Button("＋ Add Food") {
        style = s"-fx-background-color: $PRIMARY; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 14 8 14;"
        onAction = _ => onAddFood()
      },
      new Button("＋ Add Meal") {
        style = s"-fx-background-color: $AMBER; -fx-text-fill: $INK; -fx-background-radius: 10; -fx-padding: 8 12 8 12;"
        onAction = _ => onAddMeal()
      }
    ) { alignment = Pos.CenterLeft }

    val c = card() { new VBox(12, header, body, actions) }
    c.maxWidth = 800
    c
  }

  private def stickyTotalBar(label: Label): StackPane = {
    label.textFill = Color.web(SUBTLE_INK)
    label.style = "-fx-font-size: 12;"
    new StackPane { padding = Insets(8, 6, 4, 6); children = label }
  }

  private def donutRing(frac: Double, size: Double, colorHex: String): Canvas = {
    val canvas = new Canvas(size, size)
    val g = canvas.graphicsContext2D
    val cx = size / 2; val cy = size / 2; val r = size / 2 - 6
    g.setLineWidth(10)
    g.setStroke(Color.rgb(0, 0, 0, 0.10))
    g.strokeArc(cx - r, cy - r, r * 2, r * 2, 90, -360, ArcType.Open)
    g.setStroke(Color.web(colorHex))
    val sweep = -360 * clamp(frac)
    g.strokeArc(cx - r, cy - r, r * 2, r * 2, 90, sweep, ArcType.Open)
    canvas
  }

  // ---------- HELPERS ----------
  private def formatDate(date: LocalDate): String = {
    val today     = LocalDate.now()
    val yesterday = today.minusDays(1)
    val tomorrow  = today.plusDays(1)
    if (date == today) "Today"
    else if (date == yesterday) "Yesterday"
    else if (date == tomorrow) "Tomorrow"
    else date.getDayOfWeek.toString.capitalize + ", " + date.format(DateTimeFormatter.ofPattern("MMM d"))
  }

  private def clamp(x: Double): Double = math.max(0.0, math.min(1.0, x))
  private def clampKcal(x: Int): Int    = math.max(900, math.min(6000, x))

  private def estimateAdvice(u: User): CalorieEstimator.Advice = {
    val in = CalorieEstimator.fromUser(u, sexOpt = None, pace = CalorieEstimator.ModerateP)
    CalorieEstimator.recommend(in)
  }

  // --- Goal-aware macro targets (evidence-based) ---
  // Protein & fat are set per-kg using goal-specific multipliers; carbs fill the remainder.
  // Floors ensure reasonable minimums if the calorie goal is very low.
  private def macroTargetsForGoal(targetKcal: Int, user: User): (Double, Double, Double) = {
    import nutritionapp.model.CalorieEstimator._

    val weight = math.max(30.0, user.weight) // simple sanity floor

    // Defaults (typical evidence-based ranges):
    // Lose: protein ~1.8 g/kg, fat ~0.8 g/kg
    // Maintain: protein ~1.6 g/kg, fat ~0.8 g/kg
    // Gain: protein ~1.6 g/kg, fat ~0.9 g/kg
    val (protPerKg, fatPerKg) = parseGoal(user.goal) match {
      case Lose     => (1.8, 0.8)
      case Maintain => (1.6, 0.8)
      case Gain     => (1.6, 0.9)
    }

    val minProtPerKg = 1.2
    val minFatPerKg  = 0.6

    def kcalOf(p: Double, f: Double): Double = p * 4 + f * 9

    var pG = weight * protPerKg
    var fG = weight * fatPerKg

    // If protein+fat alone exceed the calorie goal, push toward floors safely
    if (kcalOf(pG, fG) > targetKcal) {
      val fFloor = weight * minFatPerKg
      if (kcalOf(pG, fFloor) <= targetKcal) {
        fG = fFloor
      } else {
        fG = fFloor
        val pFloor = weight * minProtPerKg
        if (kcalOf(pFloor, fG) <= targetKcal) {
          pG = pFloor
        } else {
          // last resort: proportionally scale protein (keep fat at floor)
          val scale = targetKcal / kcalOf(pFloor, fG)
          pG = pFloor * math.max(0.0, scale)
        }
      }
    }

    val remain = math.max(0.0, targetKcal - kcalOf(pG, fG))
    val cG = remain / 4.0
    (pG, cG, fG)
  }

  private def saveUser(updated: User): Boolean = {
    try {
      val users    = AuthManager.loadUsers()
      val replaced = users.map(u => if (u.email == updated.email) updated else u)

      def toJson(u: User): Obj = Obj(
        "name"           -> u.name,
        "email"          -> u.email,
        "password"       -> u.password,
        "age"            -> u.age,
        "height"         -> u.height,
        "activityLevel"  -> u.activityLevel,
        "goal"           -> u.goal,
        "weight"         -> u.weight,
        "targetCalories" -> (u.targetCalories match {
          case Some(v) => Num(v)
          case None    => Null
        })
      )

      val out  = Arr(replaced.map(toJson): _*)
      val path = AuthManager.usersFilePath
      val pw   = new java.io.PrintWriter(path, "UTF-8")
      try pw.write(write(out, indent = 2)) finally pw.close()
      true
    } catch { case _: Throwable => false }
  }

  // ---------- DIALOGS ----------
  private def showAddFoodDialog(mealType: String, parentStage: Stage): Unit = {
    val dlg = new Stage {
      initOwner(parentStage); initModality(Modality.ApplicationModal)
      title = s"Add Food — $mealType"
      scene = new Scene(AddFoodDialog.create(mealType, currentDate, parentStage, currentUser), 900, 640)
    }
    dlg.setOnHidden(_ => { MealPlanner.saveToFileForDate(currentDate); refreshPlanner(currentDate, parentStage) })
    dlg.show()
  }

  private def showAddMealDialog(mealType: String, parentStage: Stage): Unit = {
    val dlg = new Stage {
      initOwner(parentStage); initModality(Modality.ApplicationModal)
      title = s"Add Meal — $mealType"
      scene = new Scene(AddMealDialog.create(mealType, currentDate, parentStage, currentUser), 900, 640)
    }
    dlg.setOnHidden(_ => { MealPlanner.saveToFileForDate(currentDate); refreshPlanner(currentDate, parentStage) })
    dlg.show()
  }

  // ---------- PUBLIC: used by FoodView ----------
  def addFoodToMeal(user: User, date: LocalDate, mealType: String, food: Food): Unit = {
    try {
      MealPlanner.setCurrentUser(user.email)
      MealPlanner.loadFromFileForDate(date)

      val entry = PlannerItem(
        name     = food.name,
        source   = Option(food.category).getOrElse("Food"),
        calories = food.calories,
        protein  = food.protein,
        carbs    = food.carbs,
        fats     = food.fats,
        mealType = mealType
      )

      MealPlanner.addItemForDate(date, entry)
      MealPlanner.saveToFileForDate(date)
      try { if (EventBus.onPlannerUpdated != null) EventBus.onPlannerUpdated() } catch { case _: Throwable => () }
    } catch { case e: Throwable => e.printStackTrace() }
  }

  def addFoodToMeal(date: LocalDate, mealType: String, food: Food): Unit = {
    if (currentUser != null) addFoodToMeal(currentUser, date, mealType, food)
    else System.err.println("[PlannerView] No current user set; call addFoodToMeal(user, date, mealType, food) instead.")
  }

  // ---------- PUBLIC: used by MealView ----------
  def addMealToMeal(user: User, date: LocalDate, mealType: String, meal: Meal): Unit = {
    try {
      MealPlanner.setCurrentUser(user.email)
      MealPlanner.loadFromFileForDate(date)

      val entry = PlannerItem(
        name     = meal.name,
        source   = Option(meal.category).getOrElse("Meal"),
        calories = meal.calories,
        protein  = meal.protein,
        carbs    = meal.carbs,
        fats     = meal.fats,
        mealType = mealType
      )

      MealPlanner.addItemForDate(date, entry)
      MealPlanner.saveToFileForDate(date)
      try { if (EventBus.onPlannerUpdated != null) EventBus.onPlannerUpdated() } catch { case _: Throwable => () }
    } catch { case e: Throwable => e.printStackTrace() }
  }

  def addMealToMeal(date: LocalDate, mealType: String, meal: Meal): Unit = {
    if (currentUser != null) addMealToMeal(currentUser, date, mealType, meal)
    else System.err.println("[PlannerView] No current user set; call addMealToMeal(user, date, mealType, meal) instead.")
  }
}
