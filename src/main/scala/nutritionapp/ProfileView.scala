package nutritionapp

import scalafx.application.Platform
import scalafx.geometry.{HPos, Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.control.ContentDisplay
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text._
import scalafx.stage.Stage
import nutritionapp.model.User

object ProfileView {

  private val BG_GRADIENT  = "linear-gradient(to bottom right, #DBF5EA, #E8FBF3)"
  private val INK          = "#0D3B2F"
  private val SUBTLE_INK   = "#2F6D5F"
  private val PRIMARY      = "#6DBE75"
  private val BORDER_COL   = "#E6F2EC"
  private val FIELD_BORDER = "rgba(207,233,222,0.95)"

  def show(stage: Stage, user: User): Unit = {
    val wasMax = stage.isMaximized
    val prevX = stage.getX; val prevY = stage.getY
    val prevW = stage.getWidth; val prevH = stage.getHeight

    def icon(name: String, size: Int = 20): Option[ImageView] =
      Option(getClass.getResourceAsStream(s"/images/$name")).map { s =>
        new ImageView(new Image(s)) { fitWidth = size; fitHeight = size; preserveRatio = true }
      }

    def capsuleField(placeholder: String, initial: String = ""): TextField = new TextField {
      promptText = placeholder
      text = initial
      maxWidth = 560
      style =
        s"""-fx-background-color: #FFFFFF;
           |-fx-background-radius: 16;
           |-fx-border-radius: 16;
           |-fx-border-color: $FIELD_BORDER;
           |-fx-border-width: 1.5;
           |-fx-padding: 12;
           |-fx-font-size: 14;""".stripMargin
    }

    def capsuleCombo(items: Seq[String], default: String) = new ComboBox[String](items) {
      value = default
      maxWidth = 560
      style =
        s"""-fx-background-color: #FFFFFF;
           |-fx-background-radius: 16;
           |-fx-border-radius: 16;
           |-fx-border-color: $FIELD_BORDER;
           |-fx-border-width: 1.5;
           |-fx-padding: 6;""".stripMargin
    }

    val nameField           = capsuleField("Name", user.name)
    val ageField            = capsuleField("Age")
    val heightField         = capsuleField("Height (cm)")
    val weightField         = capsuleField("Weight (kg)")
    val targetCaloriesField = capsuleField("Target Calories")
    val activityBox         = capsuleCombo(Seq("Sedentary","Light","Moderate","Active","Very Active"), "Moderate")
    val goalBox             = capsuleCombo(Seq("Maintain","Gain Muscle","Lose Fat"), "Gain Muscle")

    val avatarInitial = user.name.trim.headOption.map(_.toUpper).getOrElse('A').toString
    val avatarBadge = new StackPane {
      minWidth = 72; minHeight = 72; maxWidth = 72; maxHeight = 72
      style = s"-fx-background-color: $PRIMARY; -fx-background-radius: 999; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 10, 0.35, 0, 3);"
      children = new Label(avatarInitial) {
        font = Font.font("Segoe UI", FontWeight.Bold, 22)
        textFill = Color.White
      }
    }

    val titleLbl = new Label("Edit Profile") {
      font = Font.font("Segoe UI", FontWeight.Bold, 28)
      textFill = Color.web(INK)
    }
    val subtitleLbl = new Label("Update your personal details") {
      font = Font.font("Segoe UI", FontWeight.Normal, 14)
      textFill = Color.web(SUBTLE_INK)
    }
    val titleBlock = new VBox(6) { alignment = Pos.Center; children = Seq(titleLbl, subtitleLbl) }
    val form = new GridPane {
      alignment = Pos.Center
      hgap = 20; vgap = 16
      padding = Insets(10, 0, 0, 0)
      columnConstraints = Seq(
        new ColumnConstraints { prefWidth = 210; halignment = HPos.Right },
        new ColumnConstraints { minWidth = 600; hgrow = Priority.Never }
      )
      def label(t: String) = new Label(t) {
        textFill = Color.web(SUBTLE_INK)
        font = Font.font("Segoe UI", FontWeight.SemiBold, 15)
      }
      add(label("Name:"),            0, 0); add(nameField,           1, 0)
      add(label("Age:"),             0, 1); add(ageField,            1, 1)
      add(label("Height (cm):"),     0, 2); add(heightField,         1, 2)
      add(label("Weight (kg):"),     0, 3); add(weightField,         1, 3)
      add(label("Target Calories:"), 0, 4); add(targetCaloriesField, 1, 4)
      add(label("Activity Level:"),  0, 5); add(activityBox,         1, 5)
      add(label("Goal:"),            0, 6); add(goalBox,             1, 6)
    }

    def primaryBtn(text0: String): Button = {
      val b = new Button(text0) {
        style =
          s"""-fx-background-color: $PRIMARY;
             |-fx-text-fill: white;
             |-fx-font-size: 16;
             |-fx-font-weight: 800;
             |-fx-background-radius: 16;
             |-fx-padding: 14 26;
             |-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.16), 12, 0.35, 0, 3);""".stripMargin
        contentDisplay = ContentDisplay.Left
        graphicTextGap = 10
        onMouseEntered = _ => { scaleX = 1.02; scaleY = 1.02 }
        onMouseExited  = _ => { scaleX = 1.00; scaleY = 1.00 }
      }
      val maybe = icon("save.png")
      maybe.foreach(iv => b.graphic = iv)
      if (maybe.isEmpty) b.text = "💾  " + text0
      b
    }

    def outlineBtn(text0: String): Button = {
      val b = new Button(text0) {
        style =
          s"""-fx-background-color: transparent;
             |-fx-text-fill: $SUBTLE_INK;
             |-fx-font-size: 16;
             |-fx-font-weight: 800;
             |-fx-background-radius: 16;
             |-fx-border-radius: 16;
             |-fx-border-color: $BORDER_COL;
             |-fx-border-width: 2;
             |-fx-padding: 14 26;""".stripMargin
        contentDisplay = ContentDisplay.Left
        graphicTextGap = 10
        onMouseEntered = _ => { scaleX = 1.02; scaleY = 1.02 }
        onMouseExited  = _ => { scaleX = 1.00; scaleY = 1.00 }
      }
      val maybe = icon("arrow-left.png")
      maybe.foreach(iv => b.graphic = iv)
      if (maybe.isEmpty) b.text = "←  " + text0
      b
    }

    val saveBtn = primaryBtn("Save")
    val backBtn = outlineBtn("Back to Dashboard")
    val buttons = new HBox(18) { alignment = Pos.Center; padding = Insets(18, 0, 0, 0); children = Seq(saveBtn, backBtn) }

    val card = new VBox(22) {
      alignment = Pos.TopCenter
      padding = Insets(48, 28, 34, 28)
      maxWidth = 980
      children = Seq(titleBlock, form, buttons)
      style =
        s"""-fx-background-color: #FFFFFF;
           |-fx-background-radius: 24;
           |-fx-border-radius: 24;
           |-fx-border-color: $BORDER_COL;
           |-fx-border-width: 1.4;
           |-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 28, 0.35, 0, 12);""".stripMargin
    }

    val cardStack = new StackPane { alignment = Pos.TopCenter; children = Seq(card, avatarBadge) }
    StackPane.setMargin(avatarBadge, Insets(-36, 0, 0, 0))

    val rootContainer = new StackPane {
      alignment = Pos.Center
      padding = Insets(40)
      children = Seq(cardStack)
      style = s"-fx-background-color: $BG_GRADIENT;"
    }

    saveBtn.onAction = _ => DashboardView.show(stage, user)
    backBtn.onAction = _ => DashboardView.show(stage, user)

    stage.scene = new Scene { root = rootContainer }
    Platform.runLater {
      if (wasMax) stage.setMaximized(true)
      else {
        stage.setX(prevX); stage.setY(prevY)
        stage.setWidth(prevW); stage.setHeight(prevH)
      }
    }
    stage.title = "Edit Profile"
  }
}