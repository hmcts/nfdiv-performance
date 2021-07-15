package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_01_CitizenCommon {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val postcodeFeeder = csv("postcodes.csv").random

  val InitialQuestions =

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


  val Jurisdictions =

    group("DivorceApp_060_HelpWithYourFeeSubmit") {
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
        .check(substring("You can use English or Welsh courts to get a divorce")))
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


  val MarriageNames =

    group("DivorceApp_130_NamesOnYourMarriageCertificate?") {
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

  val ContactDetails =

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
        .formParam("englishOrWelsh", "english")
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

  val DivorceDetailsAndUpload =

    group("DivorceApp_240_OtherCourtCases") {
      exec(http("Other court cases")
        .post(BaseURL + "/other-court-cases")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("legalProceedingsRelated", List("", "", ""))
        .formParam("legalProceedings", Case.YesOrNo.No)
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
        .formParam("uploadedFiles", "[{\"id\":\"" + "${documentId}" + "\",\"name\":\"2MB.pdf\"}]")
        .formParam("cannotUploadDocuments", "")
        .check(substring("Check your answers")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
