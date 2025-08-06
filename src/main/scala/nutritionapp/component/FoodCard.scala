package nutritionapp.component

import nutritionapp.model.Food
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.text.Font
import scalafx.scene.input.MouseEvent
import scalafx.Includes._

class FoodCard(food: Food) extends StackPane {

  // === Image ===
  private val imageView: ImageView = new ImageView {
    val stream = Option(getClass.getResourceAsStream("/" + food.imagePath))
    image = stream.map(new Image(_)).getOrElse(new Image("https://via.placeholder.com/150"))
    fitWidth = 150
    fitHeight = 100
    preserveRatio = true
  }

  // === Front
  private val front = new VBox(10) {
    padding = Insets(10)
    alignment = Pos.Center
    children = Seq(
      imageView,
      new Label(food.name) {
        font = Font.font(14)
      }
    )
    style = "-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ddd;"
  }

  // === Back
  private val back = new VBox(5) {
    padding = Insets(10)
    alignment = Pos.CenterLeft
    children = Seq(
      new Label(f"Calories: ${food.calories}%.0f kcal"),
      new Label(f"Protein: ${food.protein}%.1f g"),
      new Label(f"Carbs: ${food.carbs}%.1f g"),
      new Label(f"Fats: ${food.fats}%.1f g"),
      new Label(f"Fiber: ${food.fiber}%.1f g"),
      new Button("Add to Meal")
    )
    style = "-fx-background-color: #f5f5f5; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ccc;"
  }

  private var showingFront = true

  children = Seq(front)

  onMouseClicked = (_: MouseEvent) => {
    if (showingFront) {
      children.setAll(back)
    } else {
      children.setAll(front)
    }
    showingFront = !showingFront
  }

  // Hover animation
  onMouseEntered = _ => {
    scaleX = 1.05
    scaleY = 1.05
  }
  onMouseExited = _ => {
    scaleX = 1.0
    scaleY = 1.0
  }
}
