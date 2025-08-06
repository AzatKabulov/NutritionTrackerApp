package nutritionapp.dialog

import nutritionapp.model.{Meal, PlannerItem, MealPlanner, User}
import nutritionapp.component.MealCardMini
import nutritionapp.MealDatabase
import nutritionapp.PlannerView

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage.Stage
import java.time.LocalDate

object AddMealDialog {

  def create(mealType: String, selectedDate: LocalDate, stage: Stage, user: User): VBox = {
    MealDatabase.load()
    val allMeals = MealDatabase.getMeals

    val searchField = new TextField {
      promptText = "Search meal..."
      maxWidth = 300
    }

    val categoryBar = new HBox(10) {
      padding = Insets(10)
      alignment = Pos.CenterLeft
    }

    val categories = allMeals.map(_.category).distinct.filter(_.nonEmpty)
    var currentCategory: Option[String] = None
    var selectedMeal: Option[Meal] = None

    val confirmBtn = new Button("✅ Add Selected") {
      disable = true
    }

    val gridPane: FlowPane = new FlowPane {
      hgap = 20
      vgap = 20
      padding = Insets(10)
      prefWrapLength = 800
    }

    def updateConfirmState(): Unit = {
      confirmBtn.disable = selectedMeal.isEmpty
    }

    def refreshGrid(): Unit = {
      val filtered = allMeals.filter { meal =>
        val matchesCategory = currentCategory.forall(_ == meal.category)
        val matchesSearch = searchField.text.value.trim.isEmpty ||
          meal.name.toLowerCase.contains(searchField.text.value.toLowerCase.trim)
        matchesCategory && matchesSearch
      }

      gridPane.children.clear()

      filtered.foreach { meal =>
        gridPane.children += MealCardMini(meal, () => {
          selectedMeal = Some(meal)
          updateConfirmState()
        })
      }
    }

    categories.foreach { cat =>
      val btn = new Button(cat) {
        onAction = _ => {
          currentCategory = Some(cat)
          refreshGrid()
        }
      }
      categoryBar.children += btn
    }

    searchField.text.onChange { (_, _, _) => refreshGrid() }

    confirmBtn.onAction = _ => {
      selectedMeal.foreach { meal =>
        val item = PlannerItem(
          name = meal.name,
          source = "Meal",
          calories = meal.totalCalories,
          protein = meal.totalProtein,
          carbs = meal.totalCarbs,
          fats = meal.totalFats,
          mealType = mealType
        )
        MealPlanner.addItemForDate(selectedDate, item)
        MealPlanner.saveToFileForDate(selectedDate)
        stage.scene = new Scene(PlannerView.create(stage, user), 1000, 700)
      }
    }

    val backBtn = new Button("⬅ Back") {
      onAction = _ => {
        stage.scene = new Scene(PlannerView.create(stage, user), 1000, 700)
      }
    }

    refreshGrid()

    new VBox(15) {
      padding = Insets(20)
      children = Seq(
        new Label(s"Add Meal to $mealType") {
          style = "-fx-font-size: 16pt; -fx-font-weight: bold;"
        },
        new HBox(10, searchField, backBtn, confirmBtn) {
          alignment = Pos.CenterLeft
        },
        categoryBar,
        new ScrollPane {
          content = gridPane
          fitToWidth = true
        }
      )
    }
  }
}
