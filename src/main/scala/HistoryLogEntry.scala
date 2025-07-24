// Represents one food or meal entry in history with timestamp
case class HistoryLogEntry(
                            item: NutritionInfo,              // Can be Food or Meal
                            timestamp: String                 // Stores when it was added (e.g., "2025-07-24 14:30")
                          )
