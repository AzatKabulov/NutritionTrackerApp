package nutritionapp

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.Stage
import nutritionapp.dialog.HelpDialog

object SettingsView {

  private val currentTheme = StringProperty("Light")
  private val isDark       = BooleanProperty(false)

  // Unify light gradient to match the app
  private val LightVars =
    """-bg: linear-gradient(to bottom right, #F3FBF6, #EAF6EF);
      |-card: #FFFFFF;
      |-ink: #0D3B2F;
      |-subtle: #2F6D5F;
      |-primary: #6DBE75;
      |-border: #D9EEE4;
      |-danger: #FF6B6B;
      |-ghost: rgba(0,0,0,0.06);""".stripMargin

  private val DarkVars =
    """-bg: linear-gradient(to bottom right, #0F1E1A, #132B24);
      |-card: #121C19;
      |-ink: #EAF7F0;
      |-subtle: #B6E0CE;
      |-primary: #6BD190;
      |-border: #244A3F;
      |-danger: #E15B64;
      |-ghost: rgba(255,255,255,0.08);""".stripMargin

  private val CardStyle =
    """-fx-background-color: -card;
      |-fx-background-radius: 24;
      |-fx-border-color: -border;
      |-fx-border-radius: 24;
      |-fx-padding: 32;
      |-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 24, 0.22, 0, 10);""".stripMargin

  private val btnBase =
    """-fx-font-size: 15px;
      |-fx-font-weight: 700;
      |-fx-background-radius: 14;
      |-fx-padding: 12 18 12 18;
      |-fx-pref-width: 360;
      |-fx-cursor: hand;""".stripMargin

  private val OutlineBtn = btnBase + """
                                       |-fx-background-color: transparent;
                                       |-fx-border-color: -border;
                                       |-fx-border-width: 1;
                                       |-fx-text-fill: -ink;""".stripMargin
  private val GhostBtn = btnBase + """
                                     |-fx-background-color: -ghost;
                                     |-fx-text-fill: -ink;""".stripMargin
  private val DangerBtn = btnBase + """
                                      |-fx-background-color: -danger;
                                      |-fx-text-fill: white;""".stripMargin

  def show(stage: Stage): Unit = {
    val titleLabel = new Label("⚙ Settings") {
      font = Font.font("Segoe UI", FontWeight.Bold, 28)         // ← unified typography
      style = "-fx-text-fill: -ink;"
    }
    val subtitle = new Label("Personalize your experience") {
      font = Font.font("Segoe UI", FontWeight.SemiBold, 14)     // ← unified typography
      style = "-fx-text-fill: -subtle;"
    }

    val guideButton = new Button("📖 How to Use") { style = OutlineBtn }
    val themeToggle = new ToggleButton("🌙 Dark Mode") { style = GhostBtn; selected = false }
    val logoutButton = new Button("⏻ Logout") { style = DangerBtn }

    val backButton = new Button("⬅ Back to Dashboard") {
      style =
        """
          |-fx-background-color: transparent;
          |-fx-text-fill: -ink;
          |-fx-font-size: 16;
          |-fx-font-weight: 800;
          |-fx-background-radius: 16;
          |-fx-border-radius: 16;
          |-fx-border-color: -border;
          |-fx-border-width: 2;
          |-fx-padding: 14 26;
          |-fx-pref-width: 360;   /* keeps it aligned with other Settings buttons */
    """.stripMargin
    }

    guideButton.onAction = _ => HelpDialog.show(stage, isDark.value)

    logoutButton.onAction = _ => {
      SessionManager.clearSession()
      LoginView.show(stage)
    }

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

    val actions = new VBox(16) {
      alignment = Pos.Center
      children = Seq(
        guideButton,
        themeToggle,
        logoutButton,
        backButton
      )
    }

    val card = new VBox(16) {
      alignment = Pos.Center
      spacing = 10
      padding = Insets(12, 8, 6, 8)
      style = CardStyle
      minWidth = 560; prefWidth = 560; maxWidth = 560
      children = Seq(
        new VBox(6) {
          alignment = Pos.Center
          children = Seq(titleLabel, subtitle)
        },
        new Separator { style = "-fx-opacity: 0.35;" },
        actions
      )
    }

    val page = new StackPane {
      padding = Insets(32)
      style = LightVars + "; -fx-background-color: -bg;"
      children = Seq(
        new StackPane {
          alignment = Pos.Center
          children = Seq(card)
        }
      )
    }

    def applyTheme(dark: Boolean): Unit = {
      isDark.value = dark
      currentTheme.value = if (dark) "Dark" else "Light"
      page.style = (if (dark) DarkVars else LightVars) + "; -fx-background-color: -bg;"
      themeToggle.text = if (dark) "☀ Light Mode" else "🌙 Dark Mode"
    }

    // Show "preview only" popup when enabling dark mode
    themeToggle.selected.onChange((_, _, nowSel) => {
      applyTheme(nowSel)
      if (nowSel) {
        new Alert(Alert.AlertType.Information) {
          title = "Dark theme (preview)"
          headerText = None          // ← instead of: headerText = null
          contentText = "Dark theme is coming soon across the app. For now, you can preview it here in Settings."
        }.showAndWait()
      }
    })

    stage.title = "Settings"
    Nav.go(stage, page)
  }
}
