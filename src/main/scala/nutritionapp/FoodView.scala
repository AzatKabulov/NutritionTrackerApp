package nutritionapp

import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, Region, TilePane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.{Screen, Stage}
import scalafx.Includes._
import nutritionapp.model.{Food, User}
import nutritionapp.component.FoodCard
import java.time.LocalDate

object FoodView {

  private val INK     = "#0D3B2F"
  private val SUBTLE  = "#2F6D5F"
  private val PRIMARY = "#6DBE75"
  private val SURFACE_GRADIENT = "linear-gradient(to bottom right, #F3FBF6, #EAF6EF)"

  private val CardW = 210.0
  private val CardH = 230.0
  private val Gap   = 18.0

  private def safeUI(tag: String)(body: => Unit): Unit =
    try body
    catch {
      case e: Throwable =>
        println(s"[UI ERROR][$tag] ${e.getClass.getSimpleName}: ${e.getMessage}")
        e.printStackTrace()
    }

  private val SortItems: Seq[String] = Seq(
    "Name (A→Z)", "Name (Z→A)",
    "Protein (High→Low)", "Protein (Low→High)",
    "Calories (High→Low)", "Calories (Low→High)",
    "Carbs (High→Low)", "Carbs (Low→High)",
    "Fats (High→Low)", "Fats (Low→High)",
    "Fiber (High→Low)", "Fiber (Low→High)"
  )

  private def metricOf(label: String): String = {
    val l = label.toLowerCase
    if (l.startsWith("name")) "name"
    else if (l.startsWith("protein")) "protein"
    else if (l.startsWith("calories")) "calories"
    else if (l.startsWith("carbs")) "carbs"
    else if (l.startsWith("fats")) "fats"
    else "fiber"
  }
  private def isDescending(label: String): Boolean = label.contains("Z→A") || label.contains("High→Low")
  private def toggleVariant(label: String): String = metricOf(label) match {
    case "name"     => if (label.contains("A→Z")) "Name (Z→A)" else "Name (A→Z)"
    case "protein"  => if (label.contains("High→Low")) "Protein (Low→High)" else "Protein (High→Low)"
    case "calories" => if (label.contains("High→Low")) "Calories (Low→High)" else "Calories (High→Low)"
    case "carbs"    => if (label.contains("High→Low")) "Carbs (Low→High)" else "Carbs (High→Low)"
    case "fats"     => if (label.contains("High→Low")) "Fats (Low→High)" else "Fats (High→Low)"
    case _          => if (label.contains("High→Low")) "Fiber (Low→High)" else "Fiber (High→Low)"
  }

  // ----- public -----
  def show(stage: Stage, user: User): Unit = {
    val rootNode = create(stage, user)
    stage.scene = new Scene(rootNode)

    val vb = Screen.primary.visualBounds
    stage.fullScreen = false
    stage.resizable = true
    stage.x = vb.minX; stage.y = vb.minY
    stage.width = vb.width; stage.height = vb.height
    stage.maximized = true

    Platform.runLater {
      stage.maximized = true
      stage.width = vb.width; stage.height = vb.height
      stage.centerOnScreen()
    }
  }

  def create(stage: Stage, user: User): VBox = {
    val allFoods: List[Food] = FoodDatabase.loadFoods()
    var currentCategory: Option[String] = None

    val backBtn = new Button("⬅ Back to Dashboard") {
      style =
        s"""
           |-fx-background-radius: 10;
           |-fx-background-color: rgba(109,190,117,0.12);
           |-fx-text-fill: $INK;
           |-fx-font-weight: bold;
           |-fx-padding: 8 14 8 14;
           |-fx-border-color: rgba(109,190,117,0.35);
           |-fx-border-radius: 10;
           """.stripMargin
      onAction = _ => DashboardView.show(stage, user)
      focusTraversable = false
    }

    // ---------- SEARCH ----------
    val searchField = new TextField {
      promptText = "Search by name…"
      style = "-fx-background-color: transparent; -fx-font-size: 13px;"
      focusTraversable = false
    }
    searchField.prefWidth = 420
    searchField.maxWidth  = 420

    val searchIconView = new ImageView(
      Option(getClass.getResource("/images/search.png"))
        .map(u => new Image(u.toExternalForm))
        .getOrElse(new Image("https://via.placeholder.com/16"))
    ) { fitWidth = 16; fitHeight = 16; preserveRatio = true; smooth = true }

    val searchCapsule = new HBox(8, searchIconView, searchField) {
      alignment = Pos.CenterLeft
      padding = Insets(10, 12, 10, 12)
      style =
        """-fx-background-color: white;
          |-fx-background-radius: 12;
          |-fx-border-color: #E6F2EC;
          |-fx-border-radius: 12;""".stripMargin
      maxWidth = 460
    }

    // ---------- SORT ----------
    val sortLabel = new Label("Sort:") {
      textFill = Color.web(SUBTLE)
      font = Font.font("Segoe UI", FontWeight.SemiBold, 13)
    }
    val sortBox = new ComboBox[String](SortItems) {
      value = "Name (A→Z)"
      maxWidth = 220
      style =
        """
          |-fx-background-radius: 10;
          |-fx-padding: 6 10 6 10;
          |-fx-background-color: white;
          |-fx-border-color: #E6F2EC;
          |-fx-border-radius: 10;
          """.stripMargin
    }
    var lastClicked: String = sortBox.value.value

    val topSearchRow = new HBox {
      spacing = 12
      alignment = Pos.CenterLeft
      children = Seq(
        searchCapsule,
        new Region { hgrow = Priority.Always },
        new HBox(8, sortLabel, sortBox) { alignment = Pos.CenterRight }
      )
    }

    // ---------- GRID ----------
    val grid = new TilePane {
      hgap = Gap; vgap = Gap; padding = Insets(8)
      prefColumns = 5
      prefTileWidth = CardW; prefTileHeight = CardH
      tileAlignment = Pos.TopLeft
      prefWidth = Region.USE_COMPUTED_SIZE
      maxWidth  = Double.MaxValue
    }

    // ---- ADD HANDLER (callback ONLY to avoid duplicates) ----
    def handleAddToPlanner(f: Food, meal: String): Unit = {
      val date = java.time.LocalDate.now() // or your selected date if you have one
      PlannerView.addFoodToMeal(user, date, meal, f) // <-- pass user explicitly
      new Alert(Alert.AlertType.Information) {
        title = "Add to Planner"
        headerText = None
        contentText = s"Added '${Option(f.name).getOrElse("item")}' to $meal."
      }.showAndWait()
    }

    def cardNode(food: Food): Region = {
      val c = new nutritionapp.component.FoodCard(
        food,
        onAddToPlanner = (ff, slot) => handleAddToPlanner(ff, slot)
      )
      // IMPORTANT: do NOT also add a custom event handler here — that caused the double popup.
      c.prefWidth = CardW;
      c.minWidth = CardW;
      c.maxWidth = CardW
      c.prefHeight = CardH;
      c.minHeight = CardH;
      c.maxHeight = CardH
      c
    }


    def safeLowerTrim(s: String): String = Option(s).map(_.trim.toLowerCase).getOrElse("")

    def refreshGrid(): Unit = safeUI("refreshGrid") {
      val q = safeLowerTrim(searchField.text.value)

      val filtered = allFoods.filter { f =>
        val cat  = Option(f.category).getOrElse("")
        val name = Option(f.name).getOrElse("")
        val matchesCat    = currentCategory.forall(cat.equalsIgnoreCase)
        val matchesSearch = q.isEmpty || name.toLowerCase.contains(q)
        matchesCat && matchesSearch
      }

      val sel = sortBox.value.value
      val metric = metricOf(sel)
      val desc   = isDescending(sel)

      val sorted: List[Food] = metric match {
        case "name"     => val by = filtered.sortBy(f => Option(f.name).getOrElse("").toLowerCase); if (desc) by.reverse else by
        case "protein"  => val by = filtered.sortBy(f => Option(f.protein).getOrElse(0.0));       if (desc) by.reverse else by
        case "calories" => val by = filtered.sortBy(f => Option(f.calories).getOrElse(0.0));      if (desc) by.reverse else by
        case "carbs"    => val by = filtered.sortBy(f => Option(f.carbs).getOrElse(0.0));         if (desc) by.reverse else by
        case "fats"     => val by = filtered.sortBy(f => Option(f.fats).getOrElse(0.0));          if (desc) by.reverse else by
        case _          => val by = filtered.sortBy(f => Option(f.fiber).getOrElse(0.0));         if (desc) by.reverse else by
      }

      grid.children.clear()
      sorted.foreach(food => grid.children += cardNode(food))
    }

    // sort + search handlers
    sortBox.onAction = _ => safeUI("sortToggle") {
      val clicked = sortBox.selectionModel().getSelectedItem
      if (clicked == lastClicked) {
        val toggled = toggleVariant(clicked)
        sortBox.value = toggled
        lastClicked = toggled
      } else {
        lastClicked = clicked
      }
      refreshGrid()
    }
    sortBox.value.onChange((_, _, _) => safeUI("sortChange")(refreshGrid()))
    searchField.text.onChange((_, _, _) => safeUI("searchChange")(refreshGrid()))

    // ---------- Category pills ----------
    val pillGroup = new ToggleGroup()
    case class PillDef(label: String, cat: Option[String], accent: String)
    val pills = Seq(
      PillDef("All",           None,                  PRIMARY),
      PillDef("Fruits",        Some("Fruits"),        "#FFC9DE"),
      PillDef("Vegetables",    Some("Vegetables"),    "#B7F2C1"),
      PillDef("Protein",       Some("Protein"),       "#F3A5A5"),
      PillDef("Carbs",         Some("Carbohydrates"), "#FFEB99"),
      PillDef("Fats",          Some("Fats"),          "#FFC07A"),
      PillDef("Dairy",         Some("Dairy"),         "#BFDFFF")
    )

    def pill(defn: PillDef): ToggleButton = {
      val sel =
        s"""
           |-fx-background-radius: 12;
           |-fx-padding: 8 14 8 14;
           |-fx-font-size: 13px;
           |-fx-background-color: ${defn.accent};
           |-fx-text-fill: #0D3B2F;
           |-fx-border-color: transparent;
           |-fx-border-radius: 12;
           """.stripMargin
      val unsel =
        s"""
           |-fx-background-radius: 12;
           |-fx-padding: 8 14 8 14;
           |-fx-font-size: 13px;
           |-fx-background-color: white;
           |-fx-text-fill: #2F6D5F;
           |-fx-border-color: #E6F2EC;
           |-fx-border-radius: 12;
           """.stripMargin

      new ToggleButton(defn.label) {
        toggleGroup = pillGroup
        focusTraversable = false
        style = unsel
        selected.onChange((_, _, _) => style = if (selected.value) sel else unsel)
        onAction = _ => safeUI(s"pill:${defn.label}") {
          currentCategory = defn.cat
          refreshGrid()
        }
      }
    }

    val categoryBar = new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = pills.map(pill)
    }

    // first render
    pillGroup.getToggles.get(0).setSelected(true)
    refreshGrid()

    // responsive columns
    def recalcColumns(totalWidth: Double): Unit = {
      val contentW = math.max(600.0, totalWidth - 22 - 22 - 16)
      val perTile  = CardW + Gap
      val cols     = math.max(1, (contentW / perTile).toInt)
      grid.prefColumns = cols
    }
    recalcColumns(stage.width.value)
    stage.width.onChange((_, _, nw) => recalcColumns(nw.doubleValue()))

    new VBox {
      spacing = 14
      padding = Insets(18, 22, 22, 22)
      fillWidth = true
      style = s"-fx-background-color: $SURFACE_GRADIENT;"
      children = Seq(
        new HBox(backBtn) { alignment = Pos.CenterLeft },
        topSearchRow,
        categoryBar,
        new ScrollPane {
          content = grid
          fitToWidth = true
          fitToHeight = true
          hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
          vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
          style = "-fx-background-color: transparent;"
          VBox.setVgrow(this, Priority.Always)
        }
      )
    }
  }
}
