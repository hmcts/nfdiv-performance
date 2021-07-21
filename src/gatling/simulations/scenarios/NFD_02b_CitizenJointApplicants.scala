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

  val postcodeFeeder = csv("postcodes.csv").random

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
        .formParam("applicant2EmailAddress", "${Applicant2EmailAddress}")
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

  val Applicant2Entry =

    group("DivorceApp_310_App2LandingPage") {

      exec(http("Applicant2 Landing Page")
        .get(BaseURL + "/login-applicant2")
        .headers(CommonHeader)
        .header("sec-fetch-site", "none")
        .check(CsrfCheck.save)
        .check(substring("Sign in or create an account")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val Applicant2Application =

    group("DivorceApp_320_App2AccessCodeSubmit") {

      exec(http("Applicant2 Submit Access Code")
        .post(BaseURL + "/applicant2/enter-your-access-code")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("caseReference", "${caseId}")
        .formParam("accessCode", "${accessCode}")
        .check(CsrfCheck.save)
        .check(substring("You need to review your joint application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_330_App2ContinueApp") {

      exec(http("Applicant2 Continue Application")
        .post(BaseURL + "/applicant2/you-need-to-review-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Has your marriage irretrievably broken down (it cannot be saved)?")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_340_App2MarriageBrokenDownSubmit") {

      exec(http("Applicant2 Marriage Broken Down Submit")
        .post(BaseURL + "/applicant2/irretrievable-breakdown")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("screenHasApplicant2UnionBroken", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Enter your name")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_350_App2EnterYourName") {

      exec(http("Applicant2 Enter your name")
        .post(BaseURL + "/applicant2/enter-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2FirstNames", Common.randomString(5))
        .formParam("applicant2MiddleNames", "")
        .formParam("applicant2LastNames", Common.randomString(5))
        .check(CsrfCheck.save)
        .check(substring("Changes to your name")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_360_App2ChangesToYourName?") {

      exec(http("Applicant2 Changes to your name")
        .post(BaseURL + "/applicant2/changes-to-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2LastNameChangedWhenRelationshipFormed", Case.YesOrNo.No)
        .formParam("applicant2NameChangedSinceRelationshipFormed", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("How the court will contact you")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_370_App2HowTheCourtWillContactYou?") {

      exec(http("Applicant2 How the court will contact you")
        .post(BaseURL + "/applicant2/how-the-court-will-contact-you")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("applicant2AgreeToReceiveEmails", List("", Case.Checkbox.Checked))
        .formParam("applicant2PhoneNumber", "")
        .check(CsrfCheck.save)
        .check(substring("What language do you want to receive emails and documents in?")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_380_App2LanguageToReceiveEmailsAndDocs?") {

      exec(http("Applicant2 English or Welsh?")
        .post(BaseURL + "/applicant2/english-or-welsh")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2EnglishOrWelsh", "english")
        .check(CsrfCheck.save)
        .check(substring("Do you need your contact details kept private from your husband?")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_390_App2DetailsKeptPrivate?") {

      exec(http("Applicant2 Keep contact details private from your husband?")
        .post(BaseURL + "/applicant2/address-private")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2AddressPrivate", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Enter your postal address")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_400_App2EnterYourPostcode") {
      feed(postcodeFeeder)
        .exec(http("Applicant2 Enter your postcode")
          .post(BaseURL + "/postcode-lookup")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("postcode", "${postcode}")
          .check(regex(""""fullAddress":"(?:.+?)","street1":"(.*?)","street2":"(.*?)","town":"(.*?)","county":"*(.*?)"*,"postcode":"(.+?)"""")
            .ofType[(String, String, String, String, String)].findRandom.saveAs("addressLines")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_410_App2EnterYourPostalAddress") {
      exec(http("Applicant2 Enter your postal address")
        .post(BaseURL + "/applicant2/enter-your-address")
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
        //.check(regex("Other court cases")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  //CONTINUE DEVELOPMENT ONCE THE REST OF THE FUNCTIONALITY IS COMPLETED

}
