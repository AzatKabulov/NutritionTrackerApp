// Meal class represents a group of Food items
class Meal (val name: String, val items: List [Food]) extends NutritionInfo {

  // Total calories of all items
  val calories: Double = items.map(_.calories).sum

  // Total protein (in grams)
  val protein: Double = items.map(_.protein).sum
  // fat protein (in grams)
  val fat: Double = items.map(_.fat).sum
  // Total carbs (in grams)
  val carbs: Double = items.map(_.carbs).sum

  // This method prints a summary of the meal and its food items
  override def printSummary(): Unit = {
    println(s"[Meal] $name → $calories kcal | P: $protein g | F: $fat g | C: $carbs g")
    
    // List each food item included in the meal
    items.foreach(i => println(s"   - ${i.name}"))
  }
}



}
