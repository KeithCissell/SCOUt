package filemanager

import io.circe._
import java.io._
import scala.io.Source

object FileManager {
  // File Paths
  val environmentPath = "src/resources/environments/"
  val environmentTemplatePath = "src/resources/environment-templates/"
  val memoryFilePath = "src/resources/agent-memory/"

  def fileExists(fileName: String, path: String, extension: String): Boolean = {
    val filePath = (path + fileName + s".$extension").replace(' ', '_')
    new File(filePath).exists
  }

  def saveFile(fileName: String, path: String, extension: String, data: String): Unit = {
    val filePath = (path + fileName + s".$extension").replace(' ', '_')
    val writer = new PrintWriter(new File(filePath))
      writer.write(data)
      writer.close()
  }

  def saveFile(fileName: String, path: String, extension: String, data: Json): Unit = {
    val filePath = (path + fileName + s".$extension").replace(' ', '_')
    val writer = new PrintWriter(new File(filePath))
      writer.write(data.toString)
      writer.close()
  }

  def saveJsonFile(fileName: String, path: String, jsonData: String): Unit = {
    saveFile(fileName, path, "json", jsonData)
  }

  def saveJsonFile(fileName: String, path: String, jsonData: Json): Unit = {
    saveFile(fileName, path, "json", jsonData.toString)
  }

  def readFile(fileName: String, path: String, extension: String): String = {
    val filePath = path + fileName + s".$extension"
    return Source.fromFile(filePath).mkString
  }

  def readJsonFile(fileName: String, path: String): String = {
    readFile(fileName, path, "json")
  }

  def getEnvironmentFileNames(): List[String] = {
    val d = new File(environmentPath)
    if (d.exists && d.isDirectory) d.listFiles.filter(_.isFile).toList.map(_.getName())
    else List[String]()
  }

  def getEnvironmentTemplateFileNames(): List[String] = {
    val d = new File(environmentTemplatePath)
    if (d.exists && d.isDirectory) d.listFiles.filter(_.isFile).toList.map(_.getName())
    else List[String]()
  }

  def getTestFileNames(): List[String] = {
    val d = new File(memoryFilePath)
    if (d.exists && d.isDirectory) d.listFiles.filter(_.isFile).toList.map(_.getName())
    else List[String]()
  }

}
