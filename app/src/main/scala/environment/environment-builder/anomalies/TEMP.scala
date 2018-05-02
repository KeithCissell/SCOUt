package environment.anomaly

import environment.anomaly._
import environment.effect._
import environment.element._


class SoundMaker(
  val name: String = "Sound Maker",
  val area: Double = 15.0,
  val effects: List[Effect] = List(
    new Sound(seed = new Decibel(40.0))
  )
) extends Anomaly {}
