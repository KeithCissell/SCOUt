// AGENT

class Agent (
  name: String,                     // Defimed Per Operation
  controller: Controller,           // Defimed Per Operation
  sensors: List[Sensor],            // Defimed Per Operation
  internalMap: Array[Array[Cell]],  // Defimed Per Operation
  xPosition: Int,                   // Defimed Per Operation
  yPosition: Int,                   // Defimed Per Operation
  health: Double = 100.0,
  energyLevel: Double = 100.0,
  mobility: Mobility = new Mobility (
   movementSlopeUpperThreshHold = 1.0,
   movementSlopeLowerThreshHold = -1.0,
   movementDamageResistance = 0.0,
   movementCost = 0.5,
   slopeCost = 0.2
  ),
  durabilities: List[Durability] = List(
   new Duribility (
     elementType = "Water Depth",
     damageUpperThreshold = 0.25,
     damageLowerThreshold = Double.MaxValue,
     damageResistance = 0.0
   ),
   new Duribility (
     elementType = "Temperature",
     damageUpperThreshold = 150.0,
     damageLowerThreshold = -50.0,
     damageResistance = 0.0
   )
  )
)

// SENSORS

val elevationSensor = new Sensor (
  elementType = "Elevation",
  range = 30.0,
  energyExpense = 0.5,
  hazard = true,
  indicator: Boolean  // Defined Per Operation
)

val waterSensor = new Sensor (
  elementType = "Water Depth",
  range = 1.0,
  energyExpense = 1.0,
  hazard = true,
  indicator: Boolean  // Defined Per Operation
)

val temperatureSensor = new Sensor (
  elementType = "Temperature",
  range = 60.0,
  energyExpense = 1.0,
  hazard = true,
  indicator: Boolean  // Defined Per Operation
)

val decibelSensor = new Sensor (
  elementType = "Decibel",
  range = 15.0,
  energyExpense = 0.1,
  hazard = false,
  indicator: Boolean  // Defined Per Operation
)
