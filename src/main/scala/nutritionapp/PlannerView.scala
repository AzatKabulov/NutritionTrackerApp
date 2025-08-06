package nutritionapp

import nutritionapp.model._
import nutritionapp.dialog.{AddFoodDialog, AddMealDialog}
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry._
import scalafx.stage.Stage
import scalafx.Includes._
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

      MealPlanner.loadFromFileForDate(date)

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
              new Label(f"${item.calories}%.0f kcal"),
              new Button("🗑") {
                onAction = _ => {
                  MealPlanner.removeItemForDate(currentDate, item)
                  MealPlanner.saveToFileForDate(currentDate)
                  refreshPlanner(currentDate)
                }
                tooltip = new Tooltip("Remove item")
                style = "-fx-background-color: #ffdddd; -fx-border-color: #dd4444;"
              }
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
          onAction = _ => showAddFoodDialog(meal, stage, user)
        }

        val addMealBtn = new Button("➕ Add Meal") {
          onAction = _ => showAddMealDialog(meal, stage, user)
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

    def showAddFoodDialog(mealType: String, stage: Stage, user: User): Unit = {
      stage.scene = new scalafx.scene.Scene(
        AddFoodDialog.create(mealType, currentDate, stage, user),
        1000,
        700
      )
      MealPlanner.saveToFileForDate(currentDate)
    }

    def showAddMealDialog(mealType: String, stage: Stage, user: User): Unit = {
      stage.scene = new scalafx.scene.Scene(
        AddMealDialog.create(mealType, currentDate, stage, user),
        1000,
        700
      )
      MealPlanner.saveToFileForDate(currentDate)
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

    refreshPlanner(currentDate)
    root
  }
}
