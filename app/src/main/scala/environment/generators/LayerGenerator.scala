package environment.generator

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.generator.ElementSeeds._

import scala.math._
import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}

object LayerGenerator {

  def generateLayer(length: Int, width: Int, seed: ElementSeed): Layer = seed match {
    case s: DecibleSeed       => decibleLayer(length, width, s)
    case s: ElevationSeed     => elevationLayer(length, width, s)
    case s: LatitudeSeed      => latitudeLayer(length, width, s)
    case s: LongitudeSeed     => longitudeLayer(length, width, s)
    case s: TemperatureSeed   => temperatureLayer(length, width, s)
    case s: WindDirectionSeed => windDirectionLayer(length, width, s)
    case s: WindSpeedSeed     => windSpeedLayer(length, width, s)
  }

  // Layer generators specific to each element type

  def decibleLayer(l: Int, w: Int, seed: DecibleSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(Some(new Decible(0.0)))))
    for (rs <- 0 until seed.randomSources) seed.createRandomSource(l, w)
    for (source <- seed.sources) {
      val currentValue = layer.layer(source.x)(source.y).flatMap(dec => dec.value).getOrElse(0.0)
      if (source.value > currentValue) layer.setElement(source.x, source.y, new Decible(source.value))
      for {
        x <- 0 until l
        y <- 0 until w
        if dist(x, y, source.x, source.y) != 0
        if layer.inLayer(x, y)
      } {
        val currentValue = layer.layer(x)(y).flatMap(dec => dec.value).getOrElse(0.0)
        val newValue = seed.soundReduction(source, x, y)
        if (newValue > currentValue) layer.setElement(x, y, new Decible(newValue))
      }
    }
    return layer
  }

  def elevationLayer(l: Int, w: Int, seed: ElevationSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    if (l > 0 && w > 0) layer.setElement(0, 0, new Elevation(seed.average))
    for {
      x <- 0 until l
      y <- 0 until w
      if (x,y) != (0,0)
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      val value = seed.randomDeviation(mean)
      layer.setElement(x, y, new Elevation(value))
    }
    // Smooth the layer
    for {
      x <- 0 until l
      y <- 0 until w
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      layer.setElement(x, y, new Elevation(mean))
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

  def temperatureLayer(l: Int, w: Int, seed: TemperatureSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    if (l > 0 && w > 0) layer.setElement(0, 0, new Temperature(seed.average))
    for {
      x <- 0 until l
      y <- 0 until w
      if (x,y) != (0,0)
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      val value = seed.randomDeviation(mean)
      layer.setElement(x, y, new Temperature(value))
    }
    // Smooth the layer
    for {
      x <- 0 until l
      y <- 0 until w
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      layer.setElement(x, y, new Temperature(mean))
    }
    return layer
  }

  def windDirectionLayer(l: Int, w: Int, seed: WindDirectionSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    if (l > 0 && w > 0) layer.setElement(0, 0, new WindDirection(seed.average))
    for {
      x <- 0 until l
      y <- 0 until w
      if (x,y) != (0,0)
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      val value = seed.randomDeviation(mean)
      layer.setElement(x, y, new WindDirection(value))
    }
    // Smooth the layer
    for {
      x <- 0 until l
      y <- 0 until w
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      layer.setElement(x, y, new WindDirection(mean))
    }
    return layer
  }

  def windSpeedLayer(l: Int, w: Int, seed: WindSpeedSeed): Layer = {
    val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
    if (l > 0 && w > 0) layer.setElement(0, 0, new WindSpeed(seed.average))
    for {
      x <- 0 until l
      y <- 0 until w
      if (x,y) != (0,0)
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      val value = seed.randomDeviation(mean)
      layer.setElement(x, y, new WindSpeed(value))
    }
    // Smooth the layer
    for {
      x <- 0 until l
      y <- 0 until w
    } {
      val cluster = layer.getClusterValues(x, y, 3)
      val mean = cluster.sum / cluster.length
      layer.setElement(x, y, new WindSpeed(mean))
    }
    return layer
  }

}
