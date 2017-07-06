// src\main\scala\environment\types\Grid.scala
package customtypes

import scala.collection.mutable.ArrayBuffer


object Grid {

  type Grid[A] = ArrayBuffer[ArrayBuffer[Option[A]]]

}
