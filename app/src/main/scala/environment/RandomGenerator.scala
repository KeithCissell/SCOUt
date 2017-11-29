package environment.generator

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


object RandomGenerator {

  def generateRandomEnvironment(name: String, length: Int, width: Int, seeds: List[ElementSeed] = Nil): Environment = {
    val grid = initializedGrid(length, width)
    val environment = new Environment(name, grid)
    for (seed <- seeds) {
      val layer = seed.generateLayer(length, width)
      environment.setLayer(layer)
    }
    return environment
  }

  def initializedGrid(length: Int, width: Int): Grid[Cell] = {
    val grid: Grid[Cell] = AB.fill(length)(AB.fill(width)(None))
    for {
      x <- 0 until length
      y <- 0 until width
    } grid(x)(y) = Some(Cell(x, y))
    return grid
  }

}
