package nutritionapp.model

case class User(
                 name: String,
                 email: String,
                 password: String,
                 age: Int,
                 height: Double,
                 activityLevel: String, 
                 goal: String,          
                 weight: Double,        
                 targetCalories: Option[Int] = None 
               )
