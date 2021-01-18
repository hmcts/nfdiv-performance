package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, CsrfCheck}

//import scala.language.postfixOps
import scala.concurrent.duration._

object NFD_DivorceApplication {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val DivorceApplication =

    group("DivorceApp_010_StartApplication") {
      exec(http("Start Application")
        .get(BaseURL + "/screening-questions/language-preference")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("What language do you want us to use")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_020_LanguagePrefSubmit") {
      exec(http("Language Preference Submit")
        .post(BaseURL + "/screening-questions/language-preference")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("languagePreferenceWelsh", "No")
        .check(regex("Has your marriage broken down")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
