package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment
import com.typesafe.config.ConfigFactory

object NFD_02_GetJointApplicantAccessCode {

  val BaseURL = Environment.baseURL
  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL
  val CcdAPIURL = Environment.ccdAPIURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val clientSecret = ConfigFactory.load.getString("auth.clientSecret")

  val GetAccessCode =

    exec(http("NFD02AccessCode_010_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"nfdiv_case_api"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

    .exec(http("NFD02AccessCode_020_GetBearerToken")
      .post(IdamAPIURL + "/o/token")
      .formParam("grant_type", "password")
      .formParam("username", "${Applicant1EmailAddress}")
      .formParam("password", "${Applicant1Password}")
      .formParam("client_id", "rd-professional-api")
      .formParam("client_secret", clientSecret)
      .formParam("scope", "openid profile roles openid roles profile create-user manage-user")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("bearerToken")))

    .pause(1)

    .exec(http("NFD02AccessCode_030_GetIdamID")
      .get(IdamAPIURL + "/details")
      .header("Authorization", "Bearer ${bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))

    .pause(1)

    .exec(http("NFD02AccessCode_040_GetCase")
      .get(CcdAPIURL + "/citizens/${idamId}/jurisdictions/DIVORCE/case-types/NFD/cases")
      .header("Authorization", "Bearer ${bearerToken}")
      .header("ServiceAuthorization", "${authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$[0].id").saveAs("caseId"))
      .check(jsonPath("$[0].case_data.accessCode").saveAs("accessCode")))

    .pause(1)

}
