package environment.terrain

import environment.layer._



// Terrain
class Terrain(
    var elevation: Double,
    var waterDepth: Double,
    var navigability: Double // percentage of dificulty to navigate (1 = no trouble, 0 = impossible)
  ) {

}

// Terrain Modifications
trait TerrainModification

// Modification Classes

// Elevation modification
case class ElevationModification(
  val modification: Double,
  val deviation: Double,
  val coverage: Double,
  val slope: Double
) extends TerrainModification

// Water Modifications
// trait WaterModification extends TerrainModification

case class WaterPoolModification(
  val maxDepth: Double,
  val deviation: Double,
  val coverage: Double,
  val slope: Double
) extends TerrainModification

case class WaterStreamModification(
  val depth: Double,
  val deviation: Double,
  val width: Double,
  val length: Double
) extends TerrainModification
