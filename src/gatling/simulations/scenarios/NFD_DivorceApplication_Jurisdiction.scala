package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Environment}

import scala.concurrent.duration._

object NFD_DivorceApplication_Jurisdiction {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val Jurisdiction =
    group("DivorceApp_010_CheckJurisdictionSubmit") {
      exec(http("Check Jurisdiction Submit")
        .post(BaseURL + "/check-jurisdiction")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Where your lives are based")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_020_WhereYourLivesAreBasedSubmit") {
      exec(http("Where Your Lives Are Based Submit")
        .post(BaseURL + "/where-your-lives-are-based")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("yourLifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .formParam("partnersLifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .check(substring("You can use the English or Welsh court....")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceAppJurisdiction_030_WhereYourLivesAreBasedSubmit") {
      exec(http("Where Your Lives Are Based Submit")
        .post(BaseURL + "/where-your-lives-are-based")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("yourLifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .formParam("partnersLifeBasedInEnglandAndWales", Case.YesOrNo.No)
        .check(substring("Have you been living in England or Wales for the last 12 months?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceAppJurisdiction_040_Last12MonthsSubmit") {
      exec(http("Last 12 Months Submit")
        .post(BaseURL + "/living-england-wales-twelve-months")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("livingInEnglandWalesTwelveMonths", Case.YesOrNo.Yes)
        .check(substring("You can use the English or Welsh court....")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)
}
