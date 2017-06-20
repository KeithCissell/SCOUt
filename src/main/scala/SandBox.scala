// src\main\scala\SandBox.scala
import environment.point._
import environment.variable._

object SandBox {

  def main(args: Array[String]) = {
    val ws = new WindSpeed(7.0)
    val p1 = new EnvironmentPoint(1,2,Seq(ws))
  }

}
