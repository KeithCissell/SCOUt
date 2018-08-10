package agent

import environment.cell._
import scoututil.Util._


trait Event {
  val msg: String
  val timeStamp: Double
  val health: Double
  val energy: Double
  val x: Int
  val y: Int
}

trait Successful extends Event
trait Unsuccessful extends Event
trait Fatal extends Event

trait MovementEvent extends Event
trait ScanEvent extends Event
trait AnomalyDetectionEvent extends Event


object Event {
  // Movement events
  class MovementSuccessful(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int, val energyUse: Double, val damageTaken: Double) extends MovementEvent with Successful
  class MovementUnsuccessful(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int, val energyUse: Double, val damageTaken: Double) extends MovementEvent with Unsuccessful

  // Scan events
  class ScanSuccessful(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int, val energyUse: Double, val cellsScanned: Int, val newDiscoveries: Int) extends ScanEvent with Successful
  class ScanUnsuccessful(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int) extends ScanEvent with Unsuccessful

  // Anomaly scan events
  class AnomalyDetectionSuccessful(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int, val anomalies: List[String]) extends AnomalyDetectionEvent with Successful
  class AnomalyDetectionUnsuccessful(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int) extends AnomalyDetectionEvent with Unsuccessful

  // Fatal events
  class HealthDepleted(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int) extends Fatal
  class EnergyDepleted(val msg: String, val timeStamp: Double, val health: Double, val energy: Double, val x: Int, val y: Int) extends Fatal
}
