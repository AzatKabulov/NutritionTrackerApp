// Represents a single food item like "Egg" or "Apple"
class Food (
  val name: String,
  val calories: Double,
  val protein: Double,
  val fat: Double,
  val carbs: Double,
) extends NutritionInfo {
  override def printSummary(): Unit = {
    println(s"[Food] $name → $calories kcal | P: $protein g | F: $fat g | C: $carbs g")
  }
}
