package nutritionapp

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.stage.Stage
import nutritionapp.model.User

object LoginView {

  def show(stage: Stage): Unit = {
    val emailField = new TextField() { promptText = "Email" }
    val passwordField = new PasswordField() { promptText = "Password" }
    val loginButton = new Button("Login")

    loginButton.onAction = _ => {
      val email = emailField.text.value.trim
      val password = passwordField.text.value.trim

      if (email.isEmpty || password.isEmpty) {
        new Alert(Alert.AlertType.Error) {
          title = "Missing Information"
          contentText = "Please enter both email and password."
        }.showAndWait()
      } else {
        println("🔍 Attempting login...")
        val users = AuthManager.loadUsers()
        println(s"📦 Found ${users.length} user(s)")

        val userOpt = users.find(u => u.email == email && u.password == password)

        userOpt match {
          case Some(user) =>
            println(s"✅ Login successful for ${user.email}")
            SessionManager.saveSession(user.email)
            // Switch scene to Dashboard
            DashboardView.show(stage, user)

          case None =>
            println("❌ Login failed: invalid credentials")
            new Alert(Alert.AlertType.Error) {
              title = "Login Failed"
              contentText = "Invalid email or password."
            }.showAndWait()
        }
      }
    }

    val form = new VBox(10) {
      padding = Insets(20)
      alignment = Pos.Center
      children = Seq(
        new Label("Login"),
        emailField,
        passwordField,
        loginButton
      )
    }

    stage.scene = new Scene(400, 300) {
      root = form
    }
    stage.title = "Login"
    stage.show()
  }
}
