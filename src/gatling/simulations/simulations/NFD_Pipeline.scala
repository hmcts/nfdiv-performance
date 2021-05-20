package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.commons.stats.assertion.Assertion
import scenarios._
import utils.Environment
import scala.concurrent.duration._

class NFD_Pipeline extends Simulation {

  val BaseURL = Environment.baseURL

  /* TEST TYPE DEFINITION */
  /* perftest = performance test against the perftest environment */
  /* pipeline = nightly pipeline against the AAT environment  */
  val testType = System.getProperty("TEST_TYPE", "")

  val env = testType match{
    case "perftest" => "perftest"
    case "pipeline" => "aat"
    case _ => "**INVALID**"
  }

  /* ******************************** */

  /* PERFORMANCE TEST CONFIGURATION */
  val rampUpDurationMins = 5
  val rampDownDurationMins = 5
  val testDurationMins = 60
  val divorceHourlyTarget:Double = 165
  val divorceRatePerSec = divorceHourlyTarget / 3600
  /* ******************************** */

  /* PIPELINE CONFIGURATION */
  val numberOfPipelineUsers:Double = 2
  /* ******************************** */

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    .inferHtmlResources()
    .silentResources

  before{
    println(s"Test Type: ${testType}")
    println(s"Test Environment: ${env}")
    println(s"Test Duration: ${testDurationMins} minutes")
  }

  val NFDSimulation = scenario( "NFDSimulation")
    .exitBlockOnFail {
      exec(flushHttpCache)
      .exec(flushCookieJar)
      .exec(_.set("env", s"${env}"))
      .exec(
        CreateUser.CreateCitizen,
        Homepage.NFDHomepage,
        Login.NFDLogin,
        NFD_01DivorceApplication.ApplicationQuestions1,
        NFD_02DivorceApplication.ApplicationQuestions2,
        Logout.NFDLogout)
    }
    .exec(DeleteUser.DeleteCitizen)

    .exec {
      session =>
        println(session)
        session
    }

  def simulationProfile(simulationType: String): Seq[OpenInjectionStep] = {
    simulationType match {
      case "perftest" =>
        Seq(
          rampUsersPerSec(0.00) to (divorceRatePerSec) during (rampUpDurationMins minutes),
          constantUsersPerSec(divorceRatePerSec) during (testDurationMins minutes),
          rampUsersPerSec(divorceRatePerSec) to (0.00) during (rampDownDurationMins minutes)
        )
      case "pipeline" =>
        Seq(rampUsers(numberOfPipelineUsers.toInt) during (2 minutes))
      case _ =>
        Seq(nothingFor(0))
    }
  }

  def assertions(simulationType: String): Seq[Assertion] = {
    simulationType match {
      case "perftest" =>
        Seq(global.successfulRequests.percent.gte(95))
      case "pipeline" =>
        Seq(global.successfulRequests.percent.is(100))
      case _ =>
        Seq()
    }
  }

  setUp(
    NFDSimulation.inject(simulationProfile(testType))
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
