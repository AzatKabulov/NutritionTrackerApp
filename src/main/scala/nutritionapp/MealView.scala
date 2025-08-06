package nutritionapp

import nutritionapp.model.{Meal, User}
import nutritionapp.component.MealCard
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry._
import scalafx.stage.Stage

object MealView {

  def create(stage: Stage, user: User): VBox = {
    // Load meals from file
    MealDatabase.load()
    val mealList: List[Meal] = MealDatabase.getMeals

    // Create vertical list of meal cards
    val mealCards = new VBox(10) {
      padding = Insets(10)
      alignment = Pos.TopCenter
    }

    // Populate cards
    mealList.foreach { meal =>
      mealCards.children += MealCard(meal) // ✅ Fixed: only pass the meal
    }

    // Back button to return to dashboard
    val backButton = new Button("⬅ Back to Dashboard") {
      onAction = _ => DashboardView.show(stage, user)
    }

    // Final layout
    new VBox(20) {
      padding = Insets(20)
      alignment = Pos.TopCenter
      children = Seq(
        backButton,
        new ScrollPane {
          content = mealCards
          fitToWidth = true
        }
      )
    }
  }
}
