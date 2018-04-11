package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}


class Decibel(var value: Option[Double]) extends Element {
  val name = "Decibel"
  val unit = "dB"
  val constant = false
  val radial = false
  val lowerBound = 0.0
  val upperBound = 120.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class NoiseSource(val x: Int, val y: Int, val value: Double)

  case class DecibelSeed(
    val elementName: String = "Decibel",
    val dynamic: Boolean = true,
    val lowerBound: Double = 0.0,
    val upperBound: Double = 120.0,
    val randomSources: Int = 5,
    val sources: AB[NoiseSource] = AB.empty,
    val formFields: String = """{
      lowerBound: 0.0,
      upperBound: 120.0,
      randomSources: Int,
      sources: AB[NoiseSource] = AB.empty
    }"""
  ) extends ElementSeed {
    // NOTE: using noise ruduction of -6 dB every doubling of distance
    // http://www.sengpielaudio.com/calculator-distance.htm
    def soundReduction(source: NoiseSource, x: Int, y: Int, scale: Double): Double = {
      val cellDist = dist(source.x, source.y, x, y) * scale//scale
      val range = soundRange(source)
      if (cellDist <= range) {
        return roundDouble2(source.value - (abs(log2(cellDist)) * 6))
      } else return 0.0
    }
    def soundRange(source: NoiseSource): Double = {
      pow(2, source.value / 6)
    }
    def createRandomSource(height: Int, width: Int) = {
      val randX = round(randomDouble(0, height-1)).toInt
      val randY = round(randomDouble(0, width-1)).toInt
      val randValue = randomDouble(upperBound, lowerBound)
      sources += NoiseSource(randX, randY, randValue)
    }
    def buildLayer(height: Int, width: Int, scale: Double): Layer = {
      val layer = new Layer(AB.fill(height)(AB.fill(width)(Some(new Decibel(0.0)))))
      for (rs <- 0 until randomSources) createRandomSource(height, width)
      for (source <- sources) {
        val currentValue = layer.layer(source.x)(source.y).flatMap(dec => dec.value).getOrElse(0.0)
        if (source.value > currentValue) layer.setElement(source.x, source.y, new Decibel(source.value))
        for {
          x <- 0 until height
          y <- 0 until width
          if dist(x, y, source.x, source.y) != 0
          if layer.inLayer(x, y)
        } {
          val currentValue = layer.layer(x)(y).flatMap(dec => dec.value).getOrElse(0.0)
          val newValue = soundReduction(source, x, y, scale)
          if (newValue > currentValue) layer.setElement(x, y, new Decibel(newValue))
        }
      }
      return layer
    }
  }
}
