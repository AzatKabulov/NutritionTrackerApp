package nutritionapp

import nutritionapp.model.User
import scala.util.Try

object AuthManager {
  // Runtime-writable location inside your project
  private val dataDir   = os.pwd / "data"
  private val usersFile = dataDir / "users.json"

  /** Handy for logging/debug */
  def usersFilePath: String = usersFile.toString

  /** Ensure data/users.json exists (starts as empty array). */
  private def ensureUsersFile(): Unit = {
    if (!os.exists(dataDir)) os.makeDir.all(dataDir)
    if (!os.exists(usersFile)) {
      os.write(usersFile, "[]", createFolders = true)
      println(s"[Auth] created empty users file at: $usersFile")
    }
  }

  // -------- tolerant getters for ujson --------
  private def getObj(v: ujson.Value): Option[collection.Map[String, ujson.Value]] =
    v.objOpt.orElse(v.arrOpt.flatMap(_.headOption.flatMap(_.objOpt))) // tolerate odd shapes

  private def str(o: ujson.Value, key: String, default: String = ""): String =
    getObj(o).flatMap(_.get(key)) match {
      case Some(ujson.Str(s)) => s
      case Some(ujson.Num(n)) => n.toString
      case Some(ujson.Bool(b))=> b.toString
      case _                  => default
    }

  private def int(o: ujson.Value, key: String, default: Int = 0): Int =
    getObj(o).flatMap(_.get(key)) match {
      case Some(ujson.Num(n)) => n.toInt
      case Some(ujson.Str(s)) => Try(s.trim.toInt).getOrElse(default)
      case _                  => default
    }

  private def dbl(o: ujson.Value, key: String, default: Double = 0.0): Double =
    getObj(o).flatMap(_.get(key)) match {
      case Some(ujson.Num(n)) => n
      case Some(ujson.Str(s)) => Try(s.trim.toDouble).getOrElse(default)
      case _                  => default
    }

  private def intOpt(o: ujson.Value, key: String): Option[Int] =
    getObj(o).flatMap(_.get(key)) match {
      case Some(ujson.Num(n)) => Some(n.toInt)
      case Some(ujson.Str(s)) => Try(s.trim.toInt).toOption
      case _                  => None
    }
  // --------------------------------------------

  /** Load all users from data/users.json. */
  def loadUsers(): List[User] = {
    ensureUsersFile()
    val raw  = os.read(usersFile).trim
    val root = Try(ujson.read(raw)).toOption.getOrElse(ujson.Arr())
    val arr  = root.arrOpt.getOrElse(Seq.empty[ujson.Value])

    arr.toList.map { js =>
      User(
        name           = str(js, "name"),
        email          = str(js, "email").trim.toLowerCase,
        password       = str(js, "password"),
        age            = int(js, "age", 0),
        height         = dbl(js, "height", 0.0),
        activityLevel  = str(js, "activityLevel", "Moderate"),
        goal           = str(js, "goal", "Maintain"),
        weight         = dbl(js, "weight", 0.0),
        targetCalories = intOpt(js, "targetCalories")
      )
    }
  }

  /** Overwrite data/users.json with the provided list. */
  def saveUsers(users: List[User]): Unit = {
    ensureUsersFile()
    val jsonArr = ujson.Arr(
      users.map { u =>
        ujson.Obj(
          "name"           -> u.name,
          "email"          -> u.email.trim.toLowerCase,
          "password"       -> u.password, // (plain text; hash later if needed)
          "age"            -> u.age,
          "height"         -> u.height,
          "activityLevel"  -> u.activityLevel,
          "goal"           -> u.goal,
          "weight"         -> u.weight,
          "targetCalories" -> u.targetCalories.map(ujson.Num(_)).getOrElse(ujson.Null)
        )
      }*
    )
    os.write.over(usersFile, ujson.write(jsonArr, indent = 2), createFolders = true)
    println(s"[Auth] saved ${users.size} user(s) to: $usersFile")
  }

  /** Returns false if email already exists (case-insensitive). */
  def registerUser(newUser: User): Boolean = {
    val users = loadUsers()
    val email = newUser.email.trim.toLowerCase
    if (users.exists(_.email.equalsIgnoreCase(email))) return false
    saveUsers(users :+ newUser.copy(email = email))
    true
  }

  /** Safer register: Right(savedUser) or Left(error). */
  def register(newUser: User): Either[String, User] =
    if (registerUser(newUser)) Right(newUser.copy(email = newUser.email.trim.toLowerCase))
    else Left("Email is already registered.")

  def findUserByEmail(email: String): Option[User] =
    loadUsers().find(_.email.equalsIgnoreCase(email.trim))

  /** Update/insert by email, returns new total user count. */
  def upsertUser(updated: User): Int = {
    val email = updated.email.trim.toLowerCase
    val users = loadUsers()
    val (keep, _) = users.partition(!_.email.equalsIgnoreCase(email))
    val newList = keep :+ updated.copy(email = email)
    saveUsers(newList)
    newList.size
  }
}
