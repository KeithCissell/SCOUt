package environment.modifications

import environment.layer._


// Terrain Modifications
trait TerrainModification {
  def modify(layer: Layer, constructionLayer: ConstructionLayer): Unit
}
