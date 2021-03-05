package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, CsrfCheck}

import scala.concurrent.duration._

object NFD_DivorceApplication {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val DivorceApplication =

    group("DivorceApp_010_StartApplication") {
      exec(http("Start-No Fault Divorce Application")
        .get(BaseURL + "/irretrievable-breakdown")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("Has your marriage irretrievably broken down")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    group("DivorceApp_020_YourDetails") {
          exec(http("Your Details")
            .get(BaseURL + "/your-details")
            .headers(CommonHeader)
            .check(CsrfCheck.save)
            .check(substring("Who are you applying to divorce?")))
        }
   .pause(MinThinkTime seconds, MaxThinkTime seconds)

   group("DivorceApp_030_MarrigeBrokenDown?") {
             exec(http("Has your marriage irretrievably broken down")
               .get(BaseURL + "/irretrievable-breakdown")
               .headers(CommonHeader)
               .check(CsrfCheck.save)
               .check(substring("Has your marriage irretrievably broken down (it cannot be saved)?")))
           }
   .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
