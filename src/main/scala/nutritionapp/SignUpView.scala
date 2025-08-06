package nutritionapp

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.stage.Stage
import nutritionapp.model.User

object SignUpView {

  def show(stage: Stage): Unit = {
    val nameField = new TextField() { promptText = "Name" }
    val emailField = new TextField() { promptText = "Email" }
    val passwordField = new PasswordField() { promptText = "Password" }
    val ageSpinner = new Spinner[Int](10, 100, 25)
    val heightField = new TextField() { promptText = "Height (cm)" }

    val activityLevelBox = new ComboBox(Seq("Sedentary", "Light", "Moderate", "Active", "Very Active")) {
      promptText = "Activity Level"
    }

    val goalBox = new ComboBox(Seq("Lose Weight", "Maintain", "Gain Muscle")) {
      promptText = "Nutrition Goal"
    }

    val signUpButton = new Button("Sign Up")

    // === Sign-Up Button Logic ===
    signUpButton.onAction = _ => {
      val name = nameField.text.value.trim
      val email = emailField.text.value.trim
      val password = passwordField.text.value.trim
      val age = ageSpinner.getValue
      val height = heightField.text.value.trim
      val activityLevel = activityLevelBox.value.value
      val goal = goalBox.value.value

      // Input validation
      if (name.isEmpty || email.isEmpty || password.isEmpty || height.isEmpty || activityLevel == null || goal == null) {
        new Alert(Alert.AlertType.Error) {
          title = "Missing Information"
          contentText = "Please fill out all fields."
        }.showAndWait()
      } else {
        try {
          val user = User(
            name = name,
            email = email,
            password = password,
            age = age,
            height = height.toDouble,
            activityLevel = activityLevel,
            goal = goal
          )

          if (AuthManager.registerUser(user)) {
            new Alert(Alert.AlertType.Information) {
              title = "Success"
              contentText = "User registered successfully!"
            }.showAndWait()
          } else {
            new Alert(Alert.AlertType.Warning) {
              title = "Email Already Used"
              contentText = "This email is already registered."
            }.showAndWait()
          }
        } catch {
          case _: NumberFormatException =>
            new Alert(Alert.AlertType.Error) {
              title = "Invalid Input"
              contentText = "Height must be a valid number."
            }.showAndWait()
        }
      }
    }

    val form = new VBox(10) {
      padding = Insets(20)
      alignment = Pos.Center
      children = Seq(
        new Label("Sign Up"),
        nameField,
        emailField,
        passwordField,
        ageSpinner,
        heightField,
        activityLevelBox,
        goalBox,
        signUpButton
      )
    }

    stage.scene = new Scene(400, 500) {
      root = form
    }
    stage.title = "Sign Up"
    stage.show()
  }
}
