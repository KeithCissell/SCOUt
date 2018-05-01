package environment.anomaly

import environment.effect._


// Anomaly
trait Anomaly {
  val name: String
  val size: Double
  val effects: List[Effect]
}
