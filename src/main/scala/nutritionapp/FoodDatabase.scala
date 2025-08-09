package nutritionapp

import nutritionapp.model.Food
import scala.io.Source
import scala.util.Using

object FoodDatabase {

  def loadFoods(): List[Food] = {
    val streamOpt = Option(getClass.getResourceAsStream("/data/food.json"))
    if (streamOpt.isEmpty) {
      println("Could not find /data/food.json on classpath.")
      return List()
    }

    val jsonStr = Using(Source.fromInputStream(streamOpt.get))(_.mkString).getOrElse("[]")
    val parsed = ujson.read(jsonStr)

    parsed.arr.toList.map { item =>
      Food(
        name     = item("name").str,
        category = item("category").str,
        imagePath= item("imagePath").str,
        calories = item("calories").num,
        protein  = item("protein").num,
        carbs    = item("carbs").num,
        fats     = item("fats").num,
        fiber    = item("fiber").num
      )
    }
  }
}
