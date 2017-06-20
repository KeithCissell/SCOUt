// src\main\scala\environment\Environment.scala
package environment

import environment.point._
import environment.variable._

case class Grid(val length: Int, val width: Int) {
  def get(x: Int, y: Int) {}
}

class Environment(name: String, l: Int, w: Int)
    extends Grid(l, w) {}
