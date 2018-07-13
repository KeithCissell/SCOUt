package environment.anomaly

import environment._
import environment.effect._
import environment.layer._

import scala.math._
import scala.collection.mutable.{Map => MutableMap}


// Anomaly
trait Anomaly {
  val name: String
  val area: Double
  val effects: List[Effect]

  // this(data: Map[String, String])

  // Place Anomoly into envrionment and implement effects
  def place(environment: Environment, layers: MutableMap[String,Layer], constructionLayer: ConstructionLayer, scale: Double) = {
    // Set the anomoly's location
    val cellCount = (area / (pow(scale, 2))).toInt
    constructionLayer.getRandomUnmodified() match {
      case Some(c) => {
        val sourceX = c._1
        val sourceY = c._2
        environment.setAnomaly(sourceX, sourceY, name)
        constructionLayer.setToModified(sourceX, sourceY)

        // Set anomaly area
        for (i <- 1 until cellCount) {
          // TODO
        }

        // Propagate Anomaly Effects
        for (effect <- effects) layers.get(effect.seed.name) match {
          case Some(layer) => effect.radiate(sourceX, sourceY, layer, scale)
          case None => // Layer not found
        }
      }
      case None => // No unmodified cells found
    }
  }

}

// Gives access to all the avialable anomaly types that can be seed generated
object AnomalyList {
  // List of all anomalyTypes
  val anomalyTypes = List(
    "Human"
  )

  // List of all seeds set to default
  def defaultList(): List[Anomaly] = List(
    new Human()
  )

  // Returns the form field for the requested element type
  def getForm(anomalyType: String): String = anomalyType match {
    case "Human"  => HumanForm.formFields()
  }
}
