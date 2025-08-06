package nutritionapp.model

case class Meal(
                 name: String,
                 foods: Seq[Food],
                 category: String = "",
                 imagePath: String = "",
                 servingSize: String = ""
               ) {
  def totalCalories: Double = foods.map(_.calories).sum
  def totalProtein: Double = foods.map(_.protein).sum
  def totalCarbs: Double = foods.map(_.carbs).sum
  def totalFats: Double = foods.map(_.fats).sum
  def totalFiber: Double = foods.map(_.fiber).sum

  // Extra aliases for UI (so you can write meal.calories directly)
  def calories: Double = totalCalories
  def protein: Double = totalProtein
  def carbs: Double = totalCarbs
  def fats: Double = totalFats

  override def toString: String = name
}
