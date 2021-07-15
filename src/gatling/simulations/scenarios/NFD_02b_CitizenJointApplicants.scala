package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_02b_CitizenJointApplicants {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val HowDoYouWantToApply =
    group("DivorceApp_050_HowDoYouWantToApply") {
      exec(http("How do you want to apply")
        .post(BaseURL + "/how-do-you-want-to-apply")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicationType", "jointApplication")
        .check(CsrfCheck.save)
        .check(substring("Enter your wife&#39;s email address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val EnterTheirEmailAddress =

    group("DivorceApp_055_EnterTheirEmailAddress") {
      exec(http("Enter their email address")
        .post(BaseURL + "/their-email-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2EmailAddress", "perftest165@perftest12345.com")
        .formParam("applicant1DoesNotKnowApplicant2EmailAddress", "")
        .check(CsrfCheck.save)
        .check(substring("Help paying the divorce fee")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val EnterYourName =

    group("DivorceApp_110_EnterYourName") {
      exec(http("Enter your name")
        .post(BaseURL + "/enter-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1FirstNames", "${forename}")
        .formParam("applicant1MiddleNames", "")
        .formParam("applicant1LastNames", "${surname}")
        .check(CsrfCheck.save)
        .check(substring("Your names on your marriage certificate")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val ContactDetails =

    group("DivorceApp_190_EnterYourPostalAddress") {
      exec(http("Enter your postal address")
        .post(BaseURL + "/enter-your-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1Address1", "${addressLines(0)}")
        .formParam("applicant1Address2", "${addressLines(1)}")
        .formParam("applicant1Address3", "")
        .formParam("applicant1AddressTown", "${addressLines(2)}")
        .formParam("applicant1AddressCounty", "${addressLines(3)}")
        .formParam("applicant1AddressPostcode", "${addressLines(4)}")
        .formParam("applicant1AddressCountry", "UK")
        .check(CsrfCheck.save)
        .check(regex("Other court cases")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val CheckYourAnswers =

    group("DivorceApp_290_CheckYourAnswers") {
      exec(http("Check your answers")
        .post(BaseURL + "/check-your-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicationType", "jointApplication")
        .check(CsrfCheck.save)
        .check(substring("Your answers have been sent to your wife to review")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val SaveAndSignout =

    group("DivorceApp_300_SaveAndSignout") {
      exec(http("Save and signout")
        .get(BaseURL + "/save-and-sign-out?lng=en")
        .headers(CommonHeader)
        .check(substring("Your application has been saved")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
