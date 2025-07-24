class NutritionReport(items: List[NutritionInfo]) {

  def generateSummary(): Unit = {
    // Calculate totals
    val totalCalories = items.map(_.calories).sum
    val totalProtein  = items.map(_.protein).sum
    val totalFat      = items.map(_.fat).sum
    val totalCarbs    = items.map(_.carbs).sum

    // Print report
    println("====== Nutrition Summary ======")
    println(f"Total Calories: $totalCalories%.2f kcal")
    println(f"Protein: $totalProtein%.2f g | Fat: $totalFat%.2f g | Carbs: $totalCarbs%.2f g")
    println("\nItems included:")
    items.foreach(i => println(s"- ${i.name}"))
  }
}
