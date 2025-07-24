// Trait for search/filter operations
trait Searchable :

  // Search by name (partial match, case-insensitive)
  def searchByName(items: List[NutritionInfo], keyword: String): List[NutritionInfo] = {
    items.filter(i => i.name.toLowerCase.contains(keyword.toLowerCase))
  }

  // Filter by max calories
  def filterByMaxCalories(items: List[NutritionInfo], max: Double): List[NutritionInfo] = {
    items.filter(_.calories <= max)
  }

  // Filter by min calories
  def filterByMinCalories(items: List[NutritionInfo], min: Double): List[NutritionInfo] = {
    items.filter(_.calories >= min)
  }

  // Filter by minimum protein
  def filterByMinProtein(items: List[NutritionInfo], min: Double): List[NutritionInfo] = {
    items.filter(_.protein >= min)
  }

  // Filter by maximum fat
  def filterByMaxFat(items: List[NutritionInfo], max: Double): List[NutritionInfo] = {
    items.filter(_.fat <= max)
  }

  // Filter by exact carbs (or near range ± 1g)
  def filterByCarbs(items: List[NutritionInfo], target: Double): List[NutritionInfo] = {
    items.filter(i => math.abs(i.carbs - target) <= 1.0)
  }