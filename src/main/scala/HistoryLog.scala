import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Manages a history of consumed food and meals with timestamps
class HistoryLog {
  // Stores history entries
  private var log: List[HistoryLogEntry] = List()

  // Standard timestamp format: "yyyy-MM-dd HH:mm"
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  // Adds a new entry with the current timestamp
  def addEntry(item: NutritionInfo): Unit = {
    val now = LocalDateTime.now()
    val timestamp = now.format(formatter)
    val entry = HistoryLogEntry(item, timestamp)
    log = entry :: log // Prepends to the list (latest at top)
  }

  // Returns all entries in the history log
  def getAllEntries: List[HistoryLogEntry] = log

  // Displays all entries in readable format
  def printHistory(): Unit = {
    println("=== Eating History ===")
    if (log.isEmpty) println("No entries yet.")
    else log.foreach(entry => println(s"[${entry.timestamp}] ${entry.item.name}"))
  }

  // Returns all entries recorded on a specific date (e.g., "2025-07-24")
  def getEntriesByDate(date: String): List[HistoryLogEntry] = {
    log.filter(entry => entry.timestamp.startsWith(date))
  }

  // Returns all entries within a given date-time range (inclusive)
  def getEntriesBetween(start: String, end: String): List[HistoryLogEntry] = {
    val startTime = LocalDateTime.parse(start, formatter)
    val endTime = LocalDateTime.parse(end, formatter)
    log.filter { entry =>
      val entryTime = LocalDateTime.parse(entry.timestamp, formatter)
      (entryTime.isAfter(startTime) || entryTime.isEqual(startTime)) &&
        (entryTime.isBefore(endTime) || entryTime.isEqual(endTime))
    }
  }
}
