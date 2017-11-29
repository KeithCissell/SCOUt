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
  def randomRange(lowerBound: Double, upperBound: Double): Double = {
    val d = lowerBound + (upperBound - lowerBound) * Random.nextDouble
    roundDouble2(d)
  }
  def roundDouble2(d: Double): Double = {
    BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

}
