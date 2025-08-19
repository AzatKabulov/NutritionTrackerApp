package nutritionapp

import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, Region, TilePane, VBox, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.{Screen, Stage}
import scalafx.Includes._
import nutritionapp.model.{Meal, User}
import nutritionapp.component.MealCard
import java.time.LocalDate

object MealView {

  private val INK     = "#0D3B2F"
  private val SUBTLE  = "#2F6D5F"
  private val PRIMARY = "#6DBE75"
  private val SURFACE_GRADIENT = "linear-gradient(to bottom right, #F3FBF6, #EAF6EF)"

  private val CardW = 210.0
  private val CardH = 230.0
  private val Gap   = 18.0

  private def safeUI(tag: String)(body: => Unit): Unit =
    try body catch { case e: Throwable => println(s"[UI ERROR][$tag] ${e.getClass.getSimpleName}: ${e.getMessage}"); e.printStackTrace() }

  private val SortItems: Seq[String] = Seq(
    "Name (A→Z)", "Name (Z→A)",
    "Protein (High→Low)", "Protein (Low→High)",
    "Calories (High→Low)", "Calories (Low→High)",
    "Carbs (High→Low)", "Carbs (Low→High)",
    "Fats (High→Low)", "Fats (Low→High)"
  )

  private def metricOf(label: String): String = {
    val l = label.toLowerCase
    if (l.startsWith("name")) "name"
    else if (l.startsWith("protein")) "protein"
    else if (l.startsWith("calories")) "calories"
    else if (l.startsWith("carbs")) "carbs"
    else "fats"
  }
  private def isDescending(label: String): Boolean = label.contains("Z→A") || label.contains("High→Low")
  private def toggleVariant(label: String): String = metricOf(label) match {
    case "name"     => if (label.contains("A→Z")) "Name (Z→A)" else "Name (A→Z)"
    case "protein"  => if (label.contains("High→Low")) "Protein (Low→High)" else "Protein (High→Low)"
    case "calories" => if (label.contains("High→Low")) "Calories (Low→High)" else "Calories (High→Low)"
    case "carbs"    => if (label.contains("High→Low")) "Carbs (Low→High)" else "Carbs (High→Low)"
    case _          => if (label.contains("High→Low")) "Fats (Low→High)" else "Fats (High→Low)"
  }

  // Public show (matches FoodView)
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
    stage.fullScreen = false
    stage.maximized = true

    MealDatabase.load()
    val allMeals: List[Meal] = MealDatabase.getMeals

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
      promptText = "Search by name..."
      maxWidth = 380
      style =
        """
          |-fx-background-radius: 12;
          |-fx-padding: 10 12 10 36; /* left padding for icon */
          |-fx-font-size: 13px;
          |-fx-background-color: white;
          |-fx-border-color: #E6F2EC;
          |-fx-border-radius: 12;
          """.stripMargin
      focusTraversable = false
    }
    val searchIcon = Option(getClass.getResource("/images/search.png")).map { url =>
      new ImageView(new Image(url.toExternalForm)) {
        fitWidth = 16; fitHeight = 16; preserveRatio = true; smooth = true; opacity = 0.7
      }
    }.orNull
    val searchOverlay = new HBox {
      alignment = Pos.CenterLeft
      padding = Insets(0, 0, 0, 12)
      children = if (searchIcon != null) Seq(searchIcon) else Seq.empty
      mouseTransparent = true
      pickOnBounds = false
    }
    val searchWrap = new StackPane { children = Seq(searchField, searchOverlay) }

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

    val topRow = new HBox {
      spacing = 12
      alignment = Pos.CenterLeft
      children = Seq(
        searchWrap,
        new Region { hgrow = Priority.Always },
        new HBox(8, sortLabel, sortBox) { alignment = Pos.CenterRight }
      )
    }

    // ---------- GRID ----------
    val grid = new TilePane {
      hgap = Gap
      vgap = Gap
      padding = Insets(8)
      prefColumns = 5
      prefTileWidth = CardW
      prefTileHeight = CardH
      tileAlignment = Pos.TopLeft
      prefWidth = Region.USE_COMPUTED_SIZE
      maxWidth  = Double.MaxValue
    }

    // ---- ADD HANDLER (single callback, no events -> no double popups) ----
    def handleAddToPlanner(m: Meal, slot: String): Unit = safeUI("handleAddMealToPlanner") {
      val date = LocalDate.now()
      PlannerView.addMealToMeal(user, date, slot, m)
      new Alert(Alert.AlertType.Information) {
        title = "Add to Planner"
        headerText = None
        contentText = s"Added meal '${Option(m.name).getOrElse("Meal")}' to $slot."
      }.showAndWait()
    }

    def cardNode(m: Meal): Region = {
      val c = new MealCard(m, onAddToPlanner = (mm, slot) => handleAddToPlanner(mm, slot))
      c.prefWidth = CardW;  c.minWidth = CardW;  c.maxWidth = CardW
      c.prefHeight = CardH; c.minHeight = CardH; c.maxHeight = CardH
      c
    }

    def safeLowerTrim(s: String): String = Option(s).map(_.trim.toLowerCase).getOrElse("")
    def getCat(m: Meal): String = Option(m.category).getOrElse("")

    var currentCategory: Option[String] = None

    def refreshGrid(): Unit = safeUI("refreshGrid") {
      val q = safeLowerTrim(searchField.text.value)
      val filtered = allMeals.filter { m =>
        val matchesCat = currentCategory.forall(getCat(m).equalsIgnoreCase)
        val matchesSearch = q.isEmpty || Option(m.name).getOrElse("").toLowerCase.contains(q)
        matchesCat && matchesSearch
      }

      val sel = sortBox.value.value
      val metric = metricOf(sel)
      val desc = isDescending(sel)

      val sorted: List[Meal] = metric match {
        case "name"     => val by = filtered.sortBy(m => Option(m.name).getOrElse("").toLowerCase); if (desc) by.reverse else by
        case "protein"  => val by = filtered.sortBy(m => Option(m.protein).getOrElse(0.0));       if (desc) by.reverse else by
        case "calories" => val by = filtered.sortBy(m => Option(m.calories).getOrElse(0.0));      if (desc) by.reverse else by
        case "carbs"    => val by = filtered.sortBy(m => Option(m.carbs).getOrElse(0.0));         if (desc) by.reverse else by
        case _          => val by = filtered.sortBy(m => Option(m.fats).getOrElse(0.0));          if (desc) by.reverse else by
      }

      grid.children.clear()
      sorted.foreach(m => grid.children += cardNode(m))
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
      PillDef("All",        None,              PRIMARY),
      PillDef("Breakfast",  Some("Breakfast"), "#FFEB99"),
      PillDef("Lunch",      Some("Lunch"),     "#B7F2C1"),
      PillDef("Dinner",     Some("Dinner"),    "#F3A5A5"),
      PillDef("Snack",      Some("Snack"),     "#BFDFFF")
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
           |-fx-text-fill: $SUBTLE;
           |-fx-border-color: #E6F2EC;
           |-fx-border-radius: 12;
           """.stripMargin
      new ToggleButton(defn.label) {
        toggleGroup = pillGroup
        focusTraversable = false
        style = unsel
        selected.onChange((_, _, _) => style = if (selected.value) sel else unsel)
        onAction = _ => safeUI(s"pill:${defn.label}") { currentCategory = defn.cat; refreshGrid() }
      }
    }
    val categoryBar = new HBox { spacing = 10; alignment = Pos.CenterLeft; children = pills.map(pill) }

    // Initial selection + first render
    pillGroup.getToggles.get(0).setSelected(true)
    refreshGrid()

    // Responsive columns – match FoodView
    def recalcColumns(totalWidth: Double): Unit = {
      val contentW = math.max(600.0, totalWidth - 22 - 22 - 16)
      val perTile  = CardW + Gap
      val cols     = math.max(1, (contentW / perTile).toInt)
      grid.prefColumns = cols
    }
    recalcColumns(stage.width.value)
    stage.width.onChange((_, _, nw) => recalcColumns(nw.doubleValue()))

    // Root layout
    new VBox {
      spacing = 14
      padding = Insets(18, 22, 22, 22)
      style = s"-fx-background-color: $SURFACE_GRADIENT;"
      children = Seq(
        new HBox(backBtn) { alignment = Pos.CenterLeft },
        topRow,
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
