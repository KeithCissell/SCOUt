// src\main\scala\environment\VariableGroups.scala
package environment.variable

import environment.variable._


object Groups {

  val allVariables = Seq(
    new Elevation(None),
    new Latitude(None),
    new Longitude(None),
    new Temperature(None),
    new WindSpeed(None)
  )

}
