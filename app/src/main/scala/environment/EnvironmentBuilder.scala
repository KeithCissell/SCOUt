package environment

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


object EnvironmentBuilder {

  def buildEnvironment(name: String, height: Int, width: Int, seeds: List[ElementSeed] = Nil): Environment = {
    val grid = initializedGrid(height, width)
    val environment = new Environment(name, grid)
    for (seed <- seeds) {
      val layer = seed.buildLayer(height, width)
      environment.setLayer(layer)
    }
    return environment
  }

  def initializedGrid(height: Int, width: Int): Grid[Cell] = {
    val grid: Grid[Cell] = AB.fill(height)(AB.fill(width)(None))
    for {
      x <- 0 until height
      y <- 0 until width
    } grid(x)(y) = Some(Cell(x, y))
    return grid
  }

}
