package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_01DivorceApplication {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val ApplicationQuestions1 =
    group("DivorceApp_010_YourDetailsSubmit") {
      exec(http("Your Details Submit")
        .post(BaseURL + "/your-details")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("gender", Case.Gender.Female)
        .formParam("sameSex", Case.Checkbox.Unchecked)
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
        .check(substring("When did you get married?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_030_DateFromCertificateSubmit") {
      exec(http("Date From Certificate Submit")
        .post(BaseURL + "/date-from-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("relationshipDate-day", Common.getDay())
        .formParam("relationshipDate-month", Common.getMonth())
        .formParam("relationshipDate-year", Common.getMarriageYear())
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
        .check(substring("How do you want to apply for the divorce?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_050_HowDoYouWantToApply") {
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

    .group("DivorceApp_060_HelpWithYourFeeSubmit") {
      exec(http("Help With Your Fee Submit")
        .post(BaseURL + "/help-with-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1HelpPayingNeeded", Case.YesOrNo.No)
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
        .check(substring("Check if you can get a divorce in England and Wales")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_080_CheckJurisdictionSubmit") {
      exec(http("Check Jurisdiction Submit")
        .post(BaseURL + "/check-jurisdiction")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Where your lives are based")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_090_WhereYourLivesAreBasedSubmit") {
      exec(http("Where Your Lives Are Based Submit")
        .post(BaseURL + "/where-your-lives-are-based")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1LifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .formParam("applicant2LifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(regex("""<input class="govuk-input" id="connections" name="connections" type="hidden" value="(.+)"""").saveAs("connectionId"))
        .check(substring("You can use English or Welsh courts to apply for a divorce")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_100_CanUseEnglishWelshCourt") {
      exec(http("English or Welsh courts")
        .post(BaseURL + "/you-can-use-english-welsh-courts")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("connections", "${connectionId}")
        .check(CsrfCheck.save)
        .check(substring("Enter your name")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_110_EnterYourName") {
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

    .group("DivorceApp_120_EnterTheirName") {
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

    .group("DivorceApp_130_NamesOnYourMarriageCertificate?") {
      exec(http("Names on your marriage certificate")
        .post(BaseURL + "/your-names-on-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1FullNameOnCertificate", "${forename}" + " " + "${surname}")
        .formParam("applicant2FullNameOnCertificate", Common.randomString(5) + " " + Common.randomString(5))
        .check(CsrfCheck.save)
        .check(substring("Changes to your name")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_140_ChangesToYourName?") {
      exec(http("Changes to your name")
        .post(BaseURL + "/changes-to-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1LastNameChangedWhenRelationshipFormed", Case.YesOrNo.No)
        .formParam("applicant1NameChangedSinceRelationshipFormed", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("How the court will contact you")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
