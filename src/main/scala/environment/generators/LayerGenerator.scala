package environment.generator

import environment.layer._
import environment.element._
import environment.generator.ElementSeeds._

import customtypes.Grid._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}

object LayerGenerator {

  def generateLayer(length: Int, width: Int, seed: ElementSeed): Layer = seed match {
    case s: ElevationSeed   => elevationLayer(length, width, s)
    case s: LatitudeSeed    => latitudeLayer(length, width, s)
    case s: LongitudeSeed   => longitudeLayer(length, width, s)
  }


  def randomDeviation(deviation: Double, mean: Double): Double = {
    val lowerBound = mean - deviation
    val upperBound = mean + deviation
    val d = lowerBound + (upperBound - lowerBound) * Random.nextDouble
    BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }


  def elevationLayer(l: Int, w: Int, seed: ElevationSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    for {
      x <- 0 until l
      y <- 0 until w
    } (x,y) match {
      case (0,0)  => layer.setElement(x, y, new Elevation(seed.average))
      case _      => {
        val cluster = layer.getCluster(x, y, 3)
        val mean = cluster.sum / cluster.length
        val value = randomDeviation(seed.deviation, mean)
        layer.setElement(x, y, new Elevation(value))
      }
    }
    return layer
  }

  def latitudeLayer(l: Int, w: Int, seed: LatitudeSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    for {
      x <- 0 until l
      y <- 0 until w
    } {
      val value = seed.rootValue + (y * seed.scale)
      layer.setElement(x, y, new Latitude(value))
    }
    return layer
  }

  def longitudeLayer(l: Int, w: Int, seed: LongitudeSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    for {
      x <- 0 until l
      y <- 0 until w
    } {
      val value = seed.rootValue + (x * seed.scale)
      layer.setElement(x, y, new Longitude(value))
    }
    return layer
  }

}
