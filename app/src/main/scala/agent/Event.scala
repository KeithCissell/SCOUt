package agent

import environment.cell._
import scoututil.Util._


trait Event {
  val msg: String
  val timeStamp: Double
  val health: Double
  val energyLevel: Double
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
  class MovementSuccessful(val msg: String, val timeStamp: Double, val health: Double, val energyLevel: Double, val x: Int, val y: Int) extends MovementEvent with Successful
  class MovementUnsuccessful(val msg: String, val timeStamp: Double, val health: Double, val energyLevel: Double, val x: Int, val y: Int) extends MovementEvent with Unsuccessful

  // Scan events
  class ScanSuccessful(val msg: String, val timeStamp: Double, val health: Double, val energyLevel: Double, val x: Int, val y: Int, val cellsScanned: Int, val newDiscoveries: Int) extends ScanEvent with Successful
  class ScanUnsuccessful(val msg: String, val timeStamp: Double, val health: Double, val energyLevel: Double, val x: Int, val y: Int) extends ScanEvent with Unsuccessful

  // Anomaly scan events
  class AnomalyDetected(val msg: String, val timeStamp: Double, val health: Double, val energyLevel: Double, val x: Int, val y: Int, val anomalyType: String, val xFound: Int, val yFound: Int) extends AnomalyDetectionEvent with Successful

  // Fatal events
  class HealthDepleted(val msg: String, val timeStamp: Double, val health: Double, val energyLevel: Double, val x: Int, val y: Int) extends Fatal
  class EnergyDepleted(val msg: String, val timeStamp: Double, val health: Double, val energyLevel: Double, val x: Int, val y: Int) extends Fatal
}
