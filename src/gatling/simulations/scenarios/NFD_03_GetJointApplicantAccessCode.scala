package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_03_GetJointApplicantAccessCode {

  val BaseURL = Environment.baseURL
  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL
  val CcdAPIURL = Environment.ccdAPIURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val GetAccessCode =

    exec(http("NFD03CitGetPIN_010_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"nfdiv_case_api"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

    .exec(http("NFD03CitGetPIN_020_GetBearerToken")
      .post(IdamAPIURL + "/o/token")
      .formParam("grant_type", "password")
      .formParam("username", "${Applicant1EmailAddress}")
      .formParam("password", "${Applicant1Password}")
      .formParam("client_id", "divorce")
      .formParam("client_secret", "thUphEveC2Ekuqedaneh4jEcRuba4t2t")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("bearerToken")))

    .pause(1)

    .exec(http("NFD03CitGetPIN_030_GetIdamID")
      .get(IdamAPIURL + "/details")
      .header("Authorization", "Bearer ${bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))

    .pause(1)

    .exec(http("NFD03CitGetPIN_040_GetCase")
      .get(CcdAPIURL + "/citizens/${idamId}/jurisdictions/DIVORCE/case-types/NO_FAULT_DIVORCE18/cases/${caseId}")
      .header("Authorization", "Bearer ${bearerToken}")
      .header("ServiceAuthorization", "${authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$.case_data.accessCode").saveAs("accessCode")))

    .pause(1)

}
