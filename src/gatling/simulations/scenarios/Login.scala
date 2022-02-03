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


  def NFDLogin(userType: String) =

    exec {
      session =>
        session
          .set("emailAddress", session(s"${userType}EmailAddress").as[String])
          .set("password", session(s"${userType}Password").as[String])
    }

    .group(s"NFD_000_Login_${userType}") {

      doIfOrElse(userType.equals("Applicant1")) {

        exec(http("Login Applicant1")
          .post(IdamURL + "/login?client_id=divorce&response_type=code&redirect_uri=" + BaseURL + "/oauth2/callback")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("username", "${emailAddress}")
          .formParam("password", "${password}")
          .formParam("save", "Sign in")
          .formParam("selfRegistrationEnabled", "true")
          .formParam("_csrf", "${csrf}")
          .check(CsrfCheck.save)
          .check(regex("Who are you applying to divorce?|Confirm your joint application|Your application for divorce has been submitted|You can now apply for a ‘conditional order’")))

      }
      {
        doIf(userType.equals("Applicant2")) {
          exec(http("Login Applicant2")
            .post(IdamURL + "/login?client_id=divorce&response_type=code&redirect_uri=" + BaseURL + "/oauth2/callback-applicant2")
            .headers(CommonHeader)
            .headers(PostHeader)
            .formParam("username", "${emailAddress}")
            .formParam("password", "${password}")
            .formParam("save", "Sign in")
            .formParam("selfRegistrationEnabled", "true")
            .formParam("_csrf", "${csrf}")
            .check(CsrfCheck.save)
            .check(substring("Enter your access details")))
        }
      }
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
