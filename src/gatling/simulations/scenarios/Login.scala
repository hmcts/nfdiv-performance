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


  val NFDLogin =

    group("NFD_000_Login") {

      exec(http("Login")
        .post(IdamURL + "/login?client_id=divorce&response_type=code&redirect_uri=" + BaseURL + "/oauth2/callback")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("username", "${emailAddress}")
        .formParam("password", "${password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "true")
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Who are you applying to divorce?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)


}
