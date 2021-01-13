package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment

import scala.language.postfixOps
import scala.concurrent.duration._

object NFD_PostCall {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val NFD_API_POST_CALL =

    group("NFD_020_APIPostRequest") {
      exec(http("Make an API POST request")
        .post(BaseURL + "/api/thatAPI")
        .headers(CommonHeader)
        .headers(PostHeader)
        .check(jsonPath("$.id").find.saveAs("id")))
    }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
