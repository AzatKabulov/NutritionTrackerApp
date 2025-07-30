import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.collections.ObservableBuffer
import scalafx.Includes._

object NutritionTrackerApp extends JFXApp3 {

  val foodManager = new MealManager[Food]()
  val mealManager = new MealManager[Meal]()
  val foodListBuffer = ObservableBuffer[String]()
  val mealListBuffer = ObservableBuffer[String]()
  val checkboxes = ObservableBuffer[CheckBox]()
  val historyBuffer = ObservableBuffer[String]()

  def logToHistory(item: NutritionInfo): Unit = {
    val entry = HistoryEntry(item)
    historyBuffer += entry.displayString
  }

  override def start(): Unit = {
    // Load embedded foods from database
    FoodNutritionDatabase.allFoods.foreach { food =>
    foodManager.addItem(food)
      foodListBuffer += food.printSummary()
    }

    checkboxes ++= foodManager.getAllItems().map(f =>
      new CheckBox(f.name) { userData = f }
    )

    stage = new JFXApp3.PrimaryStage {
      title = "Nutrition Tracker"
      width = 750
      height = 550

      scene = new Scene {
        root = new BorderPane {
          padding = Insets(10)

          top = new MenuBar {
            menus = List(
              new Menu("File") {
                items = List(
                  new MenuItem("Export Report"),
                  new SeparatorMenuItem(),
                  new MenuItem("Exit")
                )
              }
            )
          }

          center = new TabPane {
            tabs = Seq(
              new Tab {
                text = "Foods"
                closable = false
                content = new VBox(10) {
                  padding = Insets(10)
                  children = Seq(
                    new Label("P = Protein, F = Fat, C = Carbohydrates") {
                      style = "-fx-font-size: 12px; -fx-text-fill: #777;"
                    },
                    new Label("List of Foods:"),
                    new ListView(foodListBuffer)
                  )
                }
              },

              new Tab {
                text = "Meals"
                closable = false
                content = new VBox(10) {
                  padding = Insets(10)

                  val mealNameField = new TextField { promptText = "Meal Name" }
                  val mealStatusLabel = new Label()
                  val mealListView = new ListView(mealListBuffer)

                  def refreshCheckboxes(): Unit = {
                    checkboxes.clear()
                    checkboxes ++= foodManager.getAllItems().map(food =>
                      new CheckBox(food.name) { userData = food }
                    )
                  }

                  refreshCheckboxes()

                  val addMealButton = new Button("Add Meal") {
                    onAction = _ => {
                      val selectedFoods = checkboxes.filter(_.selected.value).map(_.userData.asInstanceOf[Food]).toList
                      val name = mealNameField.text.value.trim

                      if (name.nonEmpty && selectedFoods.nonEmpty) {
                        val meal = new Meal(name, selectedFoods)
                        mealManager.addItem(meal)
                        mealListBuffer += meal.printSummary()
                        logToHistory(meal)

                        mealStatusLabel.text = s"Meal '$name' added"
                        mealNameField.clear()
                        checkboxes.foreach(_.selected = false)
                      } else {
                        mealStatusLabel.text = "Please enter a meal name and select at least one food"
                      }
                    }
                  }

                  children = Seq(
                    new Label("Create a New Meal:"),
                    mealNameField,
                    new VBox(5) { children = checkboxes },
                    addMealButton,
                    mealStatusLabel,
                    new Separator(),
                    new Label("Saved Meals:"),
                    mealListView
                  )
                }
              },

              new Tab {
                text = "History"
                closable = false
                content = new VBox(10) {
                  padding = Insets(10)
                  children = Seq(
                    new Label("History Log:"),
                    new ListView(historyBuffer)
                  )
                }
              },

              new Tab {
                text = "Add Food"
                closable = false
                content = new VBox(10) {
                  padding = Insets(10)
                  alignment = Pos.TopLeft

                  val nameField     = new TextField { promptText = "Name" }
                  val portionField  = new TextField { promptText = "Portion" }
                  val categoryField = new TextField { promptText = "Category" }
                  val calField      = new TextField { promptText = "Calories" }
                  val proField      = new TextField { promptText = "Protein (g)" }
                  val fatField      = new TextField { promptText = "Fat (g)" }
                  val satFatField   = new TextField { promptText = "Saturated Fat (g)" }
                  val fiberField    = new TextField { promptText = "Fiber (g)" }
                  val carbField     = new TextField { promptText = "Carbs (g)" }
                  val gramField     = new TextField { promptText = "Serving Size (g)" }
                  val statusLabel   = new Label()

                  val addButton = new Button("Add Food") {
                    onAction = _ => {
                      try {
                        val name = nameField.text.value
                        val portion = portionField.text.value // New field you'll add
                        val category = categoryField.text.value
                        val cal = calField.text.value.toDouble
                        val pro = proField.text.value.toDouble
                        val fat = fatField.text.value.toDouble
                        val carbs = carbField.text.value.toDouble
                        val grams = gramField.text.value.toDouble

                        val food = new Food(name, portion, category, cal, pro, fat, carbs, grams)

                        foodManager.addItem(food)
                        foodListBuffer += food.printSummary()
                        logToHistory(food)

                        checkboxes.clear()
                        checkboxes ++= foodManager.getAllItems().map(f =>
                          new CheckBox(f.name) { userData = f }
                        )

                        nameField.clear(); categoryField.clear(); calField.clear()
                        proField.clear(); fatField.clear(); satFatField.clear()
                        fiberField.clear(); carbField.clear(); gramField.clear()

                        statusLabel.text = s"Food '$name' added"
                      } catch case _: Exception =>
                        statusLabel.text = "Invalid input: please enter all values correctly"
                    }
                  }

                  children = Seq(
                    new Label("Add New Food:"),
                    nameField, categoryField,  portionField,
                    calField, proField, fatField,
                    satFatField, fiberField, carbField, gramField,
                    addButton,
                    statusLabel
                  )
                }
              }
            )
          }
        }
      }
    }
  }
}
