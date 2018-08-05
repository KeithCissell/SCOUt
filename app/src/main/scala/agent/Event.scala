package agent


trait Event {
  val msg: String
  val timeStamp: Double
}

trait Normal extends Event
trait Incapable extends Event
trait Fatal extends Event

object Event {

  class Successful(val msg: String, val timeStamp: Double) extends Normal
  class TookDamage(val msg: String, val timeStamp: Double) extends Normal
  class AnomalyFound(val msg: String, val timeStamp: Double) extends Normal
  class AnomalyNotFound(val msg: String, val timeStamp: Double) extends Normal
  class CannotMove(val msg: String, val timeStamp: Double) extends Incapable
  class SensorNotFound(val msg: String, val timeStamp: Double) extends Incapable
  class HealthDepleted(val msg: String, val timeStamp: Double) extends Fatal
  class EnergyDepleted(val msg: String, val timeStamp: Double) extends Fatal

}
