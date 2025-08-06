package nutritionapp

import scala.io.Source
import scala.util.Using
import java.io.PrintWriter

object SessionManager {
  private val sessionFile = "src/main/resources/data/session.json"

  def saveSession(email: String): Unit = {
    val writer = new PrintWriter(sessionFile)
    writer.write(s"""{"email": "$email"}""")
    writer.close()
  }

  def loadSessionEmail(): Option[String] = {
    if (!new java.io.File(sessionFile).exists()) return None

    val json = Using(Source.fromFile(sessionFile))(_.mkString).getOrElse("")
    val parsed = ujson.read(json)
    Some(parsed("email").str)
  }

  def clearSession(): Unit = {
    new java.io.File(sessionFile).delete()
  }
}
