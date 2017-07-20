package environment.generator

import environment.layer._
import environment.element._
import environment.generator.ElementSeeds._

import customtypes.Grid._

import scala.collection.mutable.{ArrayBuffer => AB}

object LayerGenerator {

  def generateLayer(length: Int, width: Int, seed: ElementSeed): Grid[Element] = seed match {
    case s: ElevationSeed   => elevationLayer(length, width, s)
    case s: LatitudeSeed    => latitudeLayer(length, width, s)
    case s: LongitudeSeed   => longitudeLayer(length, width, s)
  }


  def decibleLayer: Grid[Element] = {
    AB(AB(None))
  }

  def elevationLayer(l: Int, w: Int, seed: ElevationSeed): Grid[Element] = {
    val layer: Grid[Element] = AB.fill(l)(AB.fill(w)(None))
    for {
      x <- 0 until l
      y <- 0 until w
    } layer(x)(y) = Some(new Elevation(100.0))
    return layer
  }

  def latitudeLayer(l: Int, w: Int, seed: LatitudeSeed): Grid[Element] = {
    val layer: Grid[Element] = AB.fill(l)(AB.fill(w)(None))
    for {
      x <- 0 until l
      y <- 0 until w
    } layer(x)(y) = Some(new Latitude(seed.rootValue + (y * seed.scale)))
    return layer
  }

  def longitudeLayer(l: Int, w: Int, seed: LongitudeSeed): Grid[Element] = {
    val layer: Grid[Element] = AB.fill(l)(AB.fill(w)(None))
    for {
      x <- 0 until l
      y <- 0 until w
    } layer(x)(y) = Some(new Latitude(seed.rootValue + (x * seed.scale)))
    return layer
  }

  def temperatureLayer: Grid[Element] = {
    AB(AB(None))
  }

  def windSpeedLayer: Grid[Element] = {
    AB(AB(None))
  }

}
