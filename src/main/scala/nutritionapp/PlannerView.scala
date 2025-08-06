package nutritionapp

import nutritionapp.model._
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry._
import scalafx.stage.Stage
import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PlannerView {

  private var currentDate: LocalDate = LocalDate.now()

  private val dateLabel = new Label()
  private val datePicker = new DatePicker(currentDate)

  private def formatDate(date: LocalDate): String = {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val tomorrow = today.plusDays(1)

    if (date == today) "Today"
    else if (date == yesterday) "Yesterday"
    else if (date == tomorrow) "Tomorrow"
    else date.getDayOfWeek.toString.capitalize + ", " + date.format(DateTimeFormatter.ofPattern("MMM d"))
  }

  def create(stage: Stage, user: User): VBox = {
    val mealSectionsBox = new VBox(15)
    val totalLabel = new Label()

    def refreshPlanner(date: LocalDate): Unit = {
      currentDate = date
      dateLabel.text = formatDate(date)
      datePicker.value = date

      val items = MealPlanner.getItemsForDate(date)
      val grouped = items.groupBy(_.mealType)

      mealSectionsBox.children.clear()
      var totalCals, totalProtein, totalCarbs, totalFats = 0.0

      val mealOrder = Seq("Breakfast", "Lunch", "Dinner", "Snack")
      for (meal <- mealOrder) {
        val section = new VBox(5)
        val mealItems = grouped.getOrElse(meal, Seq())

        val sectionLabel = new Label(s"🍽️ $meal") {
          style = "-fx-font-size: 16pt; -fx-font-weight: bold;"
        }

        val mealItemNodes = mealItems.map { item =>
          new HBox(10) {
            alignment = Pos.CenterLeft
            children = Seq(
              new Label(s"🍎 ${item.name}"),
              new Label(f"${item.calories}%.0f kcal")
            )
          }
        }

        val kcal = mealItems.map(_.calories).sum
        val protein = mealItems.map(_.protein).sum
        val carbs = mealItems.map(_.carbs).sum
        val fats = mealItems.map(_.fats).sum

        totalCals += kcal
        totalProtein += protein
        totalCarbs += carbs
        totalFats += fats

        val macroSummary = new Label(f"Total: $kcal%.0f kcal, $protein%.1f g P, $carbs%.1f g C, $fats%.1f g F") {
          style = "-fx-font-style: italic; -fx-text-fill: #666;"
        }

        val addFoodBtn = new Button("➕ Add Food") {
          onAction = _ => showAddFoodDialog(meal, stage)
        }

        val addMealBtn = new Button("➕ Add Meal") {
          onAction = _ => showAddMealDialog(meal, stage)
        }

        val actionRow = new HBox(10, addFoodBtn, addMealBtn) {
          alignment = Pos.CenterLeft
        }

        section.children ++= (
          Seq(sectionLabel, macroSummary, actionRow).map(_.delegate) ++
            mealItemNodes.map(_.delegate)
          )
        section.padding = Insets(10)
        section.style = "-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #f9f9f9;"
        mealSectionsBox.children += section
      }

      totalLabel.text = f"🔢 Daily Total: $totalCals%.0f kcal, $totalProtein%.1f g P, $totalCarbs%.1f g C, $totalFats%.1f g F"
    }

    def showAddFoodDialog(mealType: String, stage: Stage): Unit = {
      val foods: List[Food] = List(
        Food("Oats", 150, 5, 27, 3),
        Food("Eggs", 200, 12, 1, 15)
      )

      val dialog = new ChoiceDialog(defaultChoice = foods.head, choices = foods) {
        title = s"Add Food to $mealType"
        headerText = s"Select a food to add to $mealType:"
        contentText = "Food:"
      }

      dialog.showAndWait().foreach { selected =>
        val food = selected.asInstanceOf[Food]
        val item = PlannerItem(
          name = food.name,
          source = "Food",
          calories = food.calories,
          protein = food.protein,
          carbs = food.carbs,
          fats = food.fats,
          mealType = mealType
        )
        MealPlanner.addItemForDate(currentDate, item)
        refreshPlanner(currentDate)
      }
    }

    def showAddMealDialog(mealType: String, stage: Stage): Unit = {
      val meals: List[Meal] = List(
        Meal("Simple Chicken Meal", List(Food("Chicken", 250, 30, 0, 10))),
        Meal("Tuna Sandwich", List(Food("Tuna", 200, 25, 3, 4), Food("Bread", 100, 3, 20, 1)))
      )

      val dialog = new ChoiceDialog(defaultChoice = meals.head, choices = meals) {
        title = s"Add Meal to $mealType"
        headerText = s"Select a meal to add to $mealType:"
        contentText = "Meal:"
      }

      dialog.showAndWait().foreach { selected =>
        val meal = selected.asInstanceOf[Meal]
        val item = PlannerItem(
          name = meal.name,
          source = "Meal",
          calories = meal.totalCalories,
          protein = meal.totalProtein,
          carbs = meal.totalCarbs,
          fats = meal.totalFats,
          mealType = mealType
        )
        MealPlanner.addItemForDate(currentDate, item)
        refreshPlanner(currentDate)
      }
    }

    val prevButton = new Button("⬅") {
      onAction = _ => refreshPlanner(currentDate.minusDays(1))
    }

    val nextButton = new Button("➡") {
      onAction = _ => refreshPlanner(currentDate.plusDays(1))
    }

    val todayButton = new Button("Today") {
      onAction = _ => refreshPlanner(LocalDate.now())
    }

    datePicker.onAction = _ => {
      val selected = datePicker.value.value
      if (selected != null) refreshPlanner(selected)
    }

    val dateNavBar = new HBox(10, prevButton, dateLabel, nextButton, datePicker, todayButton) {
      alignment = Pos.Center
      padding = Insets(10)
    }

    val backButton = new Button("⬅ Back to Dashboard") {
      onAction = _ => DashboardView.show(stage, user)
    }

    val root = new VBox(15) {
      padding = Insets(20)
      alignment = Pos.TopCenter
      children = Seq(
        backButton,
        dateNavBar,
        mealSectionsBox,
        totalLabel
      )
    }
    MealPlanner.addItemForDate(currentDate, PlannerItem(
      name = "Test Oats",
      source = "Food",
      calories = 150,
      protein = 5,
      carbs = 27,
      fats = 3,
      mealType = "Lunch"
    ))

    refreshPlanner(currentDate)
    return root
  }
}
