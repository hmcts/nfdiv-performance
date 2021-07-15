package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_02a_CitizenSoleApplicant {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val postcodeFeeder = csv("postcodes.csv").random

  val HowDoYouWantToApply =
    group("DivorceApp_050_HowDoYouWantToApply") {
      exec(http("How do you want to apply")
        .post(BaseURL + "/how-do-you-want-to-apply")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicationType", "soleApplication")
        .check(CsrfCheck.save)
        .check(substring("Do you need help paying the fee for your divorce?")))
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
        .check(substring("Enter your wife’s name")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val EnterTheirName =

    group("DivorceApp_120_EnterTheirName") {
      exec(http("Enter Their name")
        .post(BaseURL + "/enter-their-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2FirstNames", Common.randomString(5))
        .formParam("applicant2MiddleNames", "")
        .formParam("applicant2LastNames", Common.randomString(5))
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
        .check(regex("Enter your wife(&.+;)s email address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_200_EnterTheirEmailAddress") {
      exec(http("Enter your wife's email address")
        .post(BaseURL + "/their-email-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2EmailAddress", Common.randomString(5) + "@test.com")
        .formParam("doNotKnowApplicant2EmailAddress", "")
        .check(CsrfCheck.save)
        .check(regex("Do you have your wife(&.+;)s postal address?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_210_DoYouKnowTheirAddress") {
      exec(http("Do you have your wife's postal address?")
        .post(BaseURL + "/do-you-have-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1KnowsApplicant2Address", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Enter your wife’s postal address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_220_EnterTheirPostcode") {
      feed(postcodeFeeder)
        .exec(http("Enter their postcode")
          .post(BaseURL + "/postcode-lookup")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("postcode", "${postcode}")
          .check(regex(""""fullAddress":"(?:.+?)","street1":"(.*?)","street2":"(.*?)","town":"(.*?)","county":"*(.*?)"*,"postcode":"(.+?)"""")
            .ofType[(String, String, String, String, String)].findRandom.saveAs("addressLines")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_230_TheirAddress") {
      exec(http("Enter your wife’s postal address")
        .post(BaseURL + "/enter-their-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2Address1", "${addressLines(0)}")
        .formParam("applicant2Address2", "${addressLines(1)}")
        .formParam("applicant2Address3", "")
        .formParam("applicant2AddressTown", "${addressLines(2)}")
        .formParam("applicant2AddressCounty", "${addressLines(3)}")
        .formParam("applicant2AddressPostcode", "${addressLines(4)}")
        .formParam("applicant2AddressCountry", "UK")
        .check(CsrfCheck.save)
        .check(substring("Other court cases")))
    }

  val CheckYourAnswers =

    group("DivorceApp_290_CheckYourAnswers") {
      exec(http("Check your answers")
        .post(BaseURL + "/check-your-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("iConfirmPrayer", List("", Case.Checkbox.Checked))
        .multivaluedFormParam("iBelieveApplicationIsTrue", List("", Case.Checkbox.Checked))
        .check(CsrfCheck.save)
        .check(substring("Pay your divorce fee")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
