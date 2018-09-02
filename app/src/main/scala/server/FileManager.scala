package filemanager

import io.circe._
import java.io._
import scala.io.Source

object FileManager {

  def saveJsonFile(fileName: String, path: String, jsonData: String): Unit = {
    val filePath = (path + fileName + ".json").replace(' ', '_')
    val writer = new PrintWriter(new File(filePath))
      writer.write(jsonData)
      writer.close()
  }

  def saveJsonFile(fileName: String, path: String, jsonData: Json): Unit = {
    saveJsonFile(fileName, path, jsonData.toString)
  }

  def readJsonFile(fileName: String, path: String): String = {
    val filePath = path + fileName + ".json"
    return Source.fromFile(filePath).mkString
  }

}
