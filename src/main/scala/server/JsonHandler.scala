package jsonhandler

import io.circe._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import environment._
import environment.cell._
import environment.layer._
import environment.element._
import environment.generator.ElementSeeds._


object Decoder {

  def decodeJson(field: String, data: Json): Option[String] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[String] match {
      case Left(_)  => None
      case Right(s) => Some(s)
    }
  }

}

object Encoder {

  def encodeCell(c: Cell): String = {
    val json =
      ("cell" ->
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
          "cell" ->
            ("x" -> c.x) ~
            ("y" -> c.y) ~
            ("elements" -> c.getElements.map { e =>
              s"${e.name}" ->
                ("value" -> e.value) ~
                ("unit" -> e.unit) ~
                ("constant" -> e.constant) ~
                ("circular" -> e.circular)
            })
        })
      )
    return compact(render(json))
  }

}
