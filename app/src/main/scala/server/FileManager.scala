package filemanager

import java.io._
import scala.io.Source

object FileManager {

  def saveJsonFile(fileName: String, path: String, jsonData: String): Unit = {
    val filePath = path + fileName + ".json"
    val writer = new PrintWriter(new File(filePath))
      writer.write(jsonData)
      writer.close()
  }

  def readJsonFile(fileName: String, path: String): String = {
    val filePath = path + fileName + ".json"
    return Source.fromFile(filePath).mkString
  }

}
