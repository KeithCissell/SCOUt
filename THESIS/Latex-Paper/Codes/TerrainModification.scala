trait TerrainModification (
  val name: String
  val elementTypes: List[String]
  def modify(layers: List[Layer])
}
