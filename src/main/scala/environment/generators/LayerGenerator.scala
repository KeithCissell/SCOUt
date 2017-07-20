package environment.generator

import environment.layer._
import environment.element._
import environment.generator.ElementSeeds._

import customtypes.Grid._

import scala.collection.mutable.{ArrayBuffer => AB}

object LayerGenerator {

  def generateLayer(length: Int, width: Int, seed: ElementSeed): Layer = seed match {
    case s: ElevationSeed   => elevationLayer(length, width, s)
    case s: LatitudeSeed    => latitudeLayer(length, width, s)
    case s: LongitudeSeed   => longitudeLayer(length, width, s)
  }


  def elevationLayer(l: Int, w: Int, seed: ElevationSeed): Layer = {
    val emptyL: Grid[Element] = AB.fill(l)(AB.fill(w)(None))
    val layer = new Layer(emptyL)
    for {
      x <- 0 until l
      y <- 0 until w
    } {
      val value = 100.0
      layer.setElement(x, y, new Elevation(value))
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
    val emptyL: Grid[Element] = AB.fill(l)(AB.fill(w)(None))
    val layer = new Layer(emptyL)
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
