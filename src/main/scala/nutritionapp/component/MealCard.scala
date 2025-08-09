package nutritionapp.component

import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.Font
import scalafx.scene.input.MouseEvent
import scalafx.Includes._

import java.time.LocalDate
import java.util.{List => JList}
import javafx.scene.control.ChoiceDialog
import scala.jdk.CollectionConverters._

import nutritionapp.MealPlanner
import nutritionapp.EventBus
import nutritionapp.model.{Meal, PlannerItem}

class MealCard(meal: Meal) extends StackPane {

  private val imageView: ImageView = new ImageView {
    val stream = Option(getClass.getResourceAsStream("/" + meal.imagePath))
    image = stream.map(new Image(_)).getOrElse(new Image("https://via.placeholder.com/150"))
    fitWidth = 150
    fitHeight = 100
    preserveRatio = true
  }

  private val addButton = new Button("Add to Planner") {
    onAction = _ => {
      val today = LocalDate.now()
      val choices: JList[String] = List("Breakfast", "Lunch", "Dinner", "Snack").asJava

      val choiceDialog = new ChoiceDialog("Lunch", choices)
      choiceDialog.setTitle("Choose Meal Type")
      choiceDialog.setHeaderText("Where do you want to add this meal?")
      choiceDialog.setContentText("Select meal type:")

      val result = choiceDialog.showAndWait()
      if (result.isPresent) {
        val mealType = result.get()
        val item = PlannerItem.fromMeal(meal, mealType)
        MealPlanner.addItemForDate(today, item)
        MealPlanner.saveToFileForDate(today)
        EventBus.onPlannerUpdated()

        new Alert(Alert.AlertType.Information) {
          title = "Added to Planner"
          contentText = s"${meal.name} added to $mealType today."
        }.showAndWait()
      }
    }
  }

  private val front = new VBox(10) {
    padding = Insets(10)
    alignment = Pos.Center
    children = Seq(imageView, new Label(meal.name) { font = Font.font(14) })
    style = "-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ddd;"
  }

  private val back = new VBox(5) {
    padding = Insets(10)
    alignment = Pos.CenterLeft
    children = Seq(
      new Label(f"Serving: ${meal.servingSize}"),
      new Label(f"Calories: ${meal.calories}%.0f kcal"),
      new Label(f"Protein: ${meal.protein}%.1f g"),
      new Label(f"Carbs: ${meal.carbs}%.1f g"),
      new Label(f"Fats: ${meal.fats}%.1f g"),
      addButton
    )
    style = "-fx-background-color: #f5f5f5; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ccc;"
  }

  private var showingFront = true

  children = Seq(front)

  onMouseClicked = (_: MouseEvent) => {
    if (showingFront) children.setAll(back)
    else children.setAll(front)
    showingFront = !showingFront
  }

  onMouseEntered = _ => {
    scaleX = 1.05
    scaleY = 1.05
  }

  onMouseExited = _ => {
    scaleX = 1.0
    scaleY = 1.0
  }
}
