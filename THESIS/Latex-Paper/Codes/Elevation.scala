class Elevation(var value: Option[Double]) extends Element {
  val name = "Elevation"
  val unit = "ft"
  val constant = true
  val radial = false
  val lowerBound = -500.0
  val upperBound = 500.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}
