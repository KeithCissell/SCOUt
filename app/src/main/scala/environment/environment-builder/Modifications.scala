package environment.modifications

import environment.layer._


// Terrain Modifications
trait TerrainModification {
  val elementType: String
  def modify(layer: Layer, constructionLayer: ConstructionLayer): Unit
}
