package nutritionapp.dialog

import nutritionapp.model.{Food, PlannerItem, User}
import nutritionapp.component.FoodCardMini
import nutritionapp.{MealPlanner, PlannerView, FoodDatabase}

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage.Stage
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes._
import java.time.LocalDate
import scala.collection.mutable

object AddFoodDialog {

  private val INK     = "#0D3B2F"
  private val SUBTLE  = "#2F6D5F"
  private val PRIMARY = "#6DBE75"
  private val SURFACE = "linear-gradient(to bottom right, #F7FBF9, #EFF8F2)"

  // Sorting like FoodView
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
  private def toggleVariant(label: String): String = metricOf(label) match {
    case "name"     => if (label.contains("A→Z")) "Name (Z→A)" else "Name (A→Z)"
    case "protein"  => if (label.contains("High→Low")) "Protein (Low→High)" else "Protein (High→Low)"
    case "calories" => if (label.contains("High→Low")) "Calories (Low→High)" else "Calories (High→Low)"
    case "carbs"    => if (label.contains("High→Low")) "Carbs (Low→High)" else "Carbs (High→Low)"
    case "fats"     => if (label.contains("High→Low")) "Fats (Low→High)" else "Fats (High→Low)"
    case _          => if (label.contains("High→Low")) "Fiber (Low→High)" else "Fiber (High→Low)"
  }

  def create(mealType: String, selectedDate: LocalDate, ownerStage: Stage, user: User): VBox = {
    val allFoods = FoodDatabase.loadFoods()

    val title = new Label(s"Add Food to $mealType") {
      textFill = Color.web(INK)
      font = Font.font("Segoe UI", FontWeight.Bold, 20)
    }

    val searchField = new TextField {
      promptText = "Search food..."
      maxWidth = 320
      style =
        """
          |-fx-background-radius: 12;
          |-fx-padding: 8;
          |-fx-font-size: 12px;
          |-fx-background-color: white;
          |-fx-border-color: #E6F2EC;
          |-fx-border-radius: 12;
        """.stripMargin
    }

    val sortLabel = new Label("Sort:") {
      textFill = Color.web(SUBTLE)
      font = Font.font("Segoe UI", FontWeight.SemiBold, 12)
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

    val addSelectedBtn = new Button("Add Selected") {
      disable = true
      style =
        s"""
           |-fx-background-radius: 10;
           |-fx-padding: 6 12 6 12;
           |-fx-background-color: $PRIMARY;
           |-fx-text-fill: white;
         """.stripMargin
    }

    // Top row WITHOUT Back button
    val topRow = new HBox(8,
      searchField,
      new Region { HBox.setHgrow(this, Priority.Always) },
      new HBox(8, sortLabel, sortBox) { alignment = Pos.CenterRight },
      addSelectedBtn
    ) { alignment = Pos.CenterLeft }

    // Category pills (like FoodView)
    val pillGroup = new ToggleGroup()
    case class PillDef(label: String, cat: Option[String], accent: String)

    val categories = (Seq("All") ++ allFoods.flatMap(f => Option(f.category)).filter(_.nonEmpty)).distinct
    val pills: Seq[PillDef] = categories.map {
      case "All"            => PillDef("All", None, PRIMARY)
      case "Carbohydrates"  => PillDef("Carbohydrates", Some("Carbohydrates"), "#FFEB99")
      case "Fats"           => PillDef("Fats",          Some("Fats"),          "#FFC07A")
      case "Dairy"          => PillDef("Dairy",         Some("Dairy"),         "#BFDFFF")
      case "Protein"        => PillDef("Protein",       Some("Protein"),       "#F3A5A5")
      case "Fruits"         => PillDef("Fruits",        Some("Fruits"),        "#FFC9DE")
      case "Vegetables"     => PillDef("Vegetables",    Some("Vegetables"),    "#B7F2C1")
      case other            => PillDef(other,           Some(other),           "#E6F2EC")
    }

    def pill(defn: PillDef): ToggleButton = {
      val sel =
        s"""
           |-fx-background-radius: 12;
           |-fx-padding: 6 12 6 12;
           |-fx-font-size: 12px;
           |-fx-background-color: ${defn.accent};
           |-fx-text-fill: #0D3B2F;
           |-fx-border-color: transparent;
           |-fx-border-radius: 12;
         """.stripMargin
      val unsel =
        """
          |-fx-background-radius: 12;
          |-fx-padding: 6 12 6 12;
          |-fx-font-size: 12px;
          |-fx-background-color: white;
          |-fx-text-fill: #2F6D5F;
          |-fx-border-color: #E6F2EC;
          |-fx-border-radius: 12;
        """.stripMargin

      new ToggleButton(defn.label) {
        toggleGroup = pillGroup
        focusTraversable = false
        style = unsel
        selected.onChange((_, _, nowSel) => style = if (nowSel) sel else unsel)
      }
    }

    val pillButtons: Seq[ToggleButton] = pills.map(pill)
    val categoryBar = new HBox { spacing = 8; alignment = Pos.CenterLeft; children = pillButtons }

    // Grid: 3 columns, compact cards
    val grid = new TilePane {
      hgap = 14; vgap = 14; padding = Insets(8)
      prefColumns = 3
      tileAlignment = Pos.TopLeft
    }

    // Selection set for multi-add
    val selectedFoods = mutable.Set.empty[Food]
    def updateAddButton(): Unit = {
      addSelectedBtn.disable = selectedFoods.isEmpty
      if (selectedFoods.nonEmpty) addSelectedBtn.text = s"Add Selected (${selectedFoods.size})"
      else addSelectedBtn.text = "Add Selected"
    }

    def matchesQuery(food: Food, q: String): Boolean =
      q.isEmpty || Option(food.name).exists(_.toLowerCase.contains(q))

    var currentCategory: Option[String] = None

    def rebuildGrid(): Unit = {
      val q = Option(searchField.text.value).getOrElse("").trim.toLowerCase
      val filtered = allFoods.filter { f =>
        val inCat = currentCategory.forall(c => Option(f.category).exists(_.equalsIgnoreCase(c)))
        val inSearch = matchesQuery(f, q)
        inCat && inSearch
      }

      val sel = sortBox.value.value
      val metric = metricOf(sel)
      val desc   = isDescending(sel)

      val sorted: List[Food] = metric match {
        case "name" =>
          val by = filtered.sortBy(f => Option(f.name).getOrElse("").toLowerCase); if (desc) by.reverse else by
        case "protein" =>
          val by = filtered.sortBy(f => Option(f.protein).getOrElse(0.0)); if (desc) by.reverse else by
        case "calories" =>
          val by = filtered.sortBy(f => Option(f.calories).getOrElse(0.0)); if (desc) by.reverse else by
        case "carbs" =>
          val by = filtered.sortBy(f => Option(f.carbs).getOrElse(0.0)); if (desc) by.reverse else by
        case "fats" =>
          val by = filtered.sortBy(f => Option(f.fats).getOrElse(0.0)); if (desc) by.reverse else by
        case _ =>
          val by = filtered.sortBy(f => Option(f.fiber).getOrElse(0.0)); if (desc) by.reverse else by
      }

      grid.children.clear()
      sorted.foreach { food =>
        val node = FoodCardMini(food, compact = true, initiallySelected = selectedFoods.contains(food), onToggle = sel => {
          if (sel) selectedFoods += food else selectedFoods -= food
          updateAddButton()
        })
        grid.children += node
      }
    }

    // Wire pills
    pillGroup.getToggles.clear()
    pillButtons.foreach { tb =>
      tb.onAction = _ => {
        val label = tb.text.value
        currentCategory = if (label == "All") None else Some(label)
        rebuildGrid()
      }
      pillGroup.getToggles.add(tb.delegate)
    }
    if (pillButtons.nonEmpty) pillButtons.head.setSelected(true)

    // Sort toggle behavior like FoodView
    sortBox.onAction = _ => {
      val clicked = sortBox.selectionModel().getSelectedItem
      if (clicked == lastClicked) {
        val toggled = toggleVariant(clicked)
        sortBox.value = toggled
        lastClicked = toggled
      } else {
        lastClicked = clicked
      }
      rebuildGrid()
    }
    sortBox.value.onChange((_,_,_) => rebuildGrid())

    // Search & add
    searchField.text.onChange((_,_,_) => rebuildGrid())
    addSelectedBtn.onAction = _ => {
      selectedFoods.foreach { food =>
        MealPlanner.addItemForDate(
          selectedDate,
          PlannerItem(food.name, "Food", food.calories, food.protein, food.carbs, food.fats, mealType)
        )
      }
      MealPlanner.saveToFileForDate(selectedDate)
      if (ownerStage.modality != scalafx.stage.Modality.None) ownerStage.close()
      else ownerStage.scene = new Scene(PlannerView.create(ownerStage, user), 1000, 700)
    }

    rebuildGrid()

    new VBox {
      spacing = 12
      padding = Insets(16, 18, 18, 18)
      style = s"-fx-background-color: $SURFACE;"
      children = Seq(
        title,
        topRow,
        categoryBar,
        new ScrollPane {
          content = grid
          fitToWidth = true
          hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
          vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
          style = "-fx-background-color: transparent;"
          prefViewportHeight = 540
        }
      )
    }
  }
}
