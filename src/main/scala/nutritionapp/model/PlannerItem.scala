package nutritionapp.model

case class PlannerItem(
                        name: String,
                        source: String, // "Food" or "Meal"
                        calories: Double,
                        protein: Double,
                        carbs: Double,
                        fats: Double,
                        mealType: String // NEW
                      )
