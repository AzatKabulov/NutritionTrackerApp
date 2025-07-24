import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.stage.Stage
import scalafx.geometry.Insets

object NutritionTrackerApp extends JFXApp3 {

  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "Nutrition Tracker"  // Title of the application window
      width = 600                  // Window width
      height = 400                 // Window height

      scene = new Scene {
        root = new BorderPane {
          padding = Insets(10)     // Padding around the main layout

          // Top menu bar with File and View options
          top = new MenuBar {
            menus = List(
              new Menu("File") {
                items = List(
                  new MenuItem("Export Report"),     // (To be implemented)
                  new SeparatorMenuItem(),
                  new MenuItem("Exit")               // (To be implemented)
                )
              },
              new Menu("View") {
                items = List(
                  new MenuItem("View Foods"),        // (To switch to food view)
                  new MenuItem("View Meals"),        // (To switch to meal view)
                  new MenuItem("View History")       // (To switch to history view)
                )
              }
            )
          }

          // Placeholder label in the center of the window
          center = new Label("Welcome to Nutrition Tracker!") {
            style = "-fx-font-size: 16pt"  // Bigger font for welcome text
          }
        }
      }
    }
  }
}
