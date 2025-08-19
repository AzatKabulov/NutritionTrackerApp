package nutritionapp

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text._
import scalafx.stage.{Screen, Stage}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.Includes._

object LoginView {

  // Wrap a password field with a right-aligned eye icon (using PNGs)
  private def passwordWithToggle(placeholder: String, prefW: Double, styleStr: String)
  : (StackPane, () => String, PasswordField, TextField, Button) = {

    val pass  = new PasswordField { promptText = placeholder; prefWidth = prefW; maxWidth = prefW; style = styleStr; focusTraversable = false }
    val plain = new TextField     { promptText = placeholder; prefWidth = prefW; maxWidth = prefW; style = styleStr; visible = false; managed = false; focusTraversable = false }

    val eyeOpen  = new Image(getClass.getResource("/images/eye_open.png").toExternalForm)
    val eyeClose = new Image(getClass.getResource("/images/eye_closed.png").toExternalForm)

    val icon = new ImageView(eyeClose) {
      fitWidth = 20; fitHeight = 20; preserveRatio = true; smooth = true
      opacity = 0.85
    }

    val eyeBtn = new Button {
      graphic = icon
      focusTraversable = false
      style = "-fx-background-color: transparent; -fx-padding: 0 4 0 4; -fx-background-radius: 8;"
      onMouseEntered = _ => icon.opacity = 1.0
      onMouseExited  = _ => icon.opacity = 0.85
      onAction = _ => {
        val show = !plain.visible.value
        plain.visible = show; plain.managed = show
        pass.visible  = !show; pass.managed  = !show
        if (show) { plain.text = pass.text(); plain.requestFocus(); plain.positionCaret(plain.text.value.length); icon.image = eyeOpen }
        else      { pass.text  = plain.text(); pass.requestFocus();  pass.positionCaret(pass.text.value.length);  icon.image = eyeClose }
      }
    }

    // Overlay the eye at the far right inside the field
    val overlay = new HBox {
      alignment = Pos.CenterRight
      padding = Insets(0, 10, 0, 0)
      children = Seq(eyeBtn)
      pickOnBounds = false
    }

    val container = new StackPane { children = Seq(pass, plain, overlay) }
    StackPane.setAlignment(overlay, Pos.CenterRight)
    StackPane.setMargin(overlay, Insets(0, 10, 0, 0))

    val getter = () => if (plain.visible.value) plain.text.value else pass.text.value
    (container, getter, pass, plain, eyeBtn)
  }

  def show(stage: Stage): Unit = {
    val CardWidth   = 480.0
    val FieldWidth  = 360.0
    val TitleSize   = 24.0
    val SubTitleSz  = 13.0
    val FieldFontSz = 14.0
    val BtnFontSz   = 15.0

    val subtitle = new Label("Please enter your details") {
      font = Font.font("System", FontWeight.Normal, SubTitleSz)
      textFill = Color.Gray
    }
    val title = new Label("Welcome back") {
      font = Font.font("System", FontWeight.Bold, TitleSize)
      textFill = Color.Black
    }
    val messageLabel = new Label("") {
      textFill = Color.Red
      font = Font.font("System", FontWeight.Normal, 12)
    }

    def styledField(placeholder: String): TextField = new TextField {
      promptText = placeholder
      prefWidth = FieldWidth
      maxWidth  = FieldWidth
      style =
        s"""-fx-background-radius: 10;
           |-fx-padding: 12;
           |-fx-font-size: ${FieldFontSz.toInt};
           |-fx-background-color: white;
           |-fx-border-color: #CCCCCC;
           |-fx-border-radius: 10;""".stripMargin
      focusTraversable = false
    }

    val emailField = styledField("Email")

    val (passwordNode, getPassword, passMasked, passPlain, _) =
      passwordWithToggle(
        placeholder = "Password",
        prefW = FieldWidth,
        styleStr =
          s"""-fx-background-radius: 10;
             |-fx-padding: 12;
             |-fx-font-size: ${FieldFontSz.toInt};
             |-fx-background-color: white;
             |-fx-border-color: #CCCCCC;
             |-fx-border-radius: 10;""".stripMargin
      )

    val loginButton = new Button("Log in") {
      prefWidth = FieldWidth
      maxWidth  = FieldWidth
      style =
        s"""-fx-background-color: #6DBE75;
           |-fx-text-fill: white;
           |-fx-font-size: ${BtnFontSz.toInt};
           |-fx-font-weight: bold;
           |-fx-background-radius: 10;
           |-fx-padding: 12 0;
           |-fx-cursor: hand;""".stripMargin

      onAction = _ => {
        val email    = emailField.text.value.trim
        val password = getPassword().trim

        if (email.isEmpty || password.isEmpty) {
          messageLabel.text = "Please fill in all fields."
        } else {
          AuthManager.findUserByEmail(email) match {
            case Some(user) if user.password == password =>
              // Persist session and scope planner to this user
              SessionManager.saveSessionEmail(user.email.toLowerCase)
              MealPlanner.setCurrentUser(user.email)
              MealPlanner.loadAllForCurrentUser() // optional: pre-load cached files
              DashboardView.show(stage, user)

            case Some(_) =>
              messageLabel.text = "Incorrect password."
            case None =>
              messageLabel.text = "User not found."
          }
        }
      }
    }

    passMasked.onAction = _ => loginButton.fire()
    passPlain .onAction = _ => loginButton.fire()

    val signUpPrompt = new Label("Don’t have an account?") {
      font = Font.font("System", FontWeight.Normal, 12); textFill = Color.Gray
    }
    val signUpLink = new Label("Sign up") {
      font = Font.font("System", FontWeight.Bold, 12); textFill = Color.web("#6DBE75")
      style = "-fx-cursor: hand;"; onMouseClicked = _ => SignUpView.show(stage)
    }
    val signUpRow = new HBox { spacing = 6; alignment = Pos.Center; children = Seq(signUpPrompt, signUpLink) }

    val formLayout = new VBox {
      spacing = 14
      alignment = Pos.Center
      padding = Insets(26, 24, 26, 24)
      children = Seq(title, subtitle, emailField, passwordNode, messageLabel, loginButton, signUpRow)
    }

    val card = new VBox {
      alignment = Pos.Center
      padding = Insets(22, 30, 26, 30)
      prefWidth = CardWidth
      maxWidth  = CardWidth
      maxHeight = Region.USE_PREF_SIZE
      style =
        """-fx-background-color: white;
          |-fx-background-radius: 16;
          |-fx-border-radius: 16;
          |-fx-border-color: rgba(0,0,0,0.04);""".stripMargin
      effect = new DropShadow { radius = 20; offsetX = 0; offsetY = 3; color = Color.web("#0D3B2F", 0.16) }
      children = Seq(formLayout)
      translateY = -10
    }

    val rootLayout = new StackPane { alignment = Pos.Center; style = "-fx-background-color: #E6FAF2;"; children = Seq(card) }

    stage.title = "Login - Nutrition App"
    Nav.go(stage, rootLayout) // ← applies fullscreen-by-default or last-remembered window size
  }
}
