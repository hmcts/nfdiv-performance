package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{CsrfCheck, Environment}

import scala.concurrent.duration._

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
  def NFDLogin(userType: String, redirectURLSuffix: String, nextPageTextCheck: String) = {
    val redirectUri = s"$BaseURL/oauth2/$redirectURLSuffix"
    val encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString)
    exec {
      session =>
        session
          .set("emailAddress", session(s"${userType}EmailAddress").as[String])
          .set("password", session(s"${userType}Password").as[String])
    }

    .group("NFD_000_Login") {
        // Step 1: load IDAM sign-in landing page
        exec(http("Login Applicant1 - Load Sign In")
            .get(s"$IdamURL/o/authorize?client_id=nfdiv&response_type=code&redirect_uri=$encodedRedirectUri&scope=openid%20profile%20roles")
            .headers(CommonHeader)
            .check(status.in(200, 302, 303))
            .check(CsrfCheck.save)
            .check(substring("Sign in or create an account"))
        )
        // Step 2: submit email page
        .exec(http("Login Applicant1 - Enter Email")
           .post(IdamURL + "/enter-email")
           .headers(CommonHeader)
           .headers(PostHeader)
           .formParam("email", "#{emailAddress}")
           .formParam("_csrf", "#{csrf}")
           .check(status.in(200, 302, 303))
           .check(CsrfCheck.save)
           .check(substring("Enter your password"))
        )
        // 3) Submit password
        .exec(http("Login Applicant1 - Enter Password")
          .post(IdamURL + "/enter-password")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("password", "#{password}")
          .formParam("_csrf", "#{csrf}")
          .check(status.in(200, 302, 303))
          .check(CsrfCheck.save.optional)
          .check(substring(nextPageTextCheck))
        )
    }

    //set session variables for subsequent calls based on the userType (these are used to drive the URLs and form parameters)
    .doIfOrElse(userType.equals("Applicant1")) {
      exec(_.set("userTypeURL", "")
            .set("userType", "applicant1")
            .set("userTypeString", "appOne"))
    }
    {
      doIf(userType.equals("Applicant2")) {
        exec(_.set("userTypeURL", "applicant2/")
              .set("userType", "applicant2")
              .set("userTypeString", "appTwo"))
      }
    }

    .pause(MinThinkTime.seconds, MaxThinkTime.seconds)
  }

}
