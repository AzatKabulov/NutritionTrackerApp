package nutritionapp

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.Stage
import scalafx.scene.paint.Color
import scalafx.scene.effect.DropShadow
import scalafx.animation.ScaleTransition
import scalafx.util.Duration
import javafx.scene.input.{KeyCode => JfxKeyCode}
import nutritionapp.model.User

object DashboardView {

  private val BgGradient = "linear-gradient(to bottom right, #DFF7EF, #E6FAF2)"
  private val Ink = "#013B2D"
  private val SubtleInk = "#1A5E52"
  private val Primary = "#6DBE75"
  private val SurfaceGlass = "rgba(255,255,255,0.90)"
  private val BorderCol = "#BFE9DA"
  private val CardBg = "#FFFFFF"
  private val CardBorder = "#CDEFE4"
  private val CardShadow = 0.12

  def show(stage: Stage, user: User): Unit = {
    println("Loading DashboardView")

    val title = new Label(s"WELCOME, ${user.name.toUpperCase}!") {
      font = Font.font("Segoe UI", FontWeight.Bold, 32)
      textFill = Color.web(Ink)
    }

    def menuButton(title: String, imageFile: String)(onClick: () => Unit): VBox = {
      val img = new ImageView {
        val stream = Option(getClass.getResourceAsStream("/images/" + imageFile))
        image = stream.map(new Image(_)).getOrElse(new Image("https://via.placeholder.com/100"))
        fitWidth = 100;
        fitHeight = 100;
        preserveRatio = true
      }
      val label = new Label(title.toUpperCase) {
        font = Font.font("Segoe UI", FontWeight.Bold, 15)
        textFill = Color.web(SubtleInk)
      }
      val card = new VBox(14) {
        alignment = Pos.Center
        padding = Insets(20)
        children = Seq(img, label)
        prefWidth = 200;
        prefHeight = 200
        focusTraversable = true
        style =
          s"""-fx-background-color: $CardBg;
             |-fx-background-radius: 22;
             |-fx-border-radius: 22;
             |-fx-border-color: $CardBorder;
             |-fx-border-width: 1.25;
             |-fx-effect: dropshadow(gaussian, rgba(0,0,0,$CardShadow), 16, 0.32, 0, 4);""".stripMargin
      }

      Tooltip.install(card, new Tooltip(title.capitalize))

      val liftIn = new ScaleTransition(Duration(120), card) {
        toX = 1.05; toY = 1.05
      }
      val liftOut = new ScaleTransition(Duration(120), card) {
        toX = 1.00; toY = 1.00
      }
      card.onMouseEntered = _ => {
        liftOut.stop(); liftIn.play()
      }
      card.onMouseExited = _ => {
        liftIn.stop(); liftOut.play()
      }
      card.onMousePressed = _ => {
        card.translateY = 1; card.effect = new DropShadow(20, Color.web("#6DBE7533"))
      }
      card.onMouseReleased = _ => {
        card.translateY = 0; card.effect = null
      }
      card.onMouseClicked = _ => onClick()
      card.onKeyPressed = e => e.getCode match {
        case JfxKeyCode.ENTER | JfxKeyCode.SPACE => onClick()
        case _ => ()
      }
      card
    }

    val foodBtn = menuButton("Food", "food.png")(() => {
      Nav.go(stage, FoodView.create(stage, user))
    })
    val mealsBtn = menuButton("Meals", "meals.png")(() => {
      Nav.go(stage, MealView.create(stage, user))
    })
    val plannerBtn = menuButton("Planner", "planner.png")(() => {
      // ✅ Make sure planner is scoped to the logged-in user before opening it
      MealPlanner.setCurrentUser(user.email)
      Nav.go(stage, PlannerView.create(stage, user))
    })
    val profileBtn = menuButton("Profile", "profile.png")(() => {
      ProfileView.show(stage, user)
    })
    val settingsBtn = menuButton("Settings", "settings.png")(() => {
      SettingsView.show(stage)
    })

    val topRow = new HBox(28) {
      alignment = Pos.Center; children = Seq(foodBtn, mealsBtn, plannerBtn)
    }
    val bottomRow = new HBox(28) {
      alignment = Pos.Center; children = Seq(profileBtn, settingsBtn)
    }

    val dashboardCard = new VBox(30) {
      alignment = Pos.Center
      padding = Insets(36)
      maxWidth = 980
      children = Seq(title, topRow, bottomRow)
      style =
        s"""-fx-background-color: $SurfaceGlass;
           |-fx-background-radius: 26;
           |-fx-border-radius: 26;
           |-fx-border-color: $BorderCol;
           |-fx-border-width: 1;
           |-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 28, 0.35, 0, 12);""".stripMargin
    }

    val rootPane = new StackPane {
      padding = Insets(40)
      alignment = Pos.Center
      children = dashboardCard
      style = s"-fx-background-color: $BgGradient;"
    }

    stage.title = "Nutrition Dashboard"
    Nav.go(stage, rootPane) // ← single place that applies fullscreen/remembered size
  }

  extension (s: String) private def capitalize: String =
    if (s.nonEmpty) s.take(1).toUpperCase + s.drop(1) else s
}