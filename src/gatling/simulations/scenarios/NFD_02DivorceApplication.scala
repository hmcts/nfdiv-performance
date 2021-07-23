package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_02DivorceApplication {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val postcodeFeeder = csv("postcodes.csv").random

  val ApplicationQuestions2 =
    group("DivorceApp_150_HowTheCourtWillContactYou?") {
      exec(http("How the court will contact you")
        .post(BaseURL + "/how-the-court-will-contact-you")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("applicant1AgreeToReceiveEmails", List("", Case.Checkbox.Checked))
        .formParam("applicant1PhoneNumber", "")
        .check(CsrfCheck.save)
        .check(substring("What language do you want to receive emails and documents in?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_160_LanguageToReceiveEmailsAndDocs?") {
      exec(http("English or Welsh?")
        .post(BaseURL + "/english-or-welsh")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1EnglishOrWelsh", "english")
        .check(CsrfCheck.save)
        .check(substring("Do you need your contact details kept private from your wife?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_170_DetailsKeptPrivate?") {
      exec(http("Keep contact details private from your wife?")
        .post(BaseURL + "/address-private")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1AddressPrivate", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Enter your postal address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_180_EnterYourPostcode") {
      feed(postcodeFeeder)
        .exec(http("Enter your postcode")
          .post(BaseURL + "/postcode-lookup")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("postcode", "${postcode}")
          .check(regex(""""fullAddress":"(?:.+?)","street1":"(.*?)","street2":"(.*?)","town":"(.*?)","county":"*(.*?)"*,"postcode":"(.+?)"""")
            .ofType[(String, String, String, String, String)].findRandom.saveAs("addressLines")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_190_EnterYourPostalAddress") {
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

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_240_OtherCourtCases") {
      exec(http("Other court cases")
        .post(BaseURL + "/other-court-cases")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("applicant1LegalProceedingsRelated", List("", "", ""))
        .formParam("applicant1LegalProceedings", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Dividing your money and property")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_250_DividingYourMoneyAndProperty") {
      exec(http("Dividing your money and property")
        .post(BaseURL + "/dividing-money-property")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Do you want to apply for a financial order?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_260_ApplyForFinancialOrder") {
      exec(http("Do you want to apply for a financial order?")
        .post(BaseURL + "/do-you-want-to-apply-financial-order")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("whoIsFinancialOrderFor", List("", ""))
        .formParam("applyForFinancialOrder", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Upload your documents")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_270_DocumentUpload") {
      exec(http("Upload your documents")
        .post(BaseURL + "/document-manager?_csrf=${csrf}")
        .header("accept", "application/json")
        .header("accept-encoding", "gzip, deflate, br")
        .header("accept-language", "en-GB,en;q=0.9")
        .header("content-type", "multipart/form-data")
        .header("sec-fetch-dest", "empty")
        .header("sec-fetch-mode", "cors")
        .header("sec-fetch-site", "same-origin")
        .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36")
        .header("x-requested-with", "XMLHttpRequest")
        .bodyPart(RawFileBodyPart("files[]", "2MB.pdf")
          .fileName("2MB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .check(status.is(200))
        .check(regex(""""id":"(.+)","name":"2MB.pdf"""").saveAs("documentId")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_280_DocumentUploadSubmit") {
      exec(http("Upload your documents")
        .post(BaseURL + "/upload-your-documents")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1UploadedFiles", "[{\"id\":\"" + "${documentId}" + "\",\"name\":\"2MB.pdf\"}]")
        .formParam("applicant1CannotUploadDocuments", "")
        .check(substring("Check your answers")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_290_CheckYourAnswers") {
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

    .group("DivorceApp_300_PayYourFee") {
      exec(http("Pay your fee")
        .post(BaseURL + "/pay-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Enter card details")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
