package simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.pause.PauseType
import scenarios._
import utils.Environment
import ccd._

import scala.concurrent.duration._

class NFD_Simulation extends Simulation {

  val BaseURL = Environment.baseURL

  val CaseWorkerUserFeeder = csv("UserDataCW.csv").circular
  val LegalAdvisorUserFeeder = csv("UserDataLA.csv").circular

  /* TEST TYPE DEFINITION */
  /* pipeline = nightly pipeline against the AAT environment (see the Jenkins_nightly file) */
  /* perftest (default) = performance test against the perftest environment */
  val testType = scala.util.Properties.envOrElse("TEST_TYPE", "perftest")

  //set the environment based on the test type
  val environment = testType match{
    case "perftest" => "perftest"
    case "pipeline" => "perftest" //updated pipeline to run against perftest - change to aat to run against AAT
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

  val divorceHourlyTargetSole:Double = 80
  val divorceHourlyTargetJoint:Double = 80
  val divorceRatePerSecSole = divorceHourlyTargetSole / 3600
  val divorceRatePerSecJoint = divorceHourlyTargetJoint / 3600

  //If running in debug mode, disable pauses between steps
  val pauseOption:PauseType = debugMode match{
    case "off" => constantPauses
    case _ => customPauses(2000.toLong)
  }
  /* ******************************** */

  /* PIPELINE CONFIGURATION */
  val numberOfPipelineUsersSole:Double = 5
  val numberOfPipelineUsersJoint:Double = 5
  /* ******************************** */

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    .inferHtmlResources(DenyList(".*webchat-client.pp.ctsc.hmcts.net.*"))
    .silentResources

  before{
    println(s"Test Type: ${testType}")
    println(s"Test Environment: ${env}")
    println(s"Debug Mode: ${debugMode}")
  }

  val NFDCitizenSoleApp = scenario( "NFDCitizenSoleApp")
    .exitBlockOnFail {
      exec(  _.set("env", s"${env}")
              .set("appType", "sole"))
      //Applicant 1 - Divorce Application
      .exec(
        CreateUser.CreateCitizen("Applicant1"),
        CreateUser.CreateCitizen("Applicant2"),
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "Who are you applying to divorce?"),
        NFD_01_CitizenApplication.LandingPage,
        NFD_01_CitizenApplication.MarriageBrokenDown,
        NFD_01_CitizenApplication.MarriageCertificate,
        NFD_01_CitizenApplication.HowDoYouWantToApply,
        NFD_01_CitizenApplication.Jurisdictions,
        NFD_01_CitizenApplication.EnterYourName,
        NFD_01_CitizenApplication.EnterTheirName,
        NFD_01_CitizenApplication.YourContactDetails,
        NFD_01_CitizenApplication.TheirContactDetails,
        NFD_01_CitizenApplication.DivorceDetails,
        NFD_01_CitizenApplication.DocumentUpload,
        NFD_01_CitizenApplication.CheckYourAnswersSole,
        NFD_01_CitizenApplication.PayAndSubmit,
        Logout.NFDLogout)
      //Caseworker - Get Marriage Details & Issue Application
      .feed(CaseWorkerUserFeeder)
      .exec(
        CCDAPI.GetMarriageDetails,
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-issue-application", "bodies/events/IssueApplication.json"))
      //Applicant 1 - Get Access Code for Applicant 2
      .exec(
        CCDAPI.GetAccessCode)
      //Applicant 2 - Respond to Divorce Application
      .exec(
        Homepage.NFDHomepage("respondent"),
        Login.NFDLogin("Applicant2", "callback-applicant2", "Enter your access details"),
        NFD_02_CitizenRespondent.RespondentApplication,
        Logout.NFDLogout)
      //Caseworker - Mark the Case as Awaiting Conditional Order (to bypass 20-week holding) and set the dueDate
      .exec(
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-progress-held-case", "bodies/events/AwaitingConditionalOrder.json"),
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-update-due-date", "bodies/events/SetCOEligibilityDates.json"))
      //Applicant 1 - Apply for Conditional Order
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "You can now apply for a ‘conditional order’"),
        NFD_03_CitizenApplyForCO.ApplyForConditionalOrder,
        NFD_03_CitizenApplyForCO.ContinueWithConditionalOrderSole,
        NFD_03_CitizenApplyForCO.CompleteConditionalOrder,
        Logout.NFDLogout)
      //Legal Advisor - Grant Conditional Order
      .feed(LegalAdvisorUserFeeder)
      .exec(
        CcdHelper.addCaseEvent("#{la-user}", "#{la-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "legal-advisor-make-decision", "bodies/events/MakeDecision.json"))
      //Caseworker - Make Eligible for Final Order
      .exec(
        //link with bulk case
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-link-with-bulk-case", "bodies/events/LinkWithBulkCase.json"),
        //set case hearing and decision dates to a date in the past
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-update-case-court-hearing", "bodies/events/UpdateCaseWithCourtHearing.json"),
        //set judge details, CO granted and issued dates in the past
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-amend-case", "bodies/events/SetCODetails.json"),
        //pronounce case
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-pronounce-case", "bodies/events/PronounceCase.json"),
        //set final order eligibility dates
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-amend-case", "bodies/events/SetFOEligibilityDates.json"),
        //set case as awaiting final order
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-progress-case-awaiting-final-order", "bodies/events/AwaitingFinalOrder.json"))
      //Applicant 1 - Apply for Final Order
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "You may now apply for a &#39;final order&#39;"),
        NFD_04_CitizenApplyForFO.ApplyForFinalOrder,
        Logout.NFDLogout)
      //Caseworker - Grant Final Order
      .exec(
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-grant-final-order", "bodies/events/GrantFinalOrder.json"))
    }

    .doIf("#{Applicant1EmailAddress.exists()}") {
      exec(DeleteUser.DeleteCitizen("#{Applicant1EmailAddress}"))
    }
    .doIf("#{Applicant2EmailAddress.exists()}") {
      exec(DeleteUser.DeleteCitizen("#{Applicant2EmailAddress}"))
    }

  val NFDCitizenJointApp = scenario( "NFDCitizenJointApp")
    .exitBlockOnFail {
      exec(  _.set("env", s"${env}")
              .set("appType", "joint"))
      .exec(
        CreateUser.CreateCitizen("Applicant1"),
        CreateUser.CreateCitizen("Applicant2"))
      //Applicant 1 - Divorce Application
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "Who are you applying to divorce?"),
        NFD_01_CitizenApplication.LandingPage,
        NFD_01_CitizenApplication.MarriageBrokenDown,
        NFD_01_CitizenApplication.MarriageCertificate,
        NFD_01_CitizenApplication.HowDoYouWantToApply,
        NFD_01_CitizenApplication.EnterTheirEmailAddress,
        NFD_01_CitizenApplication.Jurisdictions,
        NFD_01_CitizenApplication.EnterYourNames,
        NFD_01_CitizenApplication.YourContactDetails,
        NFD_01_CitizenApplication.DivorceDetails,
        NFD_01_CitizenApplication.DocumentUpload,
        NFD_01_CitizenApplication.CheckYourAnswersJointApplicant1,
        NFD_01_CitizenApplication.ExitService)
      //Applicant 1 - Get Case ID and Access Code for Applicant 2
      .feed(CaseWorkerUserFeeder)
      .exec(CCDAPI.GetAccessCode)
      //Applicant 2 - Respond to Divorce Application
      .exec(
        Homepage.NFDHomepage("login-applicant2"),
        Login.NFDLogin("Applicant2", "callback-applicant2", "Enter your access details"),
        NFD_01_CitizenApplication.Applicant2ContinueApplication,
        NFD_01_CitizenApplication.MarriageBrokenDown,
        NFD_01_CitizenApplication.EnterYourNames,
        NFD_01_CitizenApplication.YourContactDetails,
        NFD_01_CitizenApplication.DivorceDetails,
        NFD_01_CitizenApplication.CheckYourAnswersJointApplicant2,
        NFD_01_CitizenApplication.ConfirmYourJointApplication,
        NFD_01_CitizenApplication.SaveAndSignout)
      //Applicant 1 - Confirm Application
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "Confirm your joint application"),
        NFD_01_CitizenApplication.ConfirmYourJointApplication,
        NFD_01_CitizenApplication.PayAndSubmit,
        Logout.NFDLogout)
      //Caseworker - Get Marriage Details & Issue Application
      .exec(
        CCDAPI.GetMarriageDetails,
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-issue-application", "bodies/events/IssueApplication.json"))
      //Applicant 1 - Confirm Receipt
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "will be checked by court staff"),
        NFD_01_CitizenApplication.ConfirmReceipt,
        Logout.NFDLogout)
      //Applicant 2 - Confirm Receipt
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant2", "callback", "will be checked by court staff"),
        NFD_01_CitizenApplication.ConfirmReceipt,
        Logout.NFDLogout)
      //Caseworker - Mark the Case as Awaiting Conditional Order (to bypass 20-week holding) and set the dueDate
      .exec(
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-progress-held-case", "bodies/events/AwaitingConditionalOrder.json"),
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-update-due-date", "bodies/events/SetCOEligibilityDates.json"))
      //Applicant 1 - Apply for Conditional Order
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "You can now apply for a ‘conditional order’"),
        NFD_03_CitizenApplyForCO.ApplyForConditionalOrder,
        NFD_03_CitizenApplyForCO.ContinueWithConditionalOrderJoint,
        NFD_03_CitizenApplyForCO.CompleteConditionalOrder,
        Logout.NFDLogout)
      //Applicant 2 - Apply for Conditional Order
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant2", "callback", "You can now apply for a ‘conditional order’"),
        NFD_03_CitizenApplyForCO.ApplyForConditionalOrder,
        NFD_03_CitizenApplyForCO.ContinueWithConditionalOrderJoint,
        NFD_03_CitizenApplyForCO.CompleteConditionalOrder,
        Logout.NFDLogout)
      //Legal Advisor - Grant Conditional Order
      .feed(LegalAdvisorUserFeeder)
      .exec(
        CcdHelper.addCaseEvent("#{la-user}", "#{la-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "legal-advisor-make-decision", "bodies/events/MakeDecision.json"))
      //Caseworker - Make Eligible for Final Order
      .exec(
        //link with bulk case
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-link-with-bulk-case", "bodies/events/LinkWithBulkCase.json"),
        //set case hearing and decision dates to a date in the past
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-update-case-court-hearing", "bodies/events/UpdateCaseWithCourtHearing.json"),
        //set judge details, CO granted and issued dates in the past
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-amend-case", "bodies/events/SetCODetails.json"),
        //pronounce case
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-pronounce-case", "bodies/events/PronounceCase.json"),
        //set final order eligibility dates
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-amend-case", "bodies/events/SetFOEligibilityDates.json"),
        //set case as awaiting final order
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "system-progress-case-awaiting-final-order", "bodies/events/AwaitingFinalOrder.json"))
      //Applicant 1 - Apply for Final Order
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant1", "callback", "You can now apply for a ‘final order’"),
        NFD_04_CitizenApplyForFO.ApplyForFinalOrder,
        Logout.NFDLogout)
      //Applicant 2 - Apply for Final Order
      .exec(
        Homepage.NFDHomepage(""),
        Login.NFDLogin("Applicant2", "callback", "You can now apply for a ‘final order’"),
        NFD_04_CitizenApplyForFO.ApplyForFinalOrder,
        Logout.NFDLogout)
      //Caseworker - Grant Final Order
      .exec(
        CcdHelper.addCaseEvent("#{cw-user}", "#{cw-password}", CcdCaseTypes.DIVORCE_NFD, "#{caseId}", "caseworker-grant-final-order", "bodies/events/GrantFinalOrder.json"))
    }

    .doIf("#{Applicant1EmailAddress.exists()}") {
      exec(DeleteUser.DeleteCitizen("#{Applicant1EmailAddress}"))
    }
    .doIf("#{Applicant2EmailAddress.exists()}") {
      exec(DeleteUser.DeleteCitizen("#{Applicant2EmailAddress}"))
    }

  //defines the Gatling simulation model, based on the inputs
  def simulationProfile(simulationType: String, userPerSecRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(
            rampUsersPerSec(0.00) to (userPerSecRate) during (rampUpDurationMins.minutes),
            constantUsersPerSec(userPerSecRate) during (testDurationMins.minutes),
            rampUsersPerSec(userPerSecRate) to (0.00) during (rampDownDurationMins.minutes)
          )
        }
        else{
          Seq(atOnceUsers(1))
        }
      case "pipeline" =>
        Seq(rampUsers(numberOfPipelineUsers.toInt) during (2.minutes))
      case _ =>
        Seq(nothingFor(0))
    }
  }

  //defines the test assertions, based on the test type
  def assertions(simulationType: String): Seq[Assertion] = {
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(global.successfulRequests.percent.gte(95),
            details("CCD_SubmitEvent_caseworker-grant-final-order").successfulRequests.percent.gte(80))
        }
        else {
          Seq(global.successfulRequests.percent.is(100))
        }
      case "pipeline" =>
        Seq(global.successfulRequests.percent.gte(95),
          details("CCD_SubmitEvent_caseworker-grant-final-order").successfulRequests.count.gte(((numberOfPipelineUsersSole + numberOfPipelineUsersJoint) * 0.8).ceil.toInt)
        )
      case _ =>
        Seq()
    }
  }

  setUp(
    NFDCitizenSoleApp.inject(simulationProfile(testType, divorceRatePerSecSole, numberOfPipelineUsersSole)).pauses(pauseOption),
    NFDCitizenJointApp.inject(simulationProfile(testType, divorceRatePerSecJoint, numberOfPipelineUsersJoint)).pauses(pauseOption)
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
