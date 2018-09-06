package agent.controller

import io.circe._
import io.circe.parser._

import agent._
import agent.Event._
import filemanager.FileManager._
import jsonhandler.Decoder._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}


class SCOUtController(val memoryFileName: String = "SCOUtMemory") extends Controller {

  def selectAction(actions: List[String], state: AgentState): String = {
    actions(randomInt(0, actions.length - 1))
  }

  // ---------------------------------MEMORY------------------------------------
  val memory: AB[StateActionPair] = AB()
  val memoryFilePath = "src/resources/agent-memory/"

  def loadMemory() = {
    val fileData = readJsonFile(memoryFileName, memoryFilePath)
    val loadedMemory = parse(fileData) match {
      case Left(_) => AB() // Memory not found or invalid
      case Right(jsonData) => extractStateActionMemory(jsonData)
    }
    memory ++= loadedMemory
  }

  def saveMemory() = {
    val memoryJson = Json.fromValues(memory.map(_.toJson()))
    saveJsonFile(memoryFileName + "-after", memoryFilePath, memoryJson)
  }

}
