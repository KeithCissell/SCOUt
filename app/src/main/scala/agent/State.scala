package agent


class AgentState(
  val health: Double,
  val energyLevel: Double,
  val clock: Double,
  val elementStates: List[ElementState]
) {

}

class ElementState(
  val elementType: String,
  val value: Option[Double],
  val northQuadrant: QuadrantState,
  val southQuadrant: QuadrantState,
  val eastQuadrant: QuadrantState,
  val westQuadrant: QuadrantState
) {

}

class QuadrantState(
  val percentKnown: Double,
  val averageValue: Option[Double],
  val immediateValue: Option[Double],
) {

}
