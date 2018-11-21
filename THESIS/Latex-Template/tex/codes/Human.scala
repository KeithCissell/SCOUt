class Human(
  val name: String = "Human",
  val area: Double = 6.0,
  val effects: List[Effect] = List(
    new Sound(seed = new Decibel(40.0)),
    new Heat(seed = new Temperature(98.6))
  )
) extends Anomaly {
  def this(formData: Map[String, String]) = this(
    area = formData("Area").toDouble,
    effects = List(
      new Sound(seed = new Decibel(formData("Sound").toDouble)),
      new Heat(seed = new Temperature(formData("Heat").toDouble))
    )
  )
}
