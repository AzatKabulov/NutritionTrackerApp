package nutritionapp

import java.time.LocalDate
import scala.collection.mutable
import upickle.default._
import os._
import nutritionapp.model.PlannerItem

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

  def removeItemForDate(date: LocalDate, item: PlannerItem): Unit = {
    val updated = getItemsForDate(date).filterNot(_ == item)
    plannerData.update(date, updated)
  }

  def clearDate(date: LocalDate): Unit = {
    plannerData.remove(date)
  }

  def getAllData: Map[LocalDate, List[PlannerItem]] = plannerData.toMap

  def setAllData(data: Map[LocalDate, List[PlannerItem]]): Unit = {
    plannerData.clear()
    plannerData ++= data
  }

  //  Save planner for a date to file
  def saveToFileForDate(date: LocalDate): Unit = {
    val items = getItemsForDate(date)
    val json = ujson.Arr(items.map { item =>
      ujson.Obj(
        "name" -> item.name,
        "source" -> item.source,
        "calories" -> item.calories,
        "protein" -> item.protein,
        "carbs" -> item.carbs,
        "fats" -> item.fats,
        "mealType" -> item.mealType
      )
    }: _*)

    val folder = os.pwd / "data"
    if (!os.exists(folder)) os.makeDir.all(folder)

    val file = folder / s"planner-${date.toString}.json"
    os.write.over(file, ujson.write(json, indent = 2))
  }

  //  Load planner from file (if exists)
  def loadFromFileForDate(date: LocalDate): Unit = {
    val file = os.pwd / "data" / s"planner-${date.toString}.json"
    if (os.exists(file)) {
      val json = ujson.read(os.read(file))
      val items = json.arr.map { obj =>
        PlannerItem(
          name = obj("name").str,
          source = obj("source").str,
          calories = obj("calories").num,
          protein = obj("protein").num,
          carbs = obj("carbs").num,
          fats = obj("fats").num,
          mealType = obj("mealType").str
        )
      }.toList
      plannerData.update(date, items)
    }
  }
}
