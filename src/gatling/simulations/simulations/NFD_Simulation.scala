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
  val numberOfPipelineUsersSole:Double = 5
  val numberOfPipelineUsersJoint:Double = 5
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
      .exec(  _.set("env", s"${env}")
              .set("appType", "sole")
              .set("userTypeURL", "")
              .set("userType", "applicant1")
              .set("union", "screenHasUnionBroken"))
      .exec(
        CreateUser.CreateCitizen("Applicant1"),
        Homepage.NFDHomepage,
        Login.NFDLogin("Applicant1"),
        NFD_01_CitizenApplication.LandingPage,
        NFD_01_CitizenApplication.MarriageBrokenDown,
        NFD_01_CitizenApplication.MarriageCertificate,
        NFD_01_CitizenApplication.HowDoYouWantToApply,
        NFD_01_CitizenApplication.Jurisdictions,
        NFD_01_CitizenApplication.EnterYourName,
        NFD_01_CitizenApplication.EnterTheirName,
        NFD_01_CitizenApplication.MarriageCertNames,
        NFD_01_CitizenApplication.YourContactDetails,
        NFD_01_CitizenApplication.TheirContactDetails,
        NFD_01_CitizenApplication.DivorceDetailsAndUpload,
        NFD_01_CitizenApplication.CheckYourAnswersSole,
        NFD_01_CitizenApplication.PayAndSubmit,
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
        .exec(  _.set("env", s"${env}")
                .set("appType", "joint")
                .set("userTypeURL", "")
                .set("userType", "applicant1")
                .set("union", "screenHasUnionBroken"))
        .exec(
          CreateUser.CreateCitizen("Applicant1"),
          CreateUser.CreateCitizen("Applicant2"))
        //Applicant 1
        .exec(
          Homepage.NFDHomepage,
          Login.NFDLogin("Applicant1"),
          NFD_01_CitizenApplication.LandingPage,
          NFD_01_CitizenApplication.MarriageBrokenDown,
          NFD_01_CitizenApplication.MarriageCertificate,
          NFD_01_CitizenApplication.HowDoYouWantToApply,
          NFD_01_CitizenApplication.EnterTheirEmailAddress,
          NFD_01_CitizenApplication.Jurisdictions,
          NFD_01_CitizenApplication.EnterYourName,
          NFD_01_CitizenApplication.MarriageCertNames,
          NFD_01_CitizenApplication.YourContactDetails,
          NFD_01_CitizenApplication.DivorceDetailsAndUpload,
          NFD_01_CitizenApplication.CheckYourAnswersJoint,
          NFD_01_CitizenApplication.SaveAndSignout,
          Logout.NFDLogout)
        //Get Access Code for Applicant 2
        .exec(flushHttpCache)
        .exec(flushCookieJar)
        .exec(NFD_02_GetJointApplicantAccessCode.GetAccessCode)
        //Applicant 2
        .exec(flushHttpCache)
        .exec(flushCookieJar)
        .exec(  _.set("userTypeURL", "applicant2/")
                .set("userType", "applicant2")
                .set("union", "applicant2ScreenHasUnionBroken"))
        .exec(
          NFD_01_CitizenApplication.Applicant2LandingPage,
          Login.NFDLogin("Applicant2"),
          NFD_01_CitizenApplication.Applicant2ContinueApplication,
          NFD_01_CitizenApplication.MarriageBrokenDown,
          NFD_01_CitizenApplication.EnterYourName,
          NFD_01_CitizenApplication.YourContactDetails,
          //TODO: ADD MORE OF THE FLOW HERE ONCE DEVELOPED
          Logout.NFDLogout)
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

  def simulationProfile(simulationType: String, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(
            //TODO: UPDATE THIS TO CATER FOR SOLE/JOINT APPLICATIONS
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
        //TODO: UPDATE ASSERTION FOR JOINT APPLICATION ONCE DEVELOPED
        Seq(global.successfulRequests.percent.gte(95),
          details("DivorceApp_330_ConfirmPayment").successfulRequests.count.gte((numberOfPipelineUsersSole * 0.8).ceil.toInt),
          details("DivorceApp_370_App2ContinueApp").successfulRequests.count.gte((numberOfPipelineUsersJoint * 0.8).ceil.toInt)
        )
      case _ =>
        Seq()
    }
  }

  setUp(
    NFDCitizenSoleApp.inject(simulationProfile(testType, numberOfPipelineUsersSole)),
    NFDCitizenJointApp.inject(simulationProfile(testType, numberOfPipelineUsersJoint))
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
