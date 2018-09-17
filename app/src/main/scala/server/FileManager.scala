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
    println(filePath)
    new File(filePath).exists
  }

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
