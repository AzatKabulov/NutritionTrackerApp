class MealManager[T <: NutritionInfo] extends Searchable[T] {
  
  //List of Items
  private var items: List[T] = List()
  
  //Add item
  def addItem(item: T): Unit = {
    items = items :+ item
    
  }

  // Remove item by name
  def removeItem(name: String): Boolean = {
    val before = items.size
    items = items.filterNot(_.name.equalsIgnoreCase(name))
    items.size < before
  }

  // Get all items
  def getAllItems(): List[T] = items

  // Search functions come from Searchable trait
}