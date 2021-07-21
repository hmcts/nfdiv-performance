package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.commons.stats.assertion.Assertion
import scenarios._
import utils.Environment
import scala.concurrent.duration._

class NFD_Simulation extends Simulation {

  val BaseURL = Environment.baseURL

  /* TEST TYPE DEFINITION */
  /* pipeline = nightly pipeline against the AAT environment (see the Jenkins_nightly file) */
  /* perftest (default) = performance test against the perftest environment */
  val testType = scala.util.Properties.envOrElse("TEST_TYPE", "perftest")

  //set the environment based on the test type
  val environment = testType match{
    case "perftest" => "perftest"
    case "pipeline" => "aat"
    case _ => "**INVALID**"
  }
  /* ******************************** */

  /* ADDITIONAL COMMAND LINE ARGUMENT OPTIONS */
  val debugMode = System.getProperty("debug", "off") //runs a single user e.g. ./gradle gatlingRun -Ddebug=on (default: off)
  val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat
  /* ******************************** */

  /* PERFORMANCE TEST CONFIGURATION */
  val rampUpDurationMins = 5
  val rampDownDurationMins = 5
  val testDurationMins = 60
  val divorceHourlyTarget:Double = 165
  val divorceRatePerSec = divorceHourlyTarget / 3600
  /* ******************************** */

  /* PIPELINE CONFIGURATION */
  val numberOfPipelineUsers:Double = 10
  /* ******************************** */

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    .inferHtmlResources()
    .silentResources

  before{
    println(s"Test Type: ${testType}")
    println(s"Test Environment: ${env}")
    println(s"Debug Mode: ${debugMode}")
  }

  val NFDCitizenSoleApp = scenario( "NFDCitizenSoleApp")
    .exitBlockOnFail {
      exec(flushHttpCache)
      .exec(flushCookieJar)
      .exec(_.set("env", s"${env}"))
      .exec(
        CreateUser.CreateCitizen("Applicant1"),
        Homepage.NFDHomepage,
        Login.NFDLogin("Applicant1"),
        NFD_01_CitizenCommon.InitialQuestions,
        NFD_02a_CitizenSoleApplicant.HowDoYouWantToApply,
        NFD_01_CitizenCommon.Jurisdictions,
        NFD_02a_CitizenSoleApplicant.EnterYourName,
        NFD_02a_CitizenSoleApplicant.EnterTheirName,
        NFD_01_CitizenCommon.MarriageNames,
        NFD_01_CitizenCommon.ContactDetails,
        NFD_02a_CitizenSoleApplicant.ContactDetails,
        NFD_01_CitizenCommon.DivorceDetailsAndUpload,
        NFD_02a_CitizenSoleApplicant.CheckYourAnswers,
        Logout.NFDLogout)
    }
    .doIf("${Applicant1EmailAddress.exists()}") {
      exec(DeleteUser.DeleteCitizen("${Applicant1EmailAddress}"))
    }

    .exec {
      session =>
        println(session)
        session
    }

  val NFDCitizenJointApp = scenario( "NFDCitizenJointApp")
    .exitBlockOnFail {
      exec(flushHttpCache)
        .exec(flushCookieJar)
        .exec(_.set("env", s"${env}"))
        .exec(
          CreateUser.CreateCitizen("Applicant1"),
          CreateUser.CreateCitizen("Applicant2"))
        //Applicant 1
        .exec(
          Homepage.NFDHomepage,
          Login.NFDLogin("Applicant1"),
          NFD_01_CitizenCommon.InitialQuestions,
          NFD_02b_CitizenJointApplicants.HowDoYouWantToApply,
          NFD_02b_CitizenJointApplicants.EnterTheirEmailAddress,
          NFD_01_CitizenCommon.Jurisdictions,
          NFD_02b_CitizenJointApplicants.EnterYourName,
          NFD_01_CitizenCommon.MarriageNames,
          NFD_01_CitizenCommon.ContactDetails,
          NFD_02b_CitizenJointApplicants.ContactDetails,
          NFD_01_CitizenCommon.DivorceDetailsAndUpload,
          NFD_02b_CitizenJointApplicants.CheckYourAnswers,
          NFD_02b_CitizenJointApplicants.SaveAndSignout,
          Logout.NFDLogout)
        //Get Access Code for Applicant 2
        .exec(NFD_03_GetJointApplicantAccessCode.GetAccessCode)
        //Applicant 2
        .exec(flushHttpCache)
        .exec(flushCookieJar)
        .exec(
          NFD_02b_CitizenJointApplicants.Applicant2Entry,
          Login.NFDLogin("Applicant2"))

    }
    .doIf("${Applicant1EmailAddress.exists()}") {
      exec(DeleteUser.DeleteCitizen("${Applicant1EmailAddress}"))
    }
    .doIf("${Applicant2EmailAddress.exists()}") {
      exec(DeleteUser.DeleteCitizen("${Applicant2EmailAddress}"))
    }

    .exec {
      session =>
        println(session)
        session
    }

  def simulationProfile(simulationType: String): Seq[OpenInjectionStep] = {
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(
            rampUsersPerSec(0.00) to (divorceRatePerSec) during (rampUpDurationMins minutes),
            constantUsersPerSec(divorceRatePerSec) during (testDurationMins minutes),
            rampUsersPerSec(divorceRatePerSec) to (0.00) during (rampDownDurationMins minutes)
          )
        }
        else{
          Seq(atOnceUsers(1))
        }
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
        Seq(global.successfulRequests.percent.gte(95),
          details("DivorceApp_300_PayYourFee").successfulRequests.count.gte((numberOfPipelineUsers * 0.8).ceil.toInt))
      case _ =>
        Seq()
    }
  }

  setUp(
    NFDCitizenSoleApp.inject(simulationProfile(testType))
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
