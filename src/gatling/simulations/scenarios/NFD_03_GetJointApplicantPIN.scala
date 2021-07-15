package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_03_GetJointApplicantPIN {

  val BaseURL = Environment.baseURL
  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val GetPIN =
    exec(http("NFD03CitGetPIN_010_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"nfdiv_case_api"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

    .exec(http("NFD03CitGetPIN_020_GetBearerToken")
      .post(IdamAPIURL + "/o/token")
      .formParam("grant_type", "password")
      .formParam("username", "")
      .formParam("password", "")
      .formParam("client_id", "")
      .formParam("client_secret", "")
      .formParam("scope", "")
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

}
