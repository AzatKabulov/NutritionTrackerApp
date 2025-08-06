name := "NutritionTracker"

version := "0.1"

scalaVersion := "3.3.1"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "com.lihaoyi" %% "upickle" % "3.1.0",
  "com.lihaoyi" %% "os-lib"  % "0.9.1"      // ✅ added
)
