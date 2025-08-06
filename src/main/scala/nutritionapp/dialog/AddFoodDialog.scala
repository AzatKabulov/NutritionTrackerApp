package nutritionapp.dialog

import nutritionapp.model.{Food, PlannerItem, MealPlanner, User}
import nutritionapp.component.FoodCardMini
import nutritionapp.FoodDatabase
import nutritionapp.PlannerView

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage.Stage
import java.time.LocalDate

object AddFoodDialog {

  def create(mealType: String, selectedDate: LocalDate, stage: Stage, user: User): VBox = {
    val allFoods = FoodDatabase.loadFoods()

    val searchField = new TextField {
      promptText = "Search food..."
      maxWidth = 300
    }

    val categoryBar = new HBox(10) {
      padding = Insets(10)
      alignment = Pos.CenterLeft
    }

    val categories = allFoods.map(_.category).distinct.filter(_.nonEmpty)
    var currentCategory: Option[String] = None
    var selectedFood: Option[Food] = None

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
      confirmBtn.disable = selectedFood.isEmpty
    }

    def refreshGrid(): Unit = {
      val filtered = allFoods.filter { food =>
        val matchesCategory = currentCategory.forall(_ == food.category)
        val matchesSearch = searchField.text.value.trim.isEmpty ||
          food.name.toLowerCase.contains(searchField.text.value.toLowerCase.trim)
        matchesCategory && matchesSearch
      }

      gridPane.children.clear()

      filtered.foreach { food =>
        gridPane.children += FoodCardMini(food, () => {
          selectedFood = Some(food)
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
      selectedFood.foreach { food =>
        val item = PlannerItem(
          name = food.name,
          source = "Food",
          calories = food.calories,
          protein = food.protein,
          carbs = food.carbs,
          fats = food.fats,
          mealType = mealType
        )
        MealPlanner.addItemForDate(selectedDate, item)
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
        new Label(s"Add Food to $mealType") {
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
