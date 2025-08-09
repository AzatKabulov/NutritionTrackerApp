package nutritionapp.model

case class PlannerItem(
                        name: String,
                        source: String, // "Food" or "Meal"
                        calories: Double,
                        protein: Double,
                        carbs: Double,
                        fats: Double,
                        mealType: String
                      )

object PlannerItem {
  def fromFood(food: Food, mealType: String): PlannerItem = {
    PlannerItem(
      name = food.name,
      source = "Food",
      calories = food.calories,
      protein = food.protein,
      carbs = food.carbs,
      fats = food.fats,
      mealType = mealType
    )
  }

  def fromMeal(meal: Meal, mealType: String): PlannerItem = {
    PlannerItem(
      name = meal.name,
      source = "Meal",
      calories = meal.calories,
      protein = meal.protein,
      carbs = meal.carbs,
      fats = meal.fats,
      mealType = mealType
    )
  }
}
