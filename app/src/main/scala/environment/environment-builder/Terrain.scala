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

// Modification Classes
case class ElevationModification(
  val modification: Double,
  val deviation: Double,
  val coverage: Double,
  val slope: Double
)

trait WaterModification

case class WaterPoolModification(
  val maxDepth: Double,
  val deviation: Double,
  val coverage: Double,
  val slope: Double
) extends WaterModification

case class WaterStreamModification(
  val depth: Double,
  val deviation: Double,
  val width: Double,
  val momentum: Double
) extends WaterModification
