class Agent (
  name: String,
  controller: Controller,
  sensors: List[Sensor],
  internalMap: Array[Array[Cell]],
  xPosition: Int,
  yPosition: Int,
  health: Double,
  energyLevel: Double,
  mobility: Mobility,
  durabilities: List[Durability]
)
