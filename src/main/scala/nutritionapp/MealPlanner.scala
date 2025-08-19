package nutritionapp

import java.time.LocalDate
import scala.collection.mutable
import scala.util.Try
import nutritionapp.model.PlannerItem
import os._
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

object MealPlanner {

  // -------------------- Current user scope --------------------
  private var currentUserEmail: Option[String] = None

  /** Call right after a successful login or signup. */
  def setCurrentUser(email: String): Unit = {
    val norm = email.trim.toLowerCase
    if (!currentUserEmail.contains(norm)) {
      currentUserEmail = Some(norm)
      plannerData.clear()            // drop previous user's in-memory data
      migrateFromHashedDirIfExists() // one-time: old hashed dir -> email-named dir
      ensureUserDirs()               // make sure planner/notes directories exist
    }
  }

  def getCurrentUserEmail: Option[String] = currentUserEmail

  private def requireUser(): String =
    currentUserEmail.getOrElse(
      throw new IllegalStateException(
        "MealPlanner: current user not set. Call MealPlanner.setCurrentUser(email) after login."
      )
    )

  // -------------------- Directory layout (email-named) --------------------
  /** Turn an email into a safe folder name. */
  private def emailDirName(e: String): String = {
    val s = e.trim.toLowerCase
    // keep alnum . _ + - ; convert @ to _at_ ; everything else -> '-'
    val replaced = s.replace("@", "_at_")
      .map { ch => if (ch.isLetterOrDigit || ".-_+".contains(ch)) ch else '-' }
    replaced.replaceAll("-{2,}", "-").stripPrefix("-").stripSuffix("-")
  }

  /** Old hashed dir (from previous versions). */
  private def hashedDir(email: String): os.Path = {
    val md = MessageDigest.getInstance("SHA-1")
    val h  = md.digest(email.getBytes(StandardCharsets.UTF_8)).map("%02x".format(_)).mkString
    os.pwd / "data" / "users" / h
  }

  /** Base dir for current user (email-named). */
  def userDataDir: os.Path =
    os.pwd / "data" / "users" / "email" / emailDirName(requireUser())

  private def userPlannerDir: os.Path = userDataDir / "planner"
  def userNotesDir: os.Path = userDataDir / "notes"

  private def ensureUserDirs(): Unit = {
    if (!os.exists(userPlannerDir)) os.makeDir.all(userPlannerDir)
    if (!os.exists(userNotesDir))   os.makeDir.all(userNotesDir)
  }

  /** One-time migration: if old hashed folder exists, move it to the new email-named folder. */
  private def migrateFromHashedDirIfExists(): Unit = {
    val email = requireUser()
    val old   = hashedDir(email)
    val neu   = userDataDir
    if (os.exists(old) && !os.exists(neu)) {
      try {
        os.makeDir.all(neu / os.up)                  // ensure parent exists
        os.move(old, neu, replaceExisting = false)   // move whole dir (planner + notes)
        println(s"[MealPlanner] Migrated user data from $old -> $neu")
      } catch { case _: Throwable => () }            // best-effort
    }
  }

  // -------------------- In-memory store (for the current user) --------------------
  private val plannerData: mutable.Map[LocalDate, List[PlannerItem]] = mutable.Map.empty

  def getItemsForDate(date: LocalDate): List[PlannerItem] =
    plannerData.getOrElse(date, Nil)

  def addItemForDate(date: LocalDate, item: PlannerItem): Unit = {
    val updated = getItemsForDate(date) :+ item
    plannerData.update(date, updated)
    saveToFileForDate(date) // auto-persist
  }

  def removeItemForDate(date: LocalDate, item: PlannerItem): Unit = {
    val updated = getItemsForDate(date).filterNot(_ == item)
    plannerData.update(date, updated)
    saveToFileForDate(date) // auto-persist
  }

  def replaceItemsForDate(date: LocalDate, items: List[PlannerItem]): Unit = {
    plannerData.update(date, items)
    saveToFileForDate(date)
  }

  def clearDate(date: LocalDate): Unit = {
    plannerData.remove(date)
    deleteFileForDateIfExists(date) // keep storage tidy
  }

  def clearAll(): Unit = plannerData.clear()

  def getAllData: Map[LocalDate, List[PlannerItem]] = plannerData.toMap

  def setAllData(data: Map[LocalDate, List[PlannerItem]]): Unit = {
    plannerData.clear()
    plannerData ++= data
  }

  // -------------------- Persistence (per-user) --------------------
  /** Save planner for a date to file for the current user. */
  def saveToFileForDate(date: LocalDate): Unit = {
    ensureUserDirs()
    val items = getItemsForDate(date)

    if (items.isEmpty) { // if nothing left for this date, remove file
      deleteFileForDateIfExists(date); return
    }

    val json = ujson.Arr(
      items.map { it =>
        ujson.Obj(
          "name"     -> it.name,
          "source"   -> it.source,
          "calories" -> it.calories,
          "protein"  -> it.protein,
          "carbs"    -> it.carbs,
          "fats"     -> it.fats,
          "mealType" -> it.mealType
        )
      }*
    )

    val file = userPlannerDir / s"${date.toString}.json"
    os.write.over(file, ujson.write(json, indent = 2))
  }

  /** Load planner for a date from the current user's folder (if exists). */
  def loadFromFileForDate(date: LocalDate): Unit = {
    val file = userPlannerDir / s"${date.toString}.json"
    if (os.exists(file)) {
      val parsed = ujson.read(os.read(file))
      val items = parsed.arr.map { obj =>
        PlannerItem(
          name     = obj("name").str,
          source   = obj("source").str,
          calories = obj("calories").num,
          protein  = obj("protein").num,
          carbs    = obj("carbs").num,
          fats     = obj("fats").num,
          mealType = obj("mealType").str
        )
      }.toList
      plannerData.update(date, items)
    } else {
      plannerData.remove(date) // ensure blank if no file
    }
  }

  /** Optional: eagerly load everything that exists for the current user. */
  def loadAllForCurrentUser(): Unit = {
    if (os.exists(userPlannerDir)) {
      for (f <- os.list(userPlannerDir) if f.ext == "json") {
        Try(LocalDate.parse(f.baseName)).toOption.foreach(loadFromFileForDate)
      }
    }
  }

  private def deleteFileForDateIfExists(date: LocalDate): Unit = {
    val file = userPlannerDir / s"${date.toString}.json"
    if (os.exists(file)) os.remove(file)
  }

  // -------------------- Maintenance helpers --------------------
  /** Wipe ALL storage for the current user (planner + notes) and in-memory cache. */
  def wipeAllStorageForCurrentUser(): Unit = {
    val dir = userDataDir
    if (os.exists(dir)) { try os.remove.all(dir) catch { case _: Throwable => () } }
    plannerData.clear()
  }

  /** For debugging: where is this user's data stored? */
  def currentUserDataPath: String = userDataDir.toString
}
