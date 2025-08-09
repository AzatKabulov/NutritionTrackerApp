package nutritionapp

import nutritionapp.model.User
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text._
import scalafx.stage.{Screen, Stage}

object SignUpView {

  def show(stage: Stage): Unit = {
    val title = new Label("Sign Up") {
      font = Font.font("System", FontWeight.Bold, 24)
      textFill = Color.web("#2B5742")
    }

    val subtitle = new Label("Please fill in the details below to create an account") {
      font = Font.font("System", FontWeight.Normal, 12)
      textFill = Color.Gray
    }

    val messageLabel = new Label("") {
      textFill = Color.Red
      font = Font.font("System", FontWeight.Normal, 11)
    }

    // === Styled Input ===
    def styledTextField(placeholder: String): TextField = new TextField {
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

    def styledSmallField(placeholder: String): TextField = new TextField {
      promptText = placeholder
      maxWidth = 135
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

    val nameField = styledTextField("Name")
    val emailField = styledTextField("Email")

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

    val ageField = styledSmallField("Age")
    val heightField = styledSmallField("Height (cm)")
    val weightField = styledTextField("Weight (kg)")

    val activityLevelBox = new ComboBox[String](Seq("Low", "Medium", "High")) {
      promptText = "Activity Level"
      maxWidth = 135
      style =
        """-fx-background-radius: 8;
          |-fx-border-radius: 8;
        """.stripMargin
      focusTraversable = false
    }

    val goalBox = new ComboBox[String](Seq("Lose Weight", "Maintain", "Gain Muscle")) {
      promptText = "Goal"
      maxWidth = 135
      style =
        """-fx-background-radius: 8;
          |-fx-border-radius: 8;
        """.stripMargin
      focusTraversable = false
    }

    val signUpButton = new Button("Sign Up") {
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
        val name = nameField.text.value.trim
        val email = emailField.text.value.trim
        val password = passwordField.text.value.trim
        val age = ageField.text.value.trim
        val height = heightField.text.value.trim
        val weight = weightField.text.value.trim
        val activityLevel = activityLevelBox.value.value
        val goal = goalBox.value.value

        if (Seq(name, email, password, age, height, weight, activityLevel, goal).exists(_.isEmpty)) {
          messageLabel.text = "Please fill in all fields."
        } else {
          try {
            val user = User(
              name = name,
              email = email,
              password = password,
              age = age.toInt,
              height = height.toDouble,
              activityLevel = activityLevel,
              goal = goal,
              weight = weight.toDouble,
              targetCalories = None
            )

            if (AuthManager.registerUser(user)) {
              SessionManager.saveSession(email)
              DashboardView.show(stage, user)
            } else {
              messageLabel.text = "User with this email already exists."
            }
          } catch {
            case _: NumberFormatException =>
              messageLabel.text = "Invalid number in age, height, or weight."
          }
        }
      }
    }

    val backToLogin = new Label("← Back to login") {
      font = Font.font("System", FontWeight.Normal, 11)
      textFill = Color.web("#2B5742")
      style = "-fx-cursor: hand;"
      onMouseClicked = _ => LoginView.show(stage)
    }

    // Two-field row helper
    def row(left: scalafx.scene.Node, right: scalafx.scene.Node): HBox = new HBox {
      spacing = 10
      alignment = Pos.Center
      children = Seq(left, right)
    }

    val ageHeightRow = row(ageField, heightField)
    val goalActivityRow = row(activityLevelBox, goalBox)

    // Main form layout
    val formLayout = new VBox {
      spacing = 12
      alignment = Pos.Center
      padding = Insets(30, 20, 30, 20)
      children = Seq(
        title, subtitle,
        nameField, emailField, passwordField,
        ageHeightRow, weightField, goalActivityRow,
        messageLabel, signUpButton, backToLogin
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

    stage.title = "Sign Up - Nutrition App"
    stage.scene = scene
    stage.show()
  }
}
