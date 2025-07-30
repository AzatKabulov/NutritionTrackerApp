class Food(
            val name: String,
            val category: String,
            val portion: String,
            val calories: Double,
            val protein: Double,
            val fat: Double,
            val carbs: Double,
            val grams: Double
          ) extends NutritionInfo {

  override def printSummary(): String = {
    f"$name ($portion) -> ${calories}%.0f kcal | Protein: ${protein}%.1f g | Fat: ${fat}%.1f g | Carbs: ${carbs}%.1f g"
  }
}
