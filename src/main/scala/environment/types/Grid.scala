package customtypes

import scala.collection.mutable.ArrayBuffer


object Grid {

  type Grid[A] = ArrayBuffer[ArrayBuffer[Option[A]]]

}
