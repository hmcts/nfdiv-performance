package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, CsrfCheck}

import scala.language.postfixOps
import scala.concurrent.duration._

object NFD_DivorceApplication {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val GetHeader = Environment.getHeader
  val PostHeader = Environment.postHeader

  val DivorceApplication =

    group("DivorceApplication_010_Homepage") {
      exec(http("Homepage")
        .get(BaseURL + "/")
        .headers(CommonHeader)
        .headers(GetHeader)
        .check(CsrfCheck.save)
        .check(regex("Some text check")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    group("DivorceApplication_020_NameSubmit") {
      exec(http("Name Submit")
        .post(BaseURL + "/name/submit")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("name", "Jon")
        .check(regex("Some text check"))
        .check(regex("a href=./get-case/([0-9]+)").find.saveAs("appId")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    group("DivorceApplication_030_GetCase") {
      exec(http("Get Case")
        .get(BaseURL + "/get-case/${appId}")
        .headers(CommonHeader)
        .headers(GetHeader)
        .check(regex("Here are ypur case details")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
