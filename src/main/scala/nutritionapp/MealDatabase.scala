package nutritionapp

import nutritionapp.model.{Food, Meal}
import scala.io.Source

object MealDatabase {
  private var meals: List[Meal] = List()

  private def numOpt(m: collection.Map[String, ujson.Value], key: String): Option[Double] =
    m.get(key).map(_.num)
  private def strOpt(m: collection.Map[String, ujson.Value], key: String): Option[String] =
    m.get(key).map(_.str)

  def load(): Unit = {
    val stream = Option(getClass.getResourceAsStream("/data/meals.json"))
    if (stream.isEmpty) { println("meals.json not found in /resources/data/. No meals loaded."); meals = List(); return }

    val source = Source.fromInputStream(stream.get)
    val jsonStr = try source.mkString finally source.close()
    val json = ujson.read(jsonStr)

    meals = json.arr.map { item =>
      val obj = item.obj

      val parsedFoods: Seq[Food] = obj.get("foods") match {
        case Some(fArray) =>
          fArray.arr.map { f =>
            val fo = f.obj
            Food(
              name      = fo("name").str,
              calories  = numOpt(fo, "calories").getOrElse(0.0),
              protein   = numOpt(fo, "protein").getOrElse(0.0),
              carbs     = numOpt(fo, "carbs").getOrElse(0.0),
              fats      = numOpt(fo, "fats").getOrElse(0.0),
              category  = strOpt(fo, "category").getOrElse(""),
              imagePath = strOpt(fo, "imagePath").getOrElse(""),
              fiber     = numOpt(fo, "fiber").getOrElse(0.0)
            )
          }.toSeq
        case None => Seq.empty
      }

      val p  = numOpt(obj, "totalProtein").orElse(numOpt(obj, "protein")).getOrElse(0.0)
      val c  = numOpt(obj, "totalCarbs").orElse(numOpt(obj, "carbs")).getOrElse(0.0)
      val f  = numOpt(obj, "totalFats").orElse(numOpt(obj, "fats")).getOrElse(0.0)
      val kc = numOpt(obj, "totalCalories").orElse(numOpt(obj, "calories")).getOrElse(0.0)

      val sumP = parsedFoods.map(_.protein).sum
      val sumC = parsedFoods.map(_.carbs).sum
      val sumF = parsedFoods.map(_.fats).sum
      val sumK = parsedFoods.map(_.calories).sum

      val finalP = if (p > 0) p else sumP
      val finalC = if (c > 0) c else sumC
      val finalF = if (f > 0) f else sumF
      val finalK = if (kc > 0) kc else if (sumK > 0) sumK else finalP*4 + finalC*4 + finalF*9

      val totalFood = Food(
        name      = "__TOTAL__",
        calories  = finalK,
        protein   = finalP,
        carbs     = finalC,
        fats      = finalF,
        category  = strOpt(obj, "category").getOrElse(""),
        imagePath = strOpt(obj, "imagePath").getOrElse(""),
        fiber     = 0.0
      )

      val foodsSeq = totalFood +: parsedFoods

      Meal(
        name        = obj("name").str,
        foods       = foodsSeq,
        category    = strOpt(obj, "category").getOrElse(""),
        imagePath   = strOpt(obj, "imagePath").getOrElse(""),
        servingSize = strOpt(obj, "servingSize").getOrElse("")
      )
    }.toList
  }

  def getMeals: List[Meal] = meals
}
