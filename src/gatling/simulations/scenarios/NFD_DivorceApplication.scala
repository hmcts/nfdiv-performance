package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_DivorceApplication {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val postcodeFeeder = csv("postcodes.csv").random

  val active =
    exec(http("DivorceApp_000_Active")
    .get(BaseURL + "/active")
    .headers(CommonHeader))

  val DivorceApplication =
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_150_HowTheCourtWillContactYou?") {
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
    .exec(active)
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_160_LanguageToReceiveEmailsAndDocumentsIn?") {
      exec(http("English or Welsh?")
        .post(BaseURL + "/english-or-welsh")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("englishOrWelsh", "english")
        .check(CsrfCheck.save)
        .check(substring("Do you need your contact details kept private from your wife?")))
    }
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_240_OtherCourtCases") {
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
    .exec(active)
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
    .exec(active)
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
    .exec(active)
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_270_DocumentUpload") {
      exec(http("Upload your documents")
        .post(BaseURL + "/document-manager?_csrf=${csrf}")
        .headers(CommonHeader)
        .headers(PostHeader)
        .bodyPart(RawFileBodyPart("files[]", "2MB.pdf")
          .fileName("2MB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .check(status.is(200)))
    }
    .exec(active)
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_280_DocumentUploadSubmit") {
      exec(http("Upload your documents")
        .post(BaseURL + "/upload-your-documents")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("uploadedFiles", "[{\"id\":\"1234\",\"name\":\"2MB.pdf\"}]")
        .formParam("cannotUploadDocuments", "")
        .check(substring("Check your answers")))
    }
    .exec(active)
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
    .exec(active)
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_300_PayYourFee") {
      exec(http("Pay your fee")
        .post(BaseURL + "/pay-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Check your answers")))
    }
    .exec(active)
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
