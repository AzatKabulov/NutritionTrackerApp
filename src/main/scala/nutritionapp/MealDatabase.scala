package nutritionapp

import nutritionapp.model.{Food, Meal}
import scala.io.Source
import upickle.default._

object MealDatabase {

  private var meals: List[Meal] = List()

  def load(): Unit = {
    val stream = Option(getClass.getResourceAsStream("/data/meals.json"))
    if (stream.isEmpty) {
      println("meals.json not found in /resources/data/. No meals loaded.")
      meals = List()
      return
    }

    val source = Source.fromInputStream(stream.get)
    val jsonStr = try source.mkString finally source.close()
    val json = ujson.read(jsonStr)

    meals = json.arr.map { item =>
      val parsedFoods = item.obj.get("foods") match {
        case Some(fArray) => fArray.arr.map { f =>
          Food(
            name = f("name").str,
            calories = f("calories").num,
            protein = f("protein").num,
            carbs = f("carbs").num,
            fats = f("fats").num,
            category = f.obj.get("category").map(_.str).getOrElse(""),
            imagePath = f.obj.get("imagePath").map(_.str).getOrElse(""),
            fiber = f.obj.get("fiber").map(_.num).getOrElse(0.0)
          )
        }.toSeq
        case None => Seq.empty
      }

      Meal(
        name = item("name").str,
        foods = parsedFoods,
        category = item.obj.get("category").map(_.str).getOrElse(""),
        imagePath = item.obj.get("imagePath").map(_.str).getOrElse(""),
        servingSize = item.obj.get("servingSize").map(_.str).getOrElse("")
      )
    }.toList
  }

  def getMeals: List[Meal] = meals
}
