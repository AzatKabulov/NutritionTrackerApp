package nutritionapp

object EventBus {
  var onPlannerUpdated: () => Unit = () => {}
}
