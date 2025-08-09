package nutritionapp

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text._
import scalafx.stage.{Screen, Stage}

object LoginView {

  def show(stage: Stage): Unit = {
    val subtitle = new Label("Please enter your details") {
      font = Font.font("System", FontWeight.Normal, 12)
      textFill = Color.Gray
    }

    val title = new Label("Welcome back") {
      font = Font.font("System", FontWeight.Bold, 20)
      textFill = Color.Black
    }

    val messageLabel = new Label("") {
      textFill = Color.Red
      font = Font.font("System", FontWeight.Normal, 11)
    }

    def styledField(placeholder: String): TextField = new TextField {
      promptText = placeholder
      maxWidth = 280
      style =
        """-fx-background-radius: 8;
          |-fx-padding: 8;
          |-fx-font-size: 13;
          |-fx-background-color: white;
          |-fx-border-color: #CCCCCC;
          |-fx-border-radius: 8;
        """.stripMargin
      focusTraversable = false
    }

    val emailField = styledField("Email")

    val passwordField = new PasswordField {
      promptText = "Password"
      maxWidth = 280
      style =
        """-fx-background-radius: 8;
          |-fx-padding: 8;
          |-fx-font-size: 13;
          |-fx-background-color: white;
          |-fx-border-color: #CCCCCC;
          |-fx-border-radius: 8;
        """.stripMargin
      focusTraversable = false
    }

    val loginButton = new Button("Log in") {
      maxWidth = 280
      style =
        """-fx-background-color: #6DBE75;
          |-fx-text-fill: white;
          |-fx-font-size: 14;
          |-fx-font-weight: bold;
          |-fx-background-radius: 8;
          |-fx-padding: 10 0;
          |-fx-cursor: hand;
        """.stripMargin

      onAction = _ => {
        val email = emailField.text.value.trim
        val password = passwordField.text.value.trim

        if (email.isEmpty || password.isEmpty) {
          messageLabel.text = "Please fill in all fields."
        } else {
          AuthManager.findUserByEmail(email) match {
            case Some(user) if user.password == password =>
              SessionManager.saveSession(user.email)
              DashboardView.show(stage, user)
            case Some(_) =>
              messageLabel.text = "Incorrect password."
            case None =>
              messageLabel.text = "User not found."
          }
        }
      }
    }

    val signUpPrompt = new Label("Don’t have an account?") {
      font = Font.font("System", FontWeight.Normal, 11)
      textFill = Color.Gray
    }

    val signUpLink = new Label("Sign up") {
      font = Font.font("System", FontWeight.Bold, 11)
      textFill = Color.web("#6DBE75")
      style = "-fx-cursor: hand;"
      onMouseClicked = _ => SignUpView.show(stage)
    }

    val signUpRow = new HBox {
      spacing = 4
      alignment = Pos.Center
      children = Seq(signUpPrompt, signUpLink)
    }

    val formLayout = new VBox {
      spacing = 12
      alignment = Pos.Center
      padding = Insets(30, 20, 30, 20)
      children = Seq(
        title, subtitle,
        emailField, passwordField,
        messageLabel,
        loginButton, signUpRow
      )
    }

    val rootLayout = new StackPane {
      alignment = Pos.Center
      style = "-fx-background-color: #E6FAF2;"
      children = Seq(formLayout)
    }

    val scene = new Scene {
      root = rootLayout
    }

    val screenBounds = Screen.primary.visualBounds
    stage.x = screenBounds.minX
    stage.y = screenBounds.minY
    stage.width = screenBounds.width
    stage.height = screenBounds.height
    stage.resizable = true

    stage.title = "Login - Nutrition App"
    stage.scene = scene
    stage.show()
  }
}
