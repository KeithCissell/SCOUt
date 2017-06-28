// src\main\scala\environment\Environment.scala
package environment

import environment.point._
import environment.variable._
import scala.collection.mutable.ArrayBuffer

object GridType {
  type Grid = ArrayBuffer[ArrayBuffer[Option[Point]]]
}

import environment.GridType.Grid
class Environment(val name: String, val length: Int,
    val width: Int, var grid: Grid) {

  def this(s: String, l: Int, w: Int) {
    this(s, l, w, ArrayBuffer.fill(l)(ArrayBuffer.fill(w)(None)))
  }

  def get(x: Int, y: Int): Option[Point] = grid(x)(y)

}
