package nutritionapp.model

import java.time.LocalDate
import scala.collection.mutable

object MealPlanner {

  // In-memory store: Date → List of PlannerItems
  private val plannerData = mutable.Map[LocalDate, List[PlannerItem]]()

  // Get all items for a specific date
  def getItemsForDate(date: LocalDate): List[PlannerItem] = {
    plannerData.getOrElse(date, List.empty)
  }

  // Add an item to a specific date
  def addItemForDate(date: LocalDate, item: PlannerItem): Unit = {
    val updated = getItemsForDate(date) :+ item
    plannerData.update(date, updated)
  }

  // Optional: clear/reset planner for a date
  def clearDate(date: LocalDate): Unit = {
    plannerData.remove(date)
  }

  // Optional: get full data map (for saving)
  def getAllData: Map[LocalDate, List[PlannerItem]] = plannerData.toMap

  // Optional: set all data (for loading)
  def setAllData(data: Map[LocalDate, List[PlannerItem]]): Unit = {
    plannerData.clear()
    plannerData ++= data
  }
}
