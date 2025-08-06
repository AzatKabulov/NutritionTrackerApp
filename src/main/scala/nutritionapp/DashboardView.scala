package nutritionapp

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.scene.text.Font
import scalafx.stage.Stage
import nutritionapp.model.User

object DashboardView {

  def show(stage: Stage, user: User): Unit = {
    println(s"🧭 Entered DashboardView for ${user.name}")

    val title = new Label(s"Welcome, ${user.name}!") {
      font = Font.font(20)
    }

    def menuButton(title: String, imagePath: String)(onClick: () => Unit): VBox = {
      val img = new ImageView {
        val stream = Option(getClass.getResourceAsStream("/images/" + imagePath))
        image = stream.map(new Image(_)).getOrElse(new Image("https://via.placeholder.com/120"))
        fitWidth = 120
        fitHeight = 120
        preserveRatio = true
      }

      val label = new Label(title) {
        font = Font.font(15)
      }

      new VBox(12) {
        alignment = Pos.Center
        padding = Insets(15)
        children = Seq(img, label)
        style =
          "-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-background-radius: 10; -fx-border-radius: 10;"

        onMouseEntered = _ => {
          scaleX = 1.05
          scaleY = 1.05
        }
        onMouseExited = _ => {
          scaleX = 1.0
          scaleY = 1.0
        }
        onMouseClicked = _ => onClick()

        prefWidth = 200
        prefHeight = 220
      }
    }

    // ✅ Arrange 3x2 buttons exactly using GridPane
    val grid = new GridPane {
      hgap = 40
      vgap = 40
      alignment = Pos.Center

      add(menuButton("🥗 Food", "food.png")(() => {
        stage.scene = new Scene(FoodView.create(stage, user), 1000, 700)
      }), 0, 0)

      add(menuButton("🍽 Meals", "meals.png")(() => {
        stage.scene = new Scene(MealView.create(stage, user), 1000, 700)
      }), 1, 0)


      add(menuButton("🧮 Planner", "planner.png")(() => {
        stage.scene = new Scene(PlannerView.create(stage, user), 1000, 700)
      }), 2, 0)


      add(menuButton("📜 History", "history.png")(() => {
        stage.scene = new Scene(new VBox {
          padding = Insets(20)
          alignment = Pos.Center
          children = Seq(new Label("History View Placeholder"))
        })
      }), 0, 1)

      add(menuButton("👤 Profile", "profile.png")(() => {
        stage.scene = new Scene(new VBox {
          padding = Insets(20)
          alignment = Pos.Center
          children = Seq(new Label("Profile View Placeholder"))
        })
      }), 1, 1)

      add(menuButton("⚙️ Settings", "settings.png")(() => {
        stage.scene = new Scene(new VBox {
          padding = Insets(20)
          alignment = Pos.Center
          children = Seq(new Label("Settings View Placeholder"))
        })
      }), 2, 1)
    }

    val wrapper = new VBox(40) {
      alignment = Pos.Center
      children = Seq(title, grid)
    }

    val outer = new StackPane {
      padding = Insets(50)
      alignment = Pos.Center
      children = wrapper
    }

    stage.scene = new Scene(1000, 700) {
      root = outer
    }

    stage.title = "Nutrition Dashboard"
  }
}
