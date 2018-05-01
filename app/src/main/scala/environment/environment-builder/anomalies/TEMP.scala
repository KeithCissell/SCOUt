package environment.anomaly

import environment.anomaly._
import environment.effect._
import environment.element._


class SoundMaker(
  val name: String = "Sound Maker",
  val size: Double = 0.0,
  val effects: List[Effect] = List(
    new Sound(source = new Decibel(40.0))
  )
) extends Anomaly {}
