package environment

import environment.anomaly._
import environment.element.seed._
import environment.terrainmodification._


class EnvironmentTemplate(
  val name: String,
  val height: Int,
  val width: Int,
  val scale: Double,
  val elementSeeds: List[ElementSeed],
  val terrainModifications: List[TerrainModification],
  val anomalies: List[Anomaly]
)
