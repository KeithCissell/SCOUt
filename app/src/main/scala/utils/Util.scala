package scoututil

import scala.math._
import scala.util.Random
import scala.collection.mutable.ArrayBuffer


object Util {

  // Custom Types
  type Grid[A] = ArrayBuffer[ArrayBuffer[Option[A]]]

  // Util Functions
  def dist(x1: Int, y1: Int, x2: Int, y2: Int): Double = {
    sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
  }
  def log2(x: Double): Double = {
    log(x) / log(2)
  }
  def randomInt(lowerBound: Int, upperBound: Int): Int = {
    val r = lowerBound + (upperBound - lowerBound) * Random.nextDouble
    Math.round(r.toFloat)
  }
  def randomDouble(lowerBound: Double, upperBound: Double): Double = {
    lowerBound + (upperBound - lowerBound) * Random.nextDouble
  }
  def roundDouble2(d: Double): Double = {
    BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

}
