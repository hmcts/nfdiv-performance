package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{CsrfCheck, Environment}

import scala.concurrent.duration._

object Login {

  val BaseURL = Environment.baseURL
  val IdamURL = Environment.idamURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  //userType: Applicant1 or Applicant2
  //redirectURLSuffix: callback or callback-applicant2 (use callback-applicant2 if the user is landing on /login-applicant2 or /respondent)
  //nextPageTextCheck: unique text that appears on the subsequent page for validation
  def NFDLogin(userType: String, redirectURLSuffix: String, nextPageTextCheck: String) =

    exec {
      session =>
        session
          .set("emailAddress", session(s"${userType}EmailAddress").as[String])
          .set("password", session(s"${userType}Password").as[String])
    }

    .group("NFD_000_Login") {
        exec(http("Login Applicant1")
          .post(IdamURL + "/login?client_id=divorce&response_type=code&redirect_uri=" + BaseURL + "/oauth2/" + redirectURLSuffix)
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("username", "${emailAddress}")
          .formParam("password", "${password}")
          .formParam("save", "Sign in")
          .formParam("selfRegistrationEnabled", "true")
          .formParam("_csrf", "${csrf}")
          .check(CsrfCheck.save)
          .check(substring(nextPageTextCheck)))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
