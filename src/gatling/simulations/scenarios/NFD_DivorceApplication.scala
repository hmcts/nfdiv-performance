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
    group("DivorceApp_000_Active") {
      exec(http("Active")
      .get(BaseURL + "/active")
      .headers(CommonHeader))
    }

  val DivorceApplication =
    group("DivorceApp_010_YourDetailsSubmit") {
      active
      .exec(http("Your Details Submit")
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
      active
      .exec(http("Marriage Broken Down Submit")
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
      active
      .exec(http("Date From Certificate Submit")
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
      active
      .exec(http("Has Marriage Certificate Submit")
        .post(BaseURL + "/do-you-have-your-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("hasCertificate", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("How do you want to apply for the divorce?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_045_HowDoYouWantToApply") {
      active
      .exec(http("How do you want to apply")
        .post(BaseURL + "/how-do-you-want-to-apply")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicationType", "soleApplication")
        .check(CsrfCheck.save)
        .check(substring("Do you need help paying the fee for your divorce?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_050_HelpWithYourFeeSubmit") {
      active
      .exec(http("Help With Your Fee Submit")
        .post(BaseURL + "/help-with-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("helpPayingNeeded", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Did you get married in the UK?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_060_InTheUKSubmit") {
      active
      .exec(http("In The UK Submit")
        .post(BaseURL + "/in-the-uk")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("inTheUk", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Check if you can get a divorce in England and Wales")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_070_CheckJurisdictionSubmit") {
      active
      .exec(http("Check Jurisdiction Submit")
        .post(BaseURL + "/check-jurisdiction")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Where your lives are based")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_080_WhereYourLivesAreBasedSubmit") {
      active
      .exec(http("Where Your Lives Are Based Submit")
        .post(BaseURL + "/where-your-lives-are-based")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("yourLifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .formParam("applicant2LifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(regex("""<input class="govuk-input" id="connections" name="connections" type="hidden" value="(.+)"""").saveAs("connectionId"))
        .check(substring("You can use English or Welsh courts to apply for a divorce")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_090_CanUseEnglishWelshCourt") {
      active
      .exec(http("English or Welsh courts")
        .post(BaseURL + "/you-can-use-english-welsh-courts")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("connections", "${connectionId}")
        .check(CsrfCheck.save)
        .check(substring("Enter your name")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_100_EnterYourName") {
      active
      .exec(http("Enter your name")
        .post(BaseURL + "/enter-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("yourFirstNames", "${forename}")
        .formParam("yourMiddleNames", "")
        .formParam("yourLastNames", "${surname}")
        .check(CsrfCheck.save)
        .check(substring("Enter your wife’s name")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_110_EnterTheirName") {
      active
      .exec(http("Enter Their name")
        .post(BaseURL + "/enter-their-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("theirFirstNames", Common.randomString(5))
        .formParam("theirMiddleNames", "")
        .formParam("theirLastNames", Common.randomString(5))
        .check(CsrfCheck.save)
        .check(substring("Your names on your marriage certificate")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_120_NamesOnYourMarriageCertificate?") {
      active
      .exec(http("Names on your marriage certificate")
        .post(BaseURL + "/your-names-on-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("fullNameOnCertificate", "${forename}" + " " + "${surname}")
        .formParam("applicant2FullNameOnCertificate", Common.randomString(5) + " " + Common.randomString(5))
        .check(CsrfCheck.save)
        .check(substring("Changes to your name")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_130_ChangesToYourName?") {
      active
      .exec(http("Changes to your name")
        .post(BaseURL + "/changes-to-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("lastNameChangeWhenRelationshipFormed", Case.YesOrNo.No)
        .formParam("anyNameChangeSinceRelationshipFormed", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("How the court will contact you")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_140_HowTheCourtWillContactYou?") {
      active
      .exec(http("How the court will contact you")
        .post(BaseURL + "/how-the-court-will-contact-you")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("agreeToReceiveEmails", List("", Case.Checkbox.Checked))
        .formParam("phoneNumber", "")
        .check(CsrfCheck.save)
        .check(substring("What language do you want to receive emails and documents in?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_150_LanguageToReceiveEmailsAndDocumentsIn?") {
      active
      .exec(http("English or Welsh?")
        .post(BaseURL + "/english-or-welsh")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("englishOrWelsh", "english")
        .check(CsrfCheck.save)
        .check(substring("Do you need your contact details kept private from your wife?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_160_DetailsKeptPrivate?") {
      active
      .exec(http("Keep contact details private from your wife?")
        .post(BaseURL + "/address-private")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("addressPrivate", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Enter your postal address")))
    }
      .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_165_EnterYourPostCode") {
      active
      .feed(postcodeFeeder)
        .exec(http("Enter your postal address")
          .post(BaseURL + "/postcode-lookup")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("postcode", "${postcode}")
          .check(regex(""""fullAddress":"(?:.+?)","street1":"(.*?)","street2":"(.*?)","town":"(.*?)","county":"(.*?)","postcode":"(.+?)"""").ofType[(String, String, String, String, String)].findRandom.saveAs("addressLines")))
    }
      .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_170_EnterYourPostalAddress") {
      active
      .exec(http("Enter your postal address")
        .post(BaseURL + "/enter-your-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("yourAddress1", "${addressLines(0)}")
        .formParam("yourAddress2", "${addressLines(1)}")
        .formParam("yourAddress3", "")
        .formParam("yourAddressTown", "${addressLines(2)}")
        .formParam("yourAddressCounty", "${addressLines(3)}")
        .formParam("yourAddressPostcode", "${addressLines(4)}")
        .formParam("yourAddressCountry", "UK")
        .check(CsrfCheck.save)
        .check(regex("Enter your wife(&.+;)s email address")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_180_EnterTheirEmailAddress") {
      active
      .exec(http("Enter your wife's email address")
        .post(BaseURL + "/their-email-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2EmailAddress", "test@test.com")
        .formParam("doNotKnowApplicant2EmailAddress", "")
        .check(CsrfCheck.save)
        .check(regex("Do you have your wife(&.+;)s postal address?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_190_DoYouKnowTheirAddress") {
      active
      .exec(http("Do you have your wife's postal address?")
        .post(BaseURL + "/do-you-have-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("knowApplicant2Address", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Enter your wife’s postal address")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_200_TheirAddress") {
      active
      .exec(http("Enter your wife’s postal address")
        .post(BaseURL + "/enter-their-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("isTheirAddressInternational", Case.YesOrNo.No)
        .formParam("theirAddress1", "2 Address, Road")
        .formParam("theirAddress2", "")
        .formParam("theirAddress3", "")
        .formParam("theirAddressTown", "Town")
        .formParam("theirAddressCounty", "County")
        .formParam("theirAddressPostcode", "E1 1AB")
        .formParam("theirAddressCountry", "UK")
        .check(CsrfCheck.save)
        .check(substring("Other court cases")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_210_OtherCourtCases") {
      active
      .exec(http("Other court cases")
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

    .group("DivorceApp_220_DividingYourMoneyAndProperty") {
      active
      .exec(http("Dividing your money and property")
        .post(BaseURL + "/dividing-money-property")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Do you want to apply for a financial order?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_230_ApplyForFinancialOrder") {
      active
      .exec(http("Do you want to apply for a financial order?")
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

    .group("DivorceApp_240_DocumentUpload") {
      active
      .exec(http("Upload your documents")
        .post(BaseURL + "/document-manager?_csrf=${csrf}")
        .headers(CommonHeader)
        .headers(PostHeader)
        .bodyPart(RawFileBodyPart("files[]", "2MB.pdf")
          .fileName("2MB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .check(status.is(200)))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_250_DocumentUploadSubmit") {
      active
      .exec(http("Upload your documents")
        .post(BaseURL + "/upload-your-documents")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("uploadedFiles", "[{\"id\":\"1234\",\"name\":\"2MB.pdf\"}]")
        .formParam("cannotUploadDocuments", "")
        .check(substring("Check your answers")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_260_CheckYourAnswers") {
      active
      .exec(http("Check your answers")
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

    .group("DivorceApp_270_PayYourFee") {
      active
      .exec(http("Pay your fee")
        .post(BaseURL + "/pay-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Check your answers")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
