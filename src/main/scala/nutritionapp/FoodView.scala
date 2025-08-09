package nutritionapp

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage.Stage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes._

import nutritionapp.model.{Food, User}
import nutritionapp.component.FoodCard

object FoodView {

  // Palette
  private val INK     = "#0D3B2F"
  private val SUBTLE  = "#2F6D5F"
  private val PRIMARY = "#6DBE75"
  private val SURFACE_GRADIENT =
    "linear-gradient(to bottom right, #F3FBF6, #EAF6EF)"

  // Card sizing (5 per row)
  private val CardW = 210.0
  private val CardH = 230.0

  // Small helper to catch & print errors from UI handlers
  private def safeUI(tag: String)(body: => Unit): Unit = {
    try body
    catch {
      case e: Throwable =>
        println(s"[UI ERROR][$tag] ${e.getClass.getSimpleName}: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  // ---------- Sort state helpers ----------
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
  private def isDescending(label: String): Boolean =
    label.contains("Z→A") || label.contains("High→Low")

  private def toggleVariant(label: String): String = {
    val m = metricOf(label)
    m match {
      case "name"     => if (label.contains("A→Z")) "Name (Z→A)" else "Name (A→Z)"
      case "protein"  => if (label.contains("High→Low")) "Protein (Low→High)" else "Protein (High→Low)"
      case "calories" => if (label.contains("High→Low")) "Calories (Low→High)" else "Calories (High→Low)"
      case "carbs"    => if (label.contains("High→Low")) "Carbs (Low→High)" else "Carbs (High→Low)"
      case "fats"     => if (label.contains("High→Low")) "Fats (Low→High)" else "Fats (High→Low)"
      case _          => if (label.contains("High→Low")) "Fiber (Low→High)" else "Fiber (High→Low)"
    }
  }
  // ---------------------------------------

  def create(stage: Stage, user: User): VBox = {
    val allFoods: List[Food] = FoodDatabase.loadFoods()
    var currentCategory: Option[String] = None

    // Back
    val backBtn = new Button("← Back to Dashboard") {
      style =
        s"""
           |-fx-background-color: transparent;
           |-fx-text-fill: $SUBTLE;
           |-fx-font-size: 14px;
           |-fx-font-weight: bold;
           """.stripMargin
      onAction = _ => DashboardView.show(stage, user)
      focusTraversable = false
    }

    // Search
    val searchField = new TextField {
      promptText = "Search by name..."
      maxWidth = 380
      style =
        """
          |-fx-background-radius: 12;
          |-fx-padding: 10;
          |-fx-font-size: 13px;
          |-fx-background-color: white;
          |-fx-border-color: #E6F2EC;
          |-fx-border-radius: 12;
          """.stripMargin
    }

    // Sort (expanded options with toggle behavior)
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
    // Track last click to support "click same option to invert"
    var lastClicked: String = sortBox.value.value
    sortBox.onAction = _ => safeUI("sortToggle") {
      val clicked = sortBox.selectionModel().getSelectedItem
      // If user selected the same item again, flip to its opposite variant
      if (clicked == lastClicked) {
        val toggled = toggleVariant(clicked)
        sortBox.value = toggled
        lastClicked = toggled
      } else {
        lastClicked = clicked
      }
      refreshGrid()
    }
    // still refresh when value changes programmatically (above)
    sortBox.value.onChange((_, _, _) => safeUI("sortChange")(refreshGrid()))

    val topSearchRow = new HBox {
      spacing = 12
      alignment = Pos.CenterLeft
      children = Seq(
        searchField,
        new Region { hgrow = Priority.Always },
        new HBox(8, sortLabel, sortBox) { alignment = Pos.CenterRight }
      )
    }

    // ---------- Grid (before refresh/category bar) ----------
    val grid = new TilePane {
      hgap = 18
      vgap = 18
      padding = Insets(8)
      prefColumns = 5
      prefTileWidth = CardW
      prefTileHeight = CardH
      tileAlignment = Pos.TopLeft
    }

    def cardNode(food: Food): Region = {
      val c = new FoodCard(food)
      c.prefWidth = CardW;  c.minWidth = CardW;  c.maxWidth = CardW
      c.prefHeight = CardH; c.minHeight = CardH; c.maxHeight = CardH
      c
    }

    def safeLowerTrim(s: String): String =
      Option(s).map(_.trim.toLowerCase).getOrElse("")

    def refreshGrid(): Unit = safeUI("refreshGrid") {
      val q = safeLowerTrim(searchField.text.value)

      val filtered = allFoods.filter { f =>
        val cat  = Option(f.category).getOrElse("")
        val name = Option(f.name).getOrElse("")
        val matchesCat    = currentCategory.forall(cat.equalsIgnoreCase)
        val matchesSearch = q.isEmpty || name.toLowerCase.contains(q)
        matchesCat && matchesSearch
      }

      // ---- Sorting logic ----
      val sel = sortBox.value.value
      val metric = metricOf(sel)
      val desc   = isDescending(sel)

      val sorted: List[Food] = metric match {
        case "name" =>
          val by = filtered.sortBy(f => Option(f.name).getOrElse("").toLowerCase)
          if (desc) by.reverse else by
        case "protein" =>
          val by = filtered.sortBy(f => Option(f.protein).getOrElse(0.0))
          if (desc) by.reverse else by
        case "calories" =>
          val by = filtered.sortBy(f => Option(f.calories).getOrElse(0.0))
          if (desc) by.reverse else by
        case "carbs" =>
          val by = filtered.sortBy(f => Option(f.carbs).getOrElse(0.0))
          if (desc) by.reverse else by
        case "fats" =>
          val by = filtered.sortBy(f => Option(f.fats).getOrElse(0.0))
          if (desc) by.reverse else by
        case _ => // fiber
          val by = filtered.sortBy(f => Option(f.fiber).getOrElse(0.0))
          if (desc) by.reverse else by
      }
      // -----------------------

      grid.children.clear()
      sorted.foreach(food => grid.children += cardNode(food))
    }
    // --------------------------------------------------------

    // Category pills with accents
    val pillGroup = new ToggleGroup()
    case class PillDef(label: String, cat: Option[String], accent: String)
    val pills = Seq(
      PillDef("All",        None,                PRIMARY),
      PillDef("Fruits",     Some("Fruits"),      "#FFB74A"),
      PillDef("Vegetables", Some("Vegetables"),  "#6FBF5B"),
      PillDef("Protein",    Some("Protein"),     "#4C6EF5"),
      PillDef("Carbs",      Some("Carbohydrates"), "#FFD43B"),
      PillDef("Fats",       Some("Fats"),        "#E599A6"),
      PillDef("Dairy",      Some("Dairy"),       "#22B8CF")
    )

    def pill(defn: PillDef): ToggleButton = {
      val sel =
        s"""
           |-fx-background-radius: 12;
           |-fx-padding: 8 14 8 14;
           |-fx-font-size: 13px;
           |-fx-background-color: ${defn.accent};
           |-fx-text-fill: white;
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

    // Wire up + defaults
    searchField.text.onChange((_, _, _) => safeUI("searchChange")(refreshGrid()))
    pillGroup.getToggles.get(0).setSelected(true) // "All"
    refreshGrid()

    // Root
    new VBox {
      spacing = 14
      padding = Insets(18, 22, 22, 22)
      style = s"-fx-background-color: $SURFACE_GRADIENT;"
      children = Seq(
        new HBox(backBtn) { alignment = Pos.CenterLeft },
        topSearchRow,
        categoryBar,
        new ScrollPane {
          content = grid
          fitToWidth = true
          hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
          vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
          style = "-fx-background-color: transparent;"
        }
      )
    }
  }
}
