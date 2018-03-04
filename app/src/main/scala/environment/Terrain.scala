package environment.terrain

import environment.layer._



// Terrain
class Terrain(
    var elevation: Double,
    var waterDepth: Double,
    var navigability: Double // percentage of dificulty to navigate (1 = no trouble, 0 = impossible)
  ) {

}

// TerrainForm with default values set
// def defaultTerrainForm(): Terrain = TerrainForm()

// Terrain Classes
case class TerrainModification(
  val modification: Double,
  val coverage: Double,
  val slope: Double
)
