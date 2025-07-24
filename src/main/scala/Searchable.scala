// Trait for search/filter operations
trait Searchable[T <: NutritionInfo] {

  // Search by name (partial match, case-insensitive)
  def searchByName(items: List[T], keyword: String): List[T] = {
    items.filter(i => i.name.toLowerCase.contains(keyword.toLowerCase))
  }

  // Filter by max calories
  def filterByMaxCalories(items: List[T], max: Double): List[T] = {
    items.filter(_.calories <= max)
  }

  // Filter by min calories
  def filterByMinCalories(items: List[T], min: Double): List[T] = {
    items.filter(_.calories >= min)
  }

  // Filter by minimum protein
  def filterByMinProtein(items: List[T], min: Double): List[T] = {
    items.filter(_.protein >= min)
  }

  // Filter by maximum fat
  def filterByMaxFat(items: List[T], max: Double): List[T] = {
    items.filter(_.fat <= max)
  }

  // Filter by exact carbs (or near range ± 1g)
  def filterByCarbs(items: List[T], target: Double): List[T] = {
    items.filter(i => math.abs(i.carbs - target) <= 1.0)
  }