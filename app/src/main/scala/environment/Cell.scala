package environment.cell

import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}


case class Cell(
    val x: Int,
    val y: Int,
    var z: Double = 0.0,
    private var elements: Map[String,Element] = Map.empty) {

  override def toString: String = {
    var str = s"\nCell ($x, $y, $z)"
    for (e <- elements.values) e.value match {
      case Some(v)  => str += s"\n\t${e.name}: $v ${e.unit}"
      case None     => str += s"\n\t${e.name}: NONE"
    }
    return str
  }

  def getX: Int = x
  def getY: Int = y
  def getZ: Double = z
  def dist(p2: Cell): Double = {
    val x1 = x.toDouble
    val y1 = y.toDouble
    val x2 = p2.getX.toDouble
    val y2 = p2.getY.toDouble
    sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
  }
  def dist(xInt: Int, yInt: Int): Double = {
    val x1 = x.toDouble
    val y1 = y.toDouble
    val x2 = xInt.toDouble
    val y2 = yInt.toDouble
    sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
  }
  def get(elementName: String): Option[Element] = {
    elements.get(elementName)
  }
  def getElements: List[Element] = elements.values.toList
  def getElementNames: Set[String] = elements.keys.toSet
  def setElement(element: Element) = elements.get(element.name) match {
    case None     => elements = elements + (element.name -> element)
    case Some(e)  => element.value match {
      case Some(v)  => e.set(v)
      case None     => // Nothing to do
    }
  }
}
