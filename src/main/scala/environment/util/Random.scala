package myutil

import scala.util.Random

object RandomRange {

  def randomRange(lowerBound: Double, upperBound: Double): Double = {
    val d = lowerBound + (upperBound - lowerBound) * Random.nextDouble
    BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

}
