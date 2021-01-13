package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import utils.Environment

object NFD_GetCall {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val GetHeader = Environment.getHeader

  val NFD_API_GET_CALL =

    group("NFD_010_APIGetRequest") {
      exec(http("Make an API GET request")
        .get(BaseURL + "/api/thisAPI")
        .headers(CommonHeader)
        .headers(GetHeader)
        .check(regex("Use this service to find a court")))
    }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
