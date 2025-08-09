package nutritionapp

import nutritionapp.model.User
import scala.io.Source
import scala.util.Using

object AuthManager {

  val userFilePath: String = {
    val resource = getClass.getResource("/data/users.json")
    if (resource != null) resource.getPath
    else "src/main/resources/data/users.json"
  }

  def loadUsers(): List[User] = {
    if (!new java.io.File(userFilePath).exists()) return List()

    val json = Using(Source.fromFile(userFilePath))(_.mkString).getOrElse("[]")
    val parsed = ujson.read(json)

    parsed.arr.toList.map { js =>
      User(
        name = js("name").str,
        email = js("email").str,
        password = js("password").str,
        age = js("age").num.toInt,
        height = js("height").num,
        activityLevel = js("activityLevel").str,
        goal = js("goal").str,
        weight = js.obj.get("weight").map(_.num).getOrElse(0.0),
        targetCalories = js.obj.get("targetCalories").map(_.num.toInt)
      )
    }
  }

  def saveUsers(users: List[User]): Unit = {
    val jsonArr = ujson.Arr(users.map { user =>
      ujson.Obj(
        "name" -> user.name,
        "email" -> user.email,
        "password" -> user.password,
        "age" -> user.age,
        "height" -> user.height,
        "activityLevel" -> user.activityLevel,
        "goal" -> user.goal,
        "weight" -> user.weight,
        "targetCalories" -> user.targetCalories.map(ujson.Num(_)).getOrElse(ujson.Null)
      )
    }: _*)

    val writer = new java.io.PrintWriter(userFilePath)
    writer.write(ujson.write(jsonArr, indent = 2))
    writer.close()
  }

  def registerUser(newUser: User): Boolean = {
    val users = loadUsers()
    if (users.exists(_.email == newUser.email)) return false
    saveUsers(users :+ newUser)
    true
  }

  // ✅ Add this:
  def findUserByEmail(email: String): Option[User] = {
    loadUsers().find(_.email == email)
  }
}
