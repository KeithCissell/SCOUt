package jsonhandler

import io.circe._


object Decoder {

  def extractString(field: String, data: Json): Option[String] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[String] match {
      case Left(_)  => None
      case Right(s) => Some(s)
    }
  }

  def extractInt(field: String, data: Json): Option[Int] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[Int] match {
      case Left(_)  => None
      case Right(i) => Some(i)
    }
  }

}
