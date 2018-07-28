package agent


trait Event {
  val msg: String
  val timeStamp: Double
}

object Event {

  class Inoperational(val msg: String, val timeStamp: Double) extends Event
  class LowEnergy(val msg: String, val timeStamp: Double) extends Event
  class Normal(val msg: String, val timeStamp: Double) extends Event
  class SensorNotFound(val msg: String, val timeStamp: Double) extends Event
  class TookDamage(val msg: String, val timeStamp: Double) extends Event

}
