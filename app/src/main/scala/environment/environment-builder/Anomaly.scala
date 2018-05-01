package environment.anomalies

import environment.element._


// Anomaly
trait Anomaly {
  val name: String
  val size: Double
  val effects: List[Effect]
}


class Effect(
  val seed: Element,
  val radiation: Double
)
