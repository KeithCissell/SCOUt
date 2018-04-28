package scoututil

import scala.math._
import scala.util.Random
import scala.collection.mutable.ArrayBuffer


object Util {

  // Custom Types
  type Grid[A] = ArrayBuffer[ArrayBuffer[Option[A]]]

  // Util Functions
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
  def dist(x1: Int, y1: Int, x2: Int, y2: Int): Double = {
    sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
  }

  // Note: All degree calculations are done under the assumption of
  //       North (up, or the positive y direction) being 0 degrees
  def normalizeDegrees(d: Double): Double = d match {
    case d if d > 360 => normalizeDegrees(d - 360)
    case d if d < 0 => normalizeDegrees(d + 360)
    case _ => d
  }
  def normalizeDegrees180(d: Double): Double = d match {
    case d if d > 180 => normalizeDegrees(d - 180)
    case d if d < 0 => normalizeDegrees(d + 180)
    case _ => d
  }
  def directionDegrees(originX: Int, originY: Int, targetX: Int, targetY: Int): Double = {
    val xDiff = originX - targetX
    val yDiff = originY - targetY
    val degrees = toDegrees(atan2(yDiff, xDiff))
    if (xDiff >= 0) return normalizeDegrees(degrees + 90)
    else return normalizeDegrees(degrees + 270)
  }
  def angleBetweenPoints(x1: Int, y1: Int, x2: Int, y2: Int): Double = {
    toDegrees(atan2(y2 - y1, x2 - x1))
  }
  def inRangeDegrees(target: Double, lowerBound: Double, upperBound: Double): Boolean = {
    var t = normalizeDegrees(target)
    var lb = normalizeDegrees(lowerBound)
    var ub = normalizeDegrees(upperBound)
    if (lb > ub) {
      t += 360
      ub += 360
    }
    return (t >= lb && t <= ub)
  }

}
