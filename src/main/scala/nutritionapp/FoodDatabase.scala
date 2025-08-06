package nutritionapp

import nutritionapp.model.Food
import scala.io.Source
import scala.util.Using

object FoodDatabase {

  val foodFilePath = "src/main/resources/data/food.json"

  def loadFoods(): List[Food] = {
    if (!new java.io.File(foodFilePath).exists()) return List()

    val json = Using(Source.fromFile(foodFilePath))(_.mkString).getOrElse("[]")
    val parsed = ujson.read(json)

    parsed.arr.toList.map { item =>
      Food(
        name = item("name").str,
        category = item("category").str,
        imagePath = item("imagePath").str,
        calories = item("calories").num,
        protein = item("protein").num,
        carbs = item("carbs").num,
        fats = item("fats").num,
        fiber = item("fiber").num
      )
    }
  }
}
