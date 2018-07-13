package environment.terrainmodification

import environment.layer._


// Terrain Modifications
trait TerrainModification {
  val name: String
  val elementType: String

  //def this(formData: Map[String, String]): TerrainModification
  def modify(layer: Layer, constructionLayer: ConstructionLayer): Unit
}

// Gives access to all the avialable terrain modification types that can be applied
object TerrainModificationList {
  // List of all terrain modification types
  val terrainModificationTypes = List(
    "Elevation Modification",
    "Water Pool Modification",
    "Water Stream Modification"
  )

  // Returns the form field for the requested element type
  def getForm(elementType: String): String = elementType match {
    case "Elevation Modification"     => ElevationModificationForm.formFields()
    case "Water Pool Modification"    => WaterPoolModificationForm.formFields()
    case "Water Stream Modification"  => WaterStreamModificationForm.formFields()
  }

  // List of all seeds set to default
  def defaultList(): List[TerrainModification] = List(
    new ElevationModification(modification = 150.0, deviation = 20.0, coverage = 0.3, slope = 26.0),
    new ElevationModification(modification = -30.0, deviation = 3.0, coverage = 0.17, slope = 10.0),
    new WaterPoolModification(maxDepth = 10.0, deviation = 2.5, coverage = 0.15, slope = 3.0),
    new WaterStreamModification(depth = 5.0, deviation = 2.0, width = 30.0, length = 1000.0)
  )
}
