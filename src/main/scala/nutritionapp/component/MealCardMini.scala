package nutritionapp.component

import nutritionapp.model.Meal
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.geometry._

object MealCardMini {

  def apply(meal: Meal, onSelect: () => Unit): VBox = {

    val imageView = new ImageView {
      val stream = Option(getClass.getResourceAsStream("/" + meal.imagePath))
      image = stream.map(new Image(_)).getOrElse(new Image("https://via.placeholder.com/80"))
      fitWidth = 80
      fitHeight = 80
      preserveRatio = true
    }

    val nameLabel = new Label(meal.name) {
      style = "-fx-font-weight: bold; -fx-font-size: 13pt;"
      wrapText = true
    }

    val kcalLabel = new Label(f"${meal.calories}%.0f kcal") {
      style = "-fx-text-fill: #444;"
    }

    val macrosLabel = new Label(f"P: ${meal.protein}%.1f | C: ${meal.carbs}%.1f | F: ${meal.fats}%.1f") {
      style = "-fx-font-size: 10pt; -fx-text-fill: #777;"
    }

    val selectBtn = new Button("Select") {
      onAction = _ => onSelect()
      maxWidth = Double.MaxValue
    }

    new VBox(8) {
      alignment = Pos.Center
      padding = Insets(8)
      spacing = 6
      children = Seq(imageView, nameLabel, kcalLabel, macrosLabel, selectBtn)
      prefWidth = 180
      maxWidth = 180
      style = "-fx-border-color: #ccc; -fx-background-color: #fefefe; -fx-border-radius: 6; -fx-background-radius: 6;"
    }
  }
} 
