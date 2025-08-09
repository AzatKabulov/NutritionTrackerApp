package nutritionapp

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.stage.Stage
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.StringProperty

object SettingsView {

  private val currentTheme = StringProperty("Light") // Placeholder theme tracker

  def show(stage: Stage): Unit = {
    val guideButton = new Button("📖 How to Use") {
      style = "-fx-font-size: 14px; -fx-background-color: #eeeeee; -fx-pref-width: 200px;"
    }

    val themeToggle = new ToggleButton("Dark Mode") {
      style = "-fx-font-size: 14px;"
      selected = false
    }

    val logoutButton = new Button("🚪 Logout") {
      style = "-fx-font-size: 16px; -fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-pref-width: 200px;"
    }

    val backButton = new Button("⬅ Back to Dashboard") {
      style = "-fx-font-size: 14px; -fx-background-color: #dddddd; -fx-pref-width: 200px;"
    }

    //  Guide
    guideButton.onAction = _ => {
      new Alert(Alert.AlertType.Information) {
        title = "How to Use"
        headerText = "Quick Guide"
        contentText =
          """📖 Features:
            |
            |• 🧮 Planner – Add foods & meals by date and time.
            |• 🥗 Food – Manage single food items with macros.
            |• 🍽 Meals – Create combos of foods.
            |• 👤 Profile – Set height, weight, goals.
            |• ⚙ Settings – Logout, theme toggle, help.
          """.stripMargin
      }.showAndWait()
    }

    //  Theme toggle placeholder
    themeToggle.onAction = _ => {
      if (themeToggle.isSelected) {
        currentTheme.value = "Dark"
        themeToggle.text = "Light Mode"
        println("🌙 Theme switching not implemented yet")
      } else {
        currentTheme.value = "Light"
        themeToggle.text = "Dark Mode"
        println("☀️ Theme switching not implemented yet")
      }
    }

    //  Logout
    logoutButton.onAction = _ => {
      SessionManager.clearSession()
      LoginView.show(stage)
    }

    //  Back to Dashboard
    backButton.onAction = _ => {
      val userOpt = SessionManager.loadSessionEmail().flatMap(email =>
        AuthManager.loadUsers().find(_.email == email)
      )

      userOpt match {
        case Some(user) => DashboardView.show(stage, user)
        case None =>
          new Alert(Alert.AlertType.Error) {
            title = "Error"
            contentText = "Could not load session. Please log in again."
          }.showAndWait()
          LoginView.show(stage)
      }
    }

    val layout = new VBox(16) {
      padding = Insets(30)
      alignment = Pos.Center
      children = Seq(
        new Label("⚙️ Settings") {
          style = "-fx-font-size: 24px;"
        },
        guideButton,
        themeToggle,
        logoutButton,
        backButton
      )
    }

    stage.scene = new Scene(400, 350) {
      root = layout
    }

    stage.title = "Settings"
  }
}
