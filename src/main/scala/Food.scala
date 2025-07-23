// Concrete class for a single food item "Egg" or "Apple"
// Inherits from the abstract class NutritionInfo
class Food (
  val name: String,
  val calories: Double,
  val protein: Double,
  val fat: Double,
  val carbs: Double,
) extends NutritionInfo {
  // Overrides the printSummary method to display details specifically for a single food item
  override def printSummary(): Unit = {
    println(s"[Food] $name → $calories kcal | P: $protein g | F: $fat g | C: $carbs g")
  }
}
