package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_04_CitizenApplyForFO {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val ApplyForFinalOrder =

    group("NFD04CitFO_010_#{userType}StartFinalOrder") {

      exec(http("Start Final Order")
        .get(BaseURL + "/#{userTypeURL}finalising-your-application")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("Do you want to finalise your divorce")))

    }

    .pause(MinThinkTime.seconds, MaxThinkTime.seconds)

    .group("NFD04CitFO_020_#{userType}ConfirmFinalOrder") {

      exec(http("Confirm Conditional Order")
        .post(BaseURL + "/#{userTypeURL}finalising-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "#{csrf}")
        .multivaluedFormParam(session => "does" + session("userType").as[String].replace("applicant", "Applicant") + "WantToApplyForFinalOrder", List("", Case.Checkbox.Checked))
        .check(regex("You have applied for a ‘final order’|both confirmed you want to finalise the divorce")))

    }

    .pause(MinThinkTime.seconds, MaxThinkTime.seconds)


}
