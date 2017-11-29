package jsonhandler

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import environment._
import environment.cell._
import environment.layer._
import environment.element._
import environment.element.seed._


object Encoder {

  def encodeList(name: String, l: List[String]): String = {
    val json =
      (name -> l.map { li => li })
    return compact(render(json))
  }

  def encodeMap(name: String, m: Map[String, Boolean]): String = {
    val json =
      (name -> m.map { case (k,v) => k -> v })
    return compact(render(json))
  }



  // Type Specific encoders

  def encodeCell(c: Cell): String = {
    val json =
      (s"cell.${c.x}.${c.y}" ->
        ("x" -> c.x) ~
        ("y" -> c.y) ~
        ("elements" -> c.getElements.map { e =>
          s"${e.name}" ->
            ("value" -> e.value) ~
            ("unit" -> e.unit) ~
            ("constant" -> e.constant) ~
            ("circular" -> e.circular)
          })
        )
    return compact(render(json))
  }

  def encodeEnvironment(e: Environment): String = {
    val json =
      ("environment" ->
        ("name" -> e.name) ~
        ("length" -> e.length) ~
        ("width" -> e.width) ~
        ("grid" -> e.getAllCells.map { c =>
          s"cell.${c.x}.${c.y}" ->
            ("x" -> c.x) ~
            ("y" -> c.y) ~
            ("elements" -> c.getElements.map { e =>
              s"${e.name}" ->
                ("value" -> e.value) ~
                ("name" -> e.name) ~
                ("unit" -> e.unit) ~
                ("constant" -> e.constant) ~
                ("circular" -> e.circular)
            })
        })
      )
    return compact(render(json))
  }

}
