package nutritionapp

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.stage.Stage

import nutritionapp.model.{Meal, User}
import nutritionapp.component.MealCard

object MealView {

  def create(stage: Stage, user: User): VBox = {
    MealDatabase.load() //  ensure data is loaded first
    val allMeals: List[Meal] = MealDatabase.getMeals

    println(s"[DEBUG] Loaded ${allMeals.length} meals") // debug

    val searchField = new TextField {
      promptText = "Search by name..."
      maxWidth = 300
    }

    val sortOptions = Seq("Name", "High Protein", "Low Carb", "High Carb", "Low Fat", "High Fat")
    val sortDropdown = new ComboBox[String](sortOptions) {
      value = "Name"
      maxWidth = 150
    }

    val controlBar = new HBox(20) {
      alignment = Pos.CenterLeft
      padding = Insets(10)
      children = Seq(
        new Button("⬅ Back to Dashboard") {
          onAction = _ => DashboardView.show(stage, user)
        },
        searchField,
        new Label("Sort:"),
        sortDropdown
      )
    }

    val gridPane = new FlowPane {
      hgap = 20
      vgap = 20
      padding = Insets(10)
      prefWrapLength = 800
    }

    def refreshGrid(): Unit = {
      val searchText = searchField.text.value.trim.toLowerCase
      val selectedSort = sortDropdown.value.value

      var filtered = allMeals.filter { meal =>
        searchText.isEmpty || meal.name.toLowerCase.contains(searchText)
      }

      filtered = selectedSort match {
        case "Name" => filtered.sortBy(_.name.toLowerCase)
        case "High Protein" => filtered.sortBy(-_.totalProtein)
        case "Low Carb" => filtered.sortBy(_.totalCarbs)
        case "High Carb" => filtered.sortBy(-_.totalCarbs)
        case "Low Fat" => filtered.sortBy(_.totalFats)
        case "High Fat" => filtered.sortBy(-_.totalFats)
        case _ => filtered
      }

      println(s"[DEBUG] Search: '$searchText', Sort: '$selectedSort'")
      println(s"[DEBUG] Filtered meals: ${filtered.map(_.name).mkString(", ")}")

      gridPane.children.clear()
      filtered.foreach { meal =>
        gridPane.children += new MealCard(meal)
      }
    }

    searchField.text.onChange { (_, _, _) => refreshGrid() }
    sortDropdown.onAction = _ => refreshGrid()

    refreshGrid()

    new VBox(15) {
      padding = Insets(20)
      children = Seq(
        controlBar,
        new ScrollPane {
          content = gridPane
          fitToWidth = true
        }
      )
    }
  }
}
