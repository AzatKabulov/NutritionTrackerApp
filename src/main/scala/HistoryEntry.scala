import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// This class logs any Food or Meal entry along with a timestamp
// Takes any item that extends NutritionInfo (e.g., Food or Meal)
case class HistoryEntry(item: NutritionInfo) {

  // Format the timestamp as "dd MMM yyyy HH:mm", e.g., "25 Jul 2025 16:40"
  val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))

  // Display string shown in the History tab
  def displayString: String = s"[$timestamp] ${item.name} - ${item.calories} kcal"
}
