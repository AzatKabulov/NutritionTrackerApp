package nutritionapp

import os._
import scala.util.Try

object SessionManager {
  private val dataDir     = os.pwd / "data"
  private val sessionFile = dataDir / "session.json"

  private def ensureDir(): Unit = if (!os.exists(dataDir)) os.makeDir.all(dataDir)

  /** Save the currently logged-in email. */
  def saveSession(email: String): Unit = {
    ensureDir()
    val js = ujson.Obj("email" -> email.trim.toLowerCase)
    os.write.over(sessionFile, ujson.write(js, indent = 2), createFolders = true)
  }

  /** Alias for convenience (used by some views). */
  def saveSessionEmail(email: String): Unit = saveSession(email)

  /** Load the last session email, if any. */
  def loadSessionEmail(): Option[String] = {
    if (!os.exists(sessionFile)) None
    else {
      val raw = Try(os.read(sessionFile)).getOrElse("")
      if (raw.trim.isEmpty) None
      else {
        val js = Try(ujson.read(raw)).toOption
        js.flatMap(o => o.objOpt.flatMap(_("email").strOpt))
      }
    }
  }

  /** Clear session. */
  def clearSession(): Unit = if (os.exists(sessionFile)) os.remove(sessionFile)
}
