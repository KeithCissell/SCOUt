// src\test\scala\environment\EnvironmentSpecs.scala
import environment._
import environment.point._
import environment.variable._
import org.specs2.mutable.Specification

object EnvironmentSpecs extends Specification {

  /*******************************************************
  ** Create data to test on
  *******************************************************/

  // Variables
  val height = new Height(Some(417.0))
  val temperature = new Temperature(77.0)
  val latitude = new Latitude()





  /*******************************************************
  ** Specs2 Tests
  *******************************************************/

  // Variable Tests
  "\nVariable classes hold environmental information and" should {

    "Properly construct" in {
      (height.value == Some(417.0)) &&
      (temperature.value == Some(77.0)) &&
      (latitude.value == None)
    }
    step(height.set(0.0))
    step(temperature.set(85.0))
    step(latitude.set(123.45))
    "Allow inconstant or uninitialized value to be set" in {
      (height.value == Some(417.0)) &&
      (temperature.value == Some(85.0)) &&
      (latitude.value == Some(123.45))
    }
  }

}
