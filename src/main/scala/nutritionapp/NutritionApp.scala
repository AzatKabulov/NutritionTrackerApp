package nutritionapp

import scalafx.application.JFXApp3
import scalafx.stage.Stage

object NutritionApp extends JFXApp3 {
  override def start(): Unit = {
    val primaryStage = new JFXApp3.PrimaryStage()

    // ✅ Try to load previous session
    SessionManager.loadSessionEmail() match {
      case Some(email) =>
        val userOpt = AuthManager.loadUsers().find(_.email == email)
        userOpt match {
          case Some(user) =>
            println(s"Auto-login as $email")
            DashboardView.show(primaryStage, user)
          case None =>
            println("Session email not found in user list.")
            LoginView.show(primaryStage)
        }
      case None =>
        println("No active session, showing login.")
        LoginView.show(primaryStage)
    }
  }
}
