package nutritionapp.dialog

import nutritionapp.model.{Meal, PlannerItem, User}
import nutritionapp.component.MealCardMini
import nutritionapp.{MealDatabase, MealPlanner, PlannerView}

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage.Stage
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes._
import java.time.LocalDate

object AddMealDialog {

  private val INK     = "#0D3B2F"
  private val SUBTLE  = "#2F6D5F"
  private val PRIMARY = "#6DBE75"
  private val SURFACE = "linear-gradient(to bottom right, #F7FBF9, #EFF8F2)"

  private val SortItems: Seq[String] = Seq(
    "Name (A→Z)", "Name (Z→A)",
    "Calories (High→Low)", "Calories (Low→High)",
    "Protein (High→Low)",  "Protein (Low→High)",
    "Carbs (High→Low)",    "Carbs (Low→High)",
    "Fats (High→Low)",     "Fats (Low→High)"
  )
  private def metricOf(label: String): String = {
    val l = label.toLowerCase
    if (l.startsWith("name")) "name"
    else if (l.startsWith("calories")) "calories"
    else if (l.startsWith("protein")) "protein"
    else if (l.startsWith("carbs")) "carbs"
    else "fats"
  }
  private def isDescending(label: String): Boolean =
    label.contains("Z→A") || label.contains("High→Low")
  private def toggleVariant(label: String): String = metricOf(label) match {
    case "name"     => if (label.contains("A→Z")) "Name (Z→A)" else "Name (A→Z)"
    case "calories" => if (label.contains("High→Low")) "Calories (Low→High)" else "Calories (High→Low)"
    case "protein"  => if (label.contains("High→Low")) "Protein (Low→High)"  else "Protein (High→Low)"
    case "carbs"    => if (label.contains("High→Low")) "Carbs (Low→High)"    else "Carbs (High→Low)"
    case _          => if (label.contains("High→Low")) "Fats (Low→High)"     else "Fats (High→Low)"
  }

  def create(mealType: String, selectedDate: LocalDate, ownerStage: Stage, user: User): VBox = {
    MealDatabase.load()
    val allMeals = MealDatabase.getMeals

    val title = new Label(s"Add Meal to $mealType") {
      textFill = Color.web(INK)
      font = Font.font("Segoe UI", FontWeight.Bold, 20)
    }

    val searchField = new TextField {
      promptText = "Search meal..."
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

    // Top row without Back button
    val topRow = new HBox(8,
      searchField,
      new Region { HBox.setHgrow(this, Priority.Always) },
      new HBox(8, sortLabel, sortBox) { alignment = Pos.CenterRight },
      addSelectedBtn
    ) { alignment = Pos.CenterLeft }

    // Pills like AddFoodDialog
    val pillGroup = new ToggleGroup()
    case class PillDef(label: String, cat: Option[String], accent: String)

    val categories = (Seq("All") ++ allMeals.map(_.category).filter(_.nonEmpty)).distinct
    val pills: Seq[PillDef] = categories.map {
      case "All"       => PillDef("All", None, PRIMARY)
      case "Breakfast" => PillDef("Breakfast", Some("Breakfast"), "#B7F2C1")
      case "Lunch"     => PillDef("Lunch",     Some("Lunch"),     "#BFDFFF")
      case "Dinner"    => PillDef("Dinner",    Some("Dinner"),    "#FFC9DE")
      case "Snack"     => PillDef("Snack",     Some("Snack"),     "#FFEB99")
      case other       => PillDef(other,       Some(other),       "#E6F2EC")
    }

    def pill(defn: PillDef): ToggleButton = {
      val sel =
        s"""
           |-fx-background-radius: 12;
           |-fx-padding: 6 12;
           |-fx-font-size: 12px;
           |-fx-background-color: ${defn.accent};
           |-fx-text-fill: #0D3B2F;
           |-fx-border-color: transparent;
           |-fx-border-radius: 12;
         """.stripMargin
      val unsel =
        """
          |-fx-background-radius: 12;
          |-fx-padding: 6 12;
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

    // Grid
    val grid = new TilePane {
      hgap = 14; vgap = 14; padding = Insets(8)
      prefColumns = 3
      tileAlignment = Pos.TopLeft
    }

    // Single-selection (card shows its own selected visuals)
    var selectedMeal: Option[Meal] = None
    def updateAddButton(): Unit = addSelectedBtn.disable = selectedMeal.isEmpty

    var currentCategory: Option[String] = None
    def matchesQuery(meal: Meal, q: String): Boolean =
      q.isEmpty || meal.name.toLowerCase.contains(q)

    def rebuildGrid(): Unit = {
      val q = Option(searchField.text.value).getOrElse("").trim.toLowerCase

      val filtered = allMeals.filter { m =>
        val inCat = currentCategory.forall(c => m.category.equalsIgnoreCase(c))
        val inSearch = matchesQuery(m, q)
        inCat && inSearch
      }

      val sel = sortBox.value.value
      val metric = metricOf(sel)
      val desc   = isDescending(sel)

      val sorted: List[Meal] = metric match {
        case "name" =>
          val by = filtered.sortBy(_.name.toLowerCase); if (desc) by.reverse else by
        case "calories" =>
          val by = filtered.sortBy(_.totalCalories);    if (desc) by.reverse else by
        case "protein" =>
          val by = filtered.sortBy(_.totalProtein);     if (desc) by.reverse else by
        case "carbs" =>
          val by = filtered.sortBy(_.totalCarbs);       if (desc) by.reverse else by
        case _ =>
          val by = filtered.sortBy(_.totalFats);        if (desc) by.reverse else by
      }

      grid.children.clear()
      sorted.foreach { meal =>
        val node = MealCardMini(
          meal,
          compact = true,
          initiallySelected = selectedMeal.contains(meal),
          onToggle = sel => {
            selectedMeal = if (sel) Some(meal) else None
            updateAddButton()
            // Force single-select look by refreshing others
            rebuildGrid()
          }
        )
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

    // Sort behavior like AddFoodDialog
    sortBox.onAction = _ => {
      val clicked = sortBox.selectionModel().getSelectedItem
      if (clicked == lastClicked) {
        val toggled = toggleVariant(clicked)
        sortBox.value = toggled
        lastClicked = toggled
      } else lastClicked = clicked
      rebuildGrid()
    }
    sortBox.value.onChange((_,_,_) => rebuildGrid())
    searchField.text.onChange((_,_,_) => rebuildGrid())

    // Add selected meal, then close dialog/window
    addSelectedBtn.onAction = _ => {
      selectedMeal.foreach { meal =>
        MealPlanner.addItemForDate(
          selectedDate,
          PlannerItem(meal.name, "Meal", meal.totalCalories, meal.totalProtein, meal.totalCarbs, meal.totalFats, mealType)
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
