package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Environment, CsrfCheck}

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
        .get(BaseURL + "/your-details")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("Who are you applying to divorce?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_010_YourDetailsSubmit") {
      exec(http("Your Details Submit")
        .post(BaseURL + "/your-details")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("gender", Case.Gender.Female)
        .formParam("sameSex", Case.Checkbox.Unchecked)
        .check(substring("Has your marriage irretrievably broken down (it cannot be saved)?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_020_MarriageBrokenDownSubmit") {
      exec(http("Marriage Broken Down Submit")
        .post(BaseURL + "/irretrievable-breakdown")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("screenHasUnionBroken", Case.YesOrNo.Yes)
        .check(substring("When did you get married?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_030_DateFromCertificateSubmit") {
      exec(http("Date From Certificate Submit")
        .post(BaseURL + "/date-from-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("relationshipDate-day", "1")
        .formParam("relationshipDate-month", "1")
        .formParam("relationshipDate-year", "2000")
        .check(substring("Do you have your marriage certificate with you?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_040_HasMarriageCertificateSubmit") {
      exec(http("Has Marriage Certificate Submit")
        .post(BaseURL + "/do-you-have-your-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("hasCertificate", Case.YesOrNo.Yes)
        .check(substring("Do you need help paying the fee for your divorce?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_050_HelpWithYourFeeSubmit") {
      exec(http("Help With Your Fee Submit")
        .post(BaseURL + "/help-with-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("helpPayingNeeded", Case.YesOrNo.Yes)
        .check(substring("Have you already applied for help with your divorce fee?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_060_AlreadyAppliedHelpWithYourFeeSubmit") {
      exec(http("Already Applied Help With Your Fee Submit")
        .post(BaseURL + "/have-you-applied-for-help-with-fees")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("alreadyAppliedForHelpPaying", Case.YesOrNo.Yes)
        .formParam("helpWithFeesRefNo", "HWF-A1B-23A")
        .check(substring("Did you get married in the UK?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_070_InTheUKSubmit") {
      exec(http("In The UK Submit")
        .post(BaseURL + "/in-the-uk")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("inTheUk", Case.YesOrNo.Yes)
        .check(substring("Check if you can get a divorce in England and Wales")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_080_CheckJurisdictionSubmit") {
      exec(http("Check Jurisdiction Submit")
        .post(BaseURL + "/check-jurisdiction")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Where your lives are based")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_090_WhereYourLivesAreBasedSubmit") {
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
}
