package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType

import scala.concurrent.duration._
import ccd._
import utilities.DateUtils
import utils.Common
class Create_NFD_Cases extends Simulation {

  //set the environment
  val environment = "perftest"

  /* ADDITIONAL COMMAND LINE ARGUMENT OPTIONS */
  val debugMode = System.getProperty("debug", "off") //runs a single user e.g. ./gradle gatlingRun -Ddebug=on (default: off)
  val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat

  //If running in debug mode, disable pauses between steps
  val pauseOption:PauseType = debugMode match{
    case "off" => constantPauses
    case _ => disabledPauses
  }

  val UserFeeder = csv("UserData.csv").random

  val users = 4
  val numberOfCasesToCreatePerUser = 3

  val CreateNFDCase = scenario( "CreateNFDCase")
    .exec(_.set("env", s"${env}"))
    //feed 2 users at a time (one applicant solicitor, one respondent solicitor)
    .feed(UserFeeder, 2)
    .repeat(if (debugMode == "off") numberOfCasesToCreatePerUser else 1){

      exitBlockOnFail {
        //initialise gatling session variables
        exec { session =>
          session.set("user1", session("user").as[Array[AnyRef]].toSeq.apply(0))
            .set("user2", session("user").as[Array[AnyRef]].toSeq.apply(1))
            .set("password1", session("password").as[Array[AnyRef]].toSeq.apply(0))

            .set("orgname1", session("orgname").as[Array[AnyRef]].toSeq.apply(0))
            .set("orgname2", session("orgname").as[Array[AnyRef]].toSeq.apply(1))

            .set("orgref1", session("orgref").as[Array[AnyRef]].toSeq.apply(0))
            .set("orgref2", session("orgref").as[Array[AnyRef]].toSeq.apply(1))

            .set("applicant1FirstName", ("App1" + Common.randomString(5)))
            .set("applicant1LastName", ("Test" + Common.randomString(5)))
            .set("applicant2FirstName", ("App2" + Common.randomString(5)))
            .set("applicant2LastName", ("Test" + Common.randomString(5)))

            .set("marriageDate",
              DateUtils.getDatePastRandom("yyyy-MM-dd", minYears = 10, maxYears = 50, minMonths = 0, maxMonths = 11, minDays = 0, maxDays = 30))
        }

        //first upload a document
        .exec(CcdHelper.uploadDocumentToCdam(
          userEmail = "#{user1}",
          userPassword = "#{password1}",
          caseType = CcdCaseTypes.DIVORCE_NFD,
          filepath = "testFile.pdf"
        ))

        //create a case
        .exec(CcdHelper.createCase(
          "#{user1}",
          "#{password1}",
          CcdCaseTypes.DIVORCE_NFD,
          "solicitor-create-application",
          "createCase.json"
        ))

      }
      .pause(100.millis)

    }

  //defines the Gatling simulation model, based on the inputs
  def simulationProfile(users: Int): Seq[OpenInjectionStep] = {
    if (debugMode == "off") {
      Seq(
        rampUsers(users).during(10.seconds)
      )
    }
    else{
      Seq(atOnceUsers(1))
    }
  }


  setUp(
    CreateNFDCase.inject(simulationProfile(users)).pauses(pauseOption)
  ).protocols(http)

}
