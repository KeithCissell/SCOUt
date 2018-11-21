case class ElevationSeed(
  val elementName: String = "Elevation",
  val average: Double = 0.0,
  val deviation: Double = 0.15
) extends ElementSeed {
  def randomDeviation(mean: Double, scale: Double): Double = {
    val lowerBound = mean - (deviation * scale)
    val upperBound = mean + (deviation * scale)
    randomDouble(lowerBound, upperBound)
  }
  def buildLayer(height: Int, width: Int, scale: Double): Layer = {
    val layer: Layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
    for {
      x <- 0 until height
      y <- 0 until width
    } {
      val value = randomDeviation(average, scale)
      layer.setElement(x, y, new Elevation(value))
    }
    layer.smoothLayer(3, 3)
    return layer
  }
}
