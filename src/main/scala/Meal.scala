class Meal(
            val name: String,
            val foods: List[Food]
          ) extends NutritionInfo {

  val calories: Double = foods.map(_.calories).sum
  val protein: Double = foods.map(_.protein).sum
  val fat: Double = foods.map(_.fat).sum
  val carbs: Double = foods.map(_.carbs).sum
  val grams: Double = foods.map(_.grams).sum

  override def printSummary(): String = {
    f"$name (${grams}%.0f g) -> ${calories}%.0f kcal | Protein: ${protein}%.1f g | Fat: ${fat}%.1f g | Carbs: ${carbs}%.1f g"
  }
}
