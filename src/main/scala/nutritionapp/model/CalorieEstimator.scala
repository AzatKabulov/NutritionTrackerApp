package nutritionapp.model

object CalorieEstimator {

  // ----- Types (Scala 2 friendly) -------------------------------------------
  sealed trait Sex
  case object Male extends Sex
  case object Female extends Sex
  case object UnknownSex extends Sex

  sealed trait Goal
  case object Lose extends Goal
  case object Maintain extends Goal
  case object Gain extends Goal

  sealed trait Activity { def factor: Double }
  case object Sedentary extends Activity { val factor = 1.20 }
  case object Light     extends Activity { val factor = 1.375 }
  case object Moderate  extends Activity { val factor = 1.55 }
  case object Very      extends Activity { val factor = 1.725 }
  case object Extra     extends Activity { val factor = 1.90 }

  sealed trait Pace { def kgPerWeek: Double; def label: String }
  case object Slow       extends Pace { val kgPerWeek = 0.25; val label = "Slow (0.25 kg/wk)" }
  case object ModerateP  extends Pace { val kgPerWeek = 0.50; val label = "Moderate (0.5 kg/wk)" }
  case object Aggressive extends Pace { val kgPerWeek = 0.75; val label = "Aggressive (0.75 kg/wk)" }

  case class Input(
                    age: Int,
                    heightCm: Double,
                    weightKg: Double,
                    sex: Sex,
                    activity: Activity,
                    goal: Goal,
                    pace: Pace = ModerateP
                  )

  case class Advice(
                     maintenanceKcal: Int,
                     targetKcal: Int,
                     deltaKcal: Int, // target - maintenance (negative = deficit)
                     goal: Goal,
                     pace: Pace
                   )

  // ----- Core formulas ------------------------------------------------------
  private def bmrMifflin(i: Input): Double = {
    val sexConstant = i.sex match {
      case Male        => 5.0
      case Female      => -161.0
      case UnknownSex  => -78.0
    }
    10.0 * i.weightKg + 6.25 * i.heightCm - 5.0 * i.age + sexConstant
  }

  private def tdee(i: Input): Double = bmrMifflin(i) * i.activity.factor

  private val KCAL_PER_KG = 7700.0

  def recommend(i: Input): Advice = {
    val maint = tdee(i)
    val deltaPerDayRaw = (i.pace.kgPerWeek * KCAL_PER_KG) / 7.0

    val rawTarget = i.goal match {
      case Maintain => maint
      case Lose     => maint - deltaPerDayRaw
      case Gain     => maint + math.min(500.0, deltaPerDayRaw) // cap surplus a bit
    }

    val minKcal = i.sex match {
      case Female     => 1200
      case Male       => 1500
      case UnknownSex => 1350
    }

    val clamped = i.goal match {
      case Lose     => math.max(rawTarget, minKcal.toDouble)
      case Maintain => rawTarget
      case Gain     => rawTarget
    }

    val maintenanceKcal = math.round(maint).toInt
    val targetKcal      = math.round(clamped).toInt
    Advice(
      maintenanceKcal = maintenanceKcal,
      targetKcal = targetKcal,
      deltaKcal = targetKcal - maintenanceKcal,
      goal = i.goal,
      pace = i.pace
    )
  }

  // ----- Helpers to map from your strings ----------------------------------

  def parseSex(s: String): Sex = s.toLowerCase match {
    case "male" | "m"   => Male
    case "female" | "f" => Female
    case _              => UnknownSex
  }

  def parseActivity(s: String): Activity = s.toLowerCase match {
    case "sedentary" | "none" | "low"         => Sedentary
    case "light" | "lightly active"           => Light
    case "moderate" | "moderately active"     => Moderate
    case "very" | "very active"               => Very
    case "extra" | "super" | "athlete"        => Extra
    case _                                    => Moderate
  }

  // ✅ More tolerant: handles "Lose Fat", "Cut", "Gain Muscle", "Bulk"
  def parseGoal(s: String): Goal = {
    val t = s.toLowerCase.trim
    if (t.contains("lose") || t.contains("cut") || t.contains("fat")) Lose
    else if (t.contains("gain") || t.contains("bulk") || t.contains("muscle")) Gain
    else Maintain
  }

  // Build Input from your existing User model
  def fromUser(
                user: User,
                sexOpt: Option[String] = None,
                pace: Pace = ModerateP
              ): Input = {
    val sex = sexOpt.map(parseSex).getOrElse(UnknownSex)
    Input(
      age = user.age,
      heightCm = user.height,
      weightKg = user.weight,
      sex = sex,
      activity = parseActivity(user.activityLevel),
      goal = parseGoal(user.goal),
      pace = pace
    )
  }

  // Optional: display rounding (ceil to nearest 10)
  def roundUp10(n: Int): Int = ((n + 9) / 10) * 10

  // Expose a friendly alias so you can write CalorieEstimator.Pace.*
  object Pace {
    val Slow       = CalorieEstimator.Slow
    val Moderate   = CalorieEstimator.ModerateP
    val Aggressive = CalorieEstimator.Aggressive
  }
}
