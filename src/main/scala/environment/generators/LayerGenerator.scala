package environment.generator

import environment.element._
import environment.generator.ElementSeeds._

import customtypes.Grid._

import scala.collection.mutable.{ArrayBuffer => AB}

object LayerGenerator {

  def generateLayer(length: Int, width: Int, seed: ElementSeed): Grid[Element] = seed match {
    case s: LatitudeSeed   => latitudeLayer(length, width, s)
    case s: LongitudeSeed  => longitudeLayer(length, width, s)
  }

  def decibleLayer: Grid[Element] = {
    AB(AB(None))
  }

  def elevationLayer: Grid[Element] = {
    AB(AB(None))
  }

  def latitudeLayer(l: Int, w: Int, seed: ElementSeed): Grid[Element] = {
    val layer: Grid[Element] = AB.fill(l)(AB.fill(w)(None))
    val root = new Latitude
    root.setRandom
    val rootValue = root.value.get
    for {
      x <- 0 until l
      y <- 0 until w
    } (x,y) match {
      case (0,0)  => layer(x)(y) = Some(new Latitude(rootValue))
      case (_,_)  => y match {
        case 0  => layer(x)(y) = Some(new Latitude(rootValue))
        case _  => layer(x)(y) = Some(new Latitude(rootValue + y))
      }
    }
    return layer
  }

  def longitudeLayer(l: Int, w: Int, seed: ElementSeed): Grid[Element] = {
    val layer: Grid[Element] = AB.fill(l)(AB.fill(w)(None))
    val root = new Latitude
    root.setRandom
    val rootValue = root.value.get
    for {
      x <- 0 until l
      y <- 0 until w
    } (x,y) match {
      case (0,0)  => layer(x)(y) = Some(new Latitude(rootValue))
      case (_,_)  => y match {
        case 0  => layer(x)(y) = Some(new Latitude(rootValue))
        case _  => layer(x)(y) = Some(new Latitude(rootValue + y))
      }
    }
    return layer
  }

  def temperatureLayer: Grid[Element] = {
    AB(AB(None))
  }

  def windSpeedLayer: Grid[Element] = {
    AB(AB(None))
  }

}
