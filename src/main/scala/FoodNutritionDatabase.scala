object FoodNutritionDatabase {
  val allFoods: List[Food] = List(
    new Food("Almonds", "Nuts", "28g", 164, 6.0, 14.0, 6.0, 28),
    new Food("Apple", "Fruits", "182g", 95, 0.3, 0.2, 25.0, 182),
    new Food("Avocado", "Fruits", "100g", 160, 2.0, 15.0, 9.0, 100),
    new Food("Banana", "Fruits", "118g", 105, 1.3, 0.3, 27.0, 118),
    new Food("Beef", "Meats", "100g", 250, 26.0, 17.0, 0.0, 100),
    new Food("Bell Pepper", "Vegetables", "120g", 24, 1.0, 0.2, 6.0, 120),
    new Food("Black Coffee", "Beverages", "240ml", 2, 0.3, 0.0, 0.0, 240),
    new Food("Blueberries", "Fruits", "148g", 84, 1.1, 0.5, 21.0, 148),
    new Food("Boiled Egg", "Protein", "50g", 78, 6.3, 5.3, 0.6, 50),
    new Food("Bread, Whole Wheat", "Grains", "28g", 70, 3.6, 1.1, 12.0, 28),
    new Food("Broccoli", "Vegetables", "91g", 31, 2.5, 0.4, 6.0, 91),
    new Food("Brown Rice", "Grains", "195g", 216, 5.0, 1.8, 45.0, 195),
    new Food("Butter", "Fats", "14g", 102, 0.1, 11.5, 0.0, 14),
    new Food("Cabbage", "Vegetables", "89g", 22, 1.1, 0.1, 5.0, 89),
    new Food("Carrots", "Vegetables", "61g", 25, 0.6, 0.1, 6.0, 61),
    new Food("Cauliflower", "Vegetables", "100g", 25, 2.0, 0.3, 5.0, 100),
    new Food("Celery", "Vegetables", "40g", 6, 0.3, 0.1, 1.2, 40),
    new Food("Cheddar Cheese", "Dairy", "28g", 113, 7.0, 9.0, 0.4, 28),
    new Food("Chicken Breast", "Meats", "100g", 165, 31.0, 3.6, 0.0, 100),
    new Food("Chickpeas", "Legumes", "164g", 269, 14.5, 4.2, 45.0, 164)
  ).sortBy(_.name)
}
