package environment.generator

import myutil.Util._
import environment.layer._
import environment.element._
import environment.generator.ElementSeeds._

import customtypes.Grid._

import scala.math._
import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}

object LayerGenerator {

  def generateLayer(length: Int, width: Int, seed: ElementSeed): Layer = seed match {
    case s: DecibleSeed     => decibleLayer(length, width, s)
    case s: ElevationSeed   => elevationLayer(length, width, s)
    case s: LatitudeSeed    => latitudeLayer(length, width, s)
    case s: LongitudeSeed   => longitudeLayer(length, width, s)
  }


  def decibleLayer(l: Int, w:Int, seed: DecibleSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    for (rs <- 0 until seed.randomSources) seed.createRandomSource(l, w)
    for (source <- seed.sources) {
      layer.setElement(source.x, source.y, new Decible(source.value))
      val cellRange = round(seed.soundRange(source) / seed.scale).toInt
      for {
        x <- (source.x - cellRange) to (source.x + cellRange)
        y <- (source.y - cellRange) to (source.y + cellRange)
        if dist(x, y, source.x, source.y) != 0
        if dist(x, y, source.x, source.y) <= cellRange
        if layer.inLayer(x, y)
      } layer.setElement(x, y, new Decible(seed.soundReduction(source, x, y)))
    }
    return layer
  }

  def elevationLayer(l: Int, w: Int, seed: ElevationSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    for {
      x <- 0 until l
      y <- 0 until w
    } (x,y) match {
      case (0,0)  => layer.setElement(x, y, new Elevation(seed.average))
      case _      => {
        val cluster = layer.getClusterValues(x, y, 3)
        val mean = cluster.sum / cluster.length
        val value = seed.randomDeviation(seed.deviation, mean)
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
