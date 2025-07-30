// This is an abstract class for all nutrition-related items like Food and Meal abstract class NutritionInfo
abstract class NutritionInfo {
  val name: String           // Name of the food or meal
  val calories: Double       // Total Calories
  val protein: Double        // Protein in grams
  val fat: Double            // Fat in grams
  val carbs: Double          // Carbs in grams

  //Print all the nutrition values
  def printSummary(): String = {
    f"$name → $calories%.1f kcal | Protein: $protein%.1f g | Fat: $fat%.1f g | Carbs: $carbs%.1f g"
  }
}