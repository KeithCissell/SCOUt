// src\main\scala\SandBox.scala
import environment._
import environment.point._
import environment.variable._
import scala.collection.mutable.ArrayBuffer


object SandBox {

  def main(args: Array[String]) = {
    val ws = new WindSpeed(7.0)
    val p1 = new Point(1,2,Seq(ws))
    println(p1)

    val env = new Environment("Test", 1, 1, ArrayBuffer(ArrayBuffer(Some(p1))))
    println(env.grid)

    val testGet = env.get(0,0)
    println(testGet)
  }

}
