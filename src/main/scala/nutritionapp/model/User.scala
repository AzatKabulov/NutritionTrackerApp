package nutritionapp.model

case class User(
                 name: String,
                 email: String,
                 password: String,
                 age: Int,
                 height: Double,
                 activityLevel: String, // we'll replace with enum later
                 goal: String           // same here
               )
