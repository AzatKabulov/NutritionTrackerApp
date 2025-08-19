package nutritionapp

import nutritionapp.model.User
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

object SignUpView {

  // Eye toggle using PNGs, aligned to the far right inside the field
  private def passwordWithToggle(placeholder: String, prefW: Double, styleStr: String)
  : (StackPane, () => String, PasswordField, TextField, Button) = {

    val pass  = new PasswordField { promptText = placeholder; prefWidth = prefW; maxWidth = prefW; style = styleStr; focusTraversable = false }
    val plain = new TextField     { promptText = placeholder; prefWidth = prefW; maxWidth = prefW; style = styleStr; visible = false; managed = false; focusTraversable = false }

    val eyeOpenUrl  = Option(getClass.getResource("/images/eye_open.png")).map(_.toExternalForm)
    val eyeCloseUrl = Option(getClass.getResource("/images/eye_closed.png")).map(_.toExternalForm)

    val icon = new ImageView(eyeCloseUrl.map(new Image(_)).getOrElse(new Image("https://via.placeholder.com/20"))) {
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
        if (show) { plain.text = pass.text(); plain.requestFocus(); plain.positionCaret(plain.text.value.length); eyeOpenUrl.foreach(u => icon.image = new Image(u)) }
        else      { pass.text  = plain.text(); pass.requestFocus();  pass.positionCaret(pass.text.value.length);  eyeCloseUrl.foreach(u => icon.image = new Image(u)) }
      }
    }

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

    // Match Login sizing
    val CardWidth   = 480.0
    val FieldWidth  = 360.0
    val SmallFieldW = 175.0
    val TitleSize   = 24.0
    val SubTitleSz  = 13.0
    val FieldFontSz = 14.0
    val BtnFontSz   = 15.0

    val fieldBaseStyle =
      s"""-fx-background-radius: 10;
         |-fx-padding: 12;
         |-fx-font-size: ${FieldFontSz.toInt};
         |-fx-background-color: white;
         |-fx-border-color: #CCCCCC;
         |-fx-border-radius: 10;""".stripMargin

    val fieldErrorStyle =
      s"""-fx-background-radius: 10;
         |-fx-padding: 12;
         |-fx-font-size: ${FieldFontSz.toInt};
         |-fx-background-color: white;
         |-fx-border-color: #E57373;
         |-fx-border-width: 1.5;
         |-fx-border-radius: 10;""".stripMargin

    def isValidEmail(s: String): Boolean =
      s.trim.matches("(?i)^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,63}$")

    val title = new Label("Sign Up") {
      font = Font.font("System", FontWeight.Bold, TitleSize)
      textFill = Color.Black
    }
    val subtitle = new Label("Please fill in the details below to create an account") {
      font = Font.font("System", FontWeight.Normal, SubTitleSz)
      textFill = Color.Gray
    }
    val messageLabel = new Label("") {
      textFill = Color.Red
      font = Font.font("System", FontWeight.Normal, 12)
    }

    def styledTextField(placeholder: String): TextField = new TextField {
      promptText = placeholder
      prefWidth = FieldWidth
      maxWidth  = FieldWidth
      style = fieldBaseStyle
      focusTraversable = false
    }
    def styledSmallField(placeholder: String): TextField = new TextField {
      promptText = placeholder
      prefWidth = SmallFieldW
      maxWidth  = SmallFieldW
      style = fieldBaseStyle
      focusTraversable = false
    }

    val nameField  = styledTextField("Name")
    val emailField = styledTextField("Email")
    val (passwordNode, getPassword, passMasked, passPlain, _) =
      passwordWithToggle("Password", FieldWidth, fieldBaseStyle)

    // Live email validation
    emailField.text.onChange { (_, _, now) =>
      val t  = Option(now).getOrElse("")
      val ok = t.trim.isEmpty || isValidEmail(t)
      emailField.style = if (ok) fieldBaseStyle else fieldErrorStyle
      if (!ok && t.nonEmpty) messageLabel.text = "Enter a valid email (e.g., name@example.com)"
      else if (messageLabel.text.value.startsWith("Enter a valid email")) messageLabel.text = ""
    }

    val ageField      = styledSmallField("Age")
    val heightField   = styledSmallField("Height (cm)")
    val weightField   = styledTextField("Weight (kg)")
    val activityLevelBox = new ComboBox[String](Seq("Low", "Medium", "High")) {
      promptText = "Activity level"
      prefWidth  = SmallFieldW; maxWidth = SmallFieldW
      style = """-fx-background-radius: 10; -fx-border-radius: 10;"""
      focusTraversable = false
    }
    val goalBox = new ComboBox[String](Seq("Lose Weight", "Maintain", "Gain Muscle")) {
      promptText = "Goal"
      prefWidth  = SmallFieldW; maxWidth = SmallFieldW
      style = """-fx-background-radius: 10; -fx-border-radius: 10;"""
      focusTraversable = false
    }

    val signUpButton = new Button("Sign Up") {
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
        val name     = nameField.text.value.trim
        val emailRaw = emailField.text.value.trim
        val email    = emailRaw.toLowerCase
        val password = getPassword().trim
        val age      = ageField.text.value.trim
        val height   = heightField.text.value.trim
        val weight   = weightField.text.value.trim
        val activityLevel = Option(activityLevelBox.value.value).getOrElse("")
        val goal          = Option(goalBox.value.value).getOrElse("")

        if (Seq(name, emailRaw, password, age, height, weight, activityLevel, goal).exists(_.isEmpty)) {
          messageLabel.text = "Please fill in all fields."
        } else if (!isValidEmail(emailRaw)) {
          messageLabel.text = "Please enter a valid email address."
          emailField.requestFocus(); emailField.style = fieldErrorStyle
        } else {
          try {
            val user = User(
              name = name, email = email, password = password,
              age = age.toInt, height = height.toDouble, activityLevel = activityLevel,
              goal = goal, weight = weight.toDouble, targetCalories = None
            )

            AuthManager.register(user) match {
              case Left(err) =>
                messageLabel.text = err
              case Right(saved) =>
                SessionManager.saveSessionEmail(saved.email)
                MealPlanner.setCurrentUser(saved.email)
                println(s"[SignUp] user saved; users.json -> ${AuthManager.usersFilePath}")
                DashboardView.show(stage, saved)
            }

          } catch {
            case _: NumberFormatException =>
              messageLabel.text = "Invalid number in age, height, or weight."
          }
        }
      }
    }

    passMasked.onAction = _ => signUpButton.fire()
    passPlain .onAction = _ => signUpButton.fire()

    val haveAccountPrompt = new Label("Already have an account?") {
      font = Font.font("System", FontWeight.Normal, 12); textFill = Color.Gray
    }
    val loginLink = new Label("Log in") {
      font = Font.font("System", FontWeight.Bold, 12); textFill = Color.web("#6DBE75")
      style = "-fx-cursor: hand;"; onMouseClicked = _ => LoginView.show(stage)
    }
    val linkRow = new HBox { spacing = 6; alignment = Pos.Center; children = Seq(haveAccountPrompt, loginLink) }

    def row(left: scalafx.scene.Node, right: scalafx.scene.Node): HBox =
      new HBox { spacing = 10; alignment = Pos.Center; children = Seq(left, right) }

    val ageHeightRow    = row(ageField, heightField)
    val goalActivityRow = row(activityLevelBox, goalBox)

    val formLayout = new VBox {
      spacing = 14
      alignment = Pos.Center
      padding = Insets(26, 24, 26, 24)
      children = Seq(
        title, subtitle,
        nameField, emailField, passwordNode,
        ageHeightRow, weightField, goalActivityRow,
        messageLabel, signUpButton, linkRow
      )
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

    stage.title = "Sign Up - Nutrition App"
    Nav.go(stage, rootLayout) // ← apply fullscreen-by-default or last-remembered size
  }
}
