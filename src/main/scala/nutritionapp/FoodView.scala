package nutritionapp

import nutritionapp.model.Food
import nutritionapp.component.FoodCard
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry._
import scalafx.stage.Stage
import nutritionapp.model.User

object FoodView {

  def create(stage: Stage, user: User): VBox = {
    val foodList = FoodDatabase.loadFoods()

    val grid = new FlowPane {
      hgap = 20
      vgap = 20
      padding = Insets(20)
      alignment = Pos.Center
      prefWrapLength = 700
      children = foodList.map(food => new FoodCard(food))
    }

    val backButton = new Button("⬅ Back to Dashboard") {
      onAction = _ => DashboardView.show(stage, user)
    }

    new VBox(20) {
      padding = Insets(20)
      alignment = Pos.TopCenter
      children = Seq(backButton, new ScrollPane {
        content = grid
        fitToWidth = true
        style = "-fx-background-color: #ffffff;"
      })
    }
  }
}
