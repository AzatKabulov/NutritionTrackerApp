package nutritionapp

import nutritionapp.model.User
import scala.io.Source
import scala.util.Using

object AuthManager {

  // Safely load users.json from resources folder
  val userFilePath: String = {
    val resource = getClass.getResource("/data/users.json")
    if (resource != null) resource.getPath
    else "src/main/resources/data/users.json"
  }

  // Load users from file
  def loadUsers(): List[User] = {
    println(s"📂 Loading users from: $userFilePath")

    if (!new java.io.File(userFilePath).exists()) {
      println("⚠️ File does not exist.")
      return List()
    }

    val json = Using(Source.fromFile(userFilePath))(_.mkString).getOrElse("[]")

    println("📖 Raw JSON Content:")
    println(json)

    val parsed = ujson.read(json)

    val users = parsed.arr.toList.map { js =>
      User(
        name = js("name").str,
        email = js("email").str,
        password = js("password").str,
        age = js("age").num.toInt,
        height = js("height").num,
        activityLevel = js("activityLevel").str,
        goal = js("goal").str
      )
    }

    println(s"✅ Loaded ${users.length} user(s)")
    users
  }

  // Save users to file
  def saveUsers(users: List[User]): Unit = {
    val jsonArr = ujson.Arr(users.map { user =>
      ujson.Obj(
        "name" -> user.name,
        "email" -> user.email,
        "password" -> user.password,
        "age" -> user.age,
        "height" -> user.height,
        "activityLevel" -> user.activityLevel,
        "goal" -> user.goal
      )
    }: _*)

    val writer = new java.io.PrintWriter(userFilePath)
    writer.write(ujson.write(jsonArr, indent = 2))
    writer.close()
  }

  // Register new user
  def registerUser(newUser: User): Boolean = {
    val users = loadUsers()
    if (users.exists(_.email == newUser.email)) return false
    saveUsers(users :+ newUser)
    true
  }
}
