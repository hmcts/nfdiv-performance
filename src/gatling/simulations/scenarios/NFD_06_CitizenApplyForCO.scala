package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_06_CitizenApplyForCO {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val ApplyForConditionalOrder =

    group("NFD06CitResp_010_StartConditionalOrder") {

      exec(http("Start Conditional Order")
        .post(BaseURL + "/hub-page")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1ApplyForConditionalOrderStarted", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Read your wife&#39;s response")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD06CitResp_020_ReadTheResponse") {

      exec(http("Read the response")
        .post(BaseURL + "/read-the-response")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Do you want to continue with your divorce")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD06CitResp_030_ContinueWithYourApplication") {

      exec(http("Continue with your application")
        .post(BaseURL + "/continue-with-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1ApplyForConditionalOrder", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Review your divorce application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD06CitResp_040_ReviewYourApplication") {

      exec(http("Review your application")
        .post(BaseURL + "/review-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1ConfirmInformationStillCorrect", Case.YesOrNo.Yes)
        .formParam("applicant1ReasonInformationNotCorrect", "")
        .check(CsrfCheck.save)
        .check(substring("Check your answers")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD06CitResp_050_CheckYourCOAnswers") {

      exec(http("Check your CO answers")
        .post(BaseURL + "/check-your-conditional-order-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("coApplicant1StatementOfTruth", List("", Case.Checkbox.Checked))
        .check(CsrfCheck.save)
        .check(substring("Latest update"))
        .check(substring("You have applied for a ‘conditional order’"))
        //check for four completed sections
        .check(substring("progress-bar__icon--complete").count.is(4)))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
