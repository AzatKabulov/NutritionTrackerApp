package nutritionapp.model

case class Food(
                 name: String,
                 calories: Double,
                 protein: Double,
                 carbs: Double,
                 fats: Double,
                 category: String = "",
                 imagePath: String = "",
                 fiber: Double = 0.0
               ) {
  override def toString: String = name
}
