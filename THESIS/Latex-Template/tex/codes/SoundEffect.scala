class Sound(
  val seed: Decibel
) extends Effect {
  val range = pow(2, seed.value.getOrElse(0.0) / 6)

  def calculate(dist: Double): Double = roundDouble2(seed.value.getOrElse(0.0) - (abs(log2(dist)) * 6))

  def radiate(sourceX: Int, sourceY: Int, layer: Layer, scale: Double) = {
    layer.setElementValue(sourceX, sourceY, seed.value.getOrElse(0.0))
    val cellBlockSize = (range / scale).toInt
    for {
      x <- (sourceX - cellBlockSize) to (sourceX + cellBlockSize)
      y <- (sourceY - cellBlockSize) to (sourceY + cellBlockSize)
      if ((x,y) != (sourceX, sourceY))
      val d: Double = dist(sourceX, sourceY, x, y) * scale
      if (d <= range)
    } layer.setElementValue(x, y, calculate(d))
  }
}
