package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_04_CitizenRespondent {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val RespondentHomepage =

    group("NFD04CitResp_010_RespLandingPage") {

      exec(http("Respondent Landing Page")
        .get(BaseURL + "/respondent")
        .headers(CommonHeader)
        .header("sec-fetch-site", "none")
        .check(CsrfCheck.save)
        .check(substring("Sign in or create an account")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val RespondentApplication =

    group("NFD04CitResp_020_RespAccessCodeSubmit") {

      exec(http("Respondent Submit Access Code")
        .post(BaseURL + "/applicant2/enter-your-access-code")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("caseReference", "${caseId}")
        .formParam("accessCode", "${accessCode}")
        .check(substring("Latest update"))
        //check that Response is the current step
        .check(regex(""" aria-current="step">(?s).*?<span class="hmcts-progress-bar__icon"></span>(?s).*?Response(?s).*?</li>""")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_030_RespContinueApplication") {

      exec(http("Respondent Continue Application")
        .get(BaseURL + "/respondent/review-the-application")
        .headers(CommonHeader)
        .header("sec-fetch-site", "none")
        .check(CsrfCheck.save)
        .check(substring("Review the divorce application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_040_RespReviewApplication") {

      exec(http("Respondent Review Application")
        .post(BaseURL + "/respondent/review-the-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("confirmReadPetition", List("", Case.Checkbox.Checked))
        .check(CsrfCheck.save)
        .check(substring("How do you want to respond")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_050_RespHowDoYouWantToRespond") {

      exec(http("Respondent How Do You Want To Respond")
        .post(BaseURL + "/respondent/how-do-you-want-to-respond")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("disputeApplication", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("The legal power")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_060_RespJurisdiction") {

      exec(http("Respondent Jurisdiction")
        .post(BaseURL + "/respondent/legal-jurisdiction-of-the-courts")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("jurisdictionAgree", Case.YesOrNo.Yes)
        .formParam("reasonCourtsOfEnglandAndWalesHaveNoJurisdiction", "")
        .formParam("inWhichCountryIsYourLifeMainlyBased", "")
        .check(CsrfCheck.save)
        .check(substring("Other court cases")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_070_RespOtherCourtCases") {

      exec(http("Respondent Other Court Cases")
        .post(BaseURL + "/respondent/other-court-cases")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2LegalProceedings", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("How the court will contact you")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_080_RespHowCourtWillContactYou") {
      exec(http("How the court will contact you")
        .post(BaseURL + "/respondent/how-the-court-will-contact-you")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("applicant2AgreeToReceiveEmails", List("", Case.Checkbox.Checked))
        .formParam("applicant2PhoneNumber", "")
        .check(CsrfCheck.save)
        .check(substring("What language do you want to receive emails and documents in?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_090_RespLanguageToReceiveDocs") {
      exec(http("English or Welsh?")
        .post(BaseURL + "/respondent/english-or-welsh")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2EnglishOrWelsh", "english")
        .check(CsrfCheck.save)
        .check(substring("Check your answers")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD04CitResp_100_RespCheckYourAnswers") {
      exec(http("Check your answers")
        .post(BaseURL + "/respondent/check-your-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("applicant2IBelieveApplicationIsTrue", List("", Case.Checkbox.Checked))
        .check(substring("Latest update"))
        .check(substring("You have responded"))
        //check for two completed sections
        .check(substring("progress-bar__icon--complete").count.is(2)))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
