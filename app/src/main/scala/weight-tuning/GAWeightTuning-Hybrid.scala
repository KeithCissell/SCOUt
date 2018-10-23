package weighttuning

import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scoutagent._
import scoutagent.State._
import scoutagent.controller._
import environment._
import environment.anomaly._
import environment.element._
import environment.element.seed._
import environment.terrainmodification._
import environment.EnvironmentBuilder._
import test._
import operation._
import scoututil.Util._
import jsonhandler.Encoder._
import jsonhandler.Decoder._
import filemanager.FileManager._
import weighttuning.WeightTuning._
import org.joda.time.DateTime
import scala.collection.mutable.{Set => MutableSet}
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


object WeightTuningHybrid {
  // GA Attributes
  val runName = "Hybrid"

  // Test stuff
  val goalTemplate1 = new FindAnomaliesTemplate(Map("Human" -> 1), None)
  val memoryFileName1 = "FHOfficialTemplatesTEST2"

  val goalTemplate2 = new MapElementsTemplate(List("Water Depth"), None)
  val memoryFileName2 = "MWOfficialTemplatesTEST2"

  // Run Individual Through Tests
  def runTests(population: List[Individual]): Unit = {
    // Create a controller for each individual
    val controllers1: Map[String,SCOUtController] = (for (i <- 0 until population.length) yield {
      val ind = population(i)
      val name = i.toString
      (name -> new SCOUtController(memoryFileName1, "json", false, ind.generateWeightsSet))
    }).toMap
    // Setup Test
    val gaTest1 = new Test(
      testEnvironments = Map(),
      testTemplates = Map(
        "EASY" -> (25, 1),
        "MEDIUM" -> (15, 1),
        "HARD" -> (10, 1)),
      controllers = controllers1,
      sensors = List(
        new ElevationSensor(false),
        new DecibelSensor(true),
        new TemperatureSensor(true),
        new WaterSensor(false)),
      goalTemplate = goalTemplate1,
      maxActions = Some(maxActions))

    // Run Test
    gaTest1.run

    // Create a controller for each individual
    val controllers2: Map[String,SCOUtController] = (for (i <- 0 until population.length) yield {
      val ind = population(i)
      val name = i.toString
      (name -> new SCOUtController(memoryFileName2, "json", false, ind.generateWeightsSet))
    }).toMap
    // Setup Test
    val gaTest2 = new Test(
      testEnvironments = Map(),
      testTemplates = Map(
        "EASY" -> (25, 1),
        "MEDIUM" -> (15, 1),
        "HARD" -> (10, 1)),
      controllers = controllers2,
      sensors = List(
        new ElevationSensor(false),
        new DecibelSensor(true),
        new TemperatureSensor(true),
        new WaterSensor(false)),
      goalTemplate = goalTemplate2,
      maxActions = Some(maxActions))

    // Run Test
    gaTest2.run

    // Set Results to Individuals
    for ((name,tm) <- gaTest1.testMetrics) {
      val ind = population(name.toInt)
      ind.avgActions match {
        case Some(v) => ind.avgActions = Some((v + tm.avgActions) / 2)
        case None => ind.avgActions = Some(tm.avgActions)
      }
      ind.avgRemainingHealth match {
        case Some(v) => ind.avgRemainingHealth = Some((v + tm.avgRemainingHealth) / 2.0)
        case None => ind.avgRemainingHealth = Some(tm.avgRemainingHealth)
      }
      ind.avgRemainingEnergy match {
        case Some(v) => ind.avgRemainingEnergy = Some((v + tm.avgRemainingEnergy) / 2.0)
        case None => ind.avgRemainingEnergy = Some(tm.avgRemainingEnergy)
      }
      ind.avgGoalCompletion match {
        case Some(v) => ind.avgGoalCompletion = Some((v + tm.avgGoalCompletion) / 2.0)
        case None => ind.avgGoalCompletion = Some(tm.avgGoalCompletion)
      }
    }

    // Set Results to Individuals
    for ((name,tm) <- gaTest2.testMetrics) {
      val ind = population(name.toInt)
      ind.avgActions match {
        case Some(v) => ind.avgActions = Some((v + tm.avgActions) / 2)
        case None => ind.avgActions = Some(tm.avgActions)
      }
      ind.avgRemainingHealth match {
        case Some(v) => ind.avgRemainingHealth = Some((v + tm.avgRemainingHealth) / 2.0)
        case None => ind.avgRemainingHealth = Some(tm.avgRemainingHealth)
      }
      ind.avgRemainingEnergy match {
        case Some(v) => ind.avgRemainingEnergy = Some((v + tm.avgRemainingEnergy) / 2.0)
        case None => ind.avgRemainingEnergy = Some(tm.avgRemainingEnergy)
      }
      ind.avgGoalCompletion match {
        case Some(v) => ind.avgGoalCompletion = Some((v + tm.avgGoalCompletion) / 2.0)
        case None => ind.avgGoalCompletion = Some(tm.avgGoalCompletion)
      }
    }
  }

  // MAIN FUNCTION
  def main(args: Array[String]): Unit = {
    println()
    println("INITIALIZING...")

    // Initialize Population
    var population: List[Individual] = (for (i <- 0 until populationSize) yield {
      val weights = for (j <- 0 to 21) yield {
        if (j != 21) randomDouble(0.0, 1.0)
        else randomDouble(0.0, maxCompared)
      }
      new Individual(weights = weights.toList)
    }).toList
    // Score initial Individuals
    runTests(population)

    // Collect Generation Run Fitness
    var avgFitnesses: AB[Double] = AB()
    var avgFitnessesPop: AB[Double] = AB()

    // EVOLVE
    for (g <- 0 until numGenerations) {
      println()
      println()
      println(s"RUNNING GENERATION ${g + 1} / $numGenerations")

      // Create Crossovers
      val crossovers = for (i <- 0 until (populationSize / 2)) yield {
        val pop = population.to[AB]
        val ind1 = rouletteSelect(pop.toList)
        val ind2 = rouletteSelect((pop -= ind1).toList)
        crossover(ind1, ind2)
      }

      // Create Mutated Copies
      val mutates = for (i <- 0 until (populationSize / 2)) yield {
        val ind = rouletteSelect(population)
        mutate(ind, mutationRate(g))
      }

      // Score Individuals
      var totalPopulation: AB[Individual] = (population ++ crossovers ++ mutates).to[AB]
      runTests(totalPopulation.toList)

      // Find Average Fitness of Generation
      val avgFitness: Double = totalPopulation.map(_.fitness).foldLeft(0.0)(_ + _) / totalPopulation.size
      avgFitnesses += avgFitness
      println()
      println(s"AVERAGE FITNESS: ${roundDoubleX(avgFitness, 4)}")
      println()
      println("Best Individual Stats")
      println()

      // Select Survivors
      var newPopulation: AB[Individual] = AB()
      for (i <- 0 until elites) {
        var bestInd = totalPopulation(0)
        var bestFitness = bestInd.fitness
        for (ind <- totalPopulation) if (ind.fitness > bestFitness) {
          bestInd = ind
          bestFitness = ind.fitness
        }
        bestInd.print
        newPopulation += bestInd
        totalPopulation -= bestInd
      }
      for (i <- 0 until populationSize - elites) {
        val selectedInd = rouletteSelect(totalPopulation.toList)
        newPopulation += selectedInd
        totalPopulation -= selectedInd
      }

      // Save New Population Average Fitness
      val avgFitnessPop: Double = newPopulation.map(_.fitness).foldLeft(0.0)(_ + _) / newPopulation.size
      avgFitnessesPop += avgFitnessPop
      println()
      println(s"AVERAGE FITNESS NEW POPULATION: ${roundDoubleX(avgFitnessPop, 4)}")

      // Set new population
      population = newPopulation.toList

      // Iterative Save
      savePopulation(runName, s"Gen${g+1}", population)
    }

    // Save Output
    savePopulation(runName, "FINAL", population)
    saveGenerations(runName, avgFitnesses.toList, avgFitnessesPop.toList)

    println("FINAL RESULTS")
    for (ind <- population) ind.print
  }

}
