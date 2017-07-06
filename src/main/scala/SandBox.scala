// src\main\scala\SandBox.scala
import environment._
import environment.point._
import environment.variable._
import scala.collection.mutable.{ArrayBuffer => AB}


object SandBox {

  def main(args: Array[String]) = {
    val ws = new WindSpeed(7.0)
    val p1 = new Point(1,2,AB(ws))
    val env = new Environment("Test", AB(AB(Some(p1))))
  }

}
