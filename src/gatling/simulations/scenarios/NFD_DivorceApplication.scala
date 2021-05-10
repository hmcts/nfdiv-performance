package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_DivorceApplication {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val DivorceApplication =
    group("DivorceApp_010_YourDetailsSubmit") {
      exec(http("Your Details Submit")
        .get(BaseURL + "/your-details")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("Who are you applying to divorce?")))
    }
      .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_020_YourDetailsSubmit") {
      exec(http("Your Details Submit")
        .post(BaseURL + "/your-details")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("gender", Case.Gender.Female)
        .formParam("sameSex", Case.Checkbox.Unchecked)
        .check(substring("Has your marriage irretrievably broken down (it cannot be saved)?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_030_MarriageBrokenDownSubmit") {
      exec(http("Marriage Broken Down Submit")
        .post(BaseURL + "/irretrievable-breakdown")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("screenHasUnionBroken", Case.YesOrNo.Yes)
        .check(substring("When did you get married?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_040_DateFromCertificateSubmit") {
      exec(http("Date From Certificate Submit")
        .post(BaseURL + "/date-from-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("relationshipDate-day", "1")
        .formParam("relationshipDate-month", "1")
        .formParam("relationshipDate-year", "2000")
        .check(substring("Do you have your marriage certificate with you?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_050_HasMarriageCertificateSubmit") {
      exec(http("Has Marriage Certificate Submit")
        .post(BaseURL + "/do-you-have-your-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("hasCertificate", Case.YesOrNo.Yes)
        .check(substring("Do you need help paying the fee for your divorce?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_060_HelpWithYourFeeSubmit") {
      exec(http("Help With Your Fee Submit")
        .post(BaseURL + "/help-with-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("helpPayingNeeded", Case.YesOrNo.Yes)
        .check(substring("Have you already applied for help with your divorce fee?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_070_AlreadyAppliedHelpWithYourFeeSubmit") {
      exec(http("Already Applied Help With Your Fee Submit")
        .post(BaseURL + "/have-you-applied-for-help-with-fees")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("alreadyAppliedForHelpPaying", Case.YesOrNo.Yes)
        .formParam("helpWithFeesRefNo", "HWF-A1B-23A")
        .check(substring("Did you get married in the UK?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_080_InTheUKSubmit") {
      exec(http("In The UK Submit")
        .post(BaseURL + "/in-the-uk")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("inTheUk", Case.YesOrNo.Yes)
        .check(substring("Check if you can get a divorce in England and Wales")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_090_CheckJurisdictionSubmit") {
      exec(http("Check Jurisdiction Submit")
        .post(BaseURL + "/check-jurisdiction")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Where your lives are based")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_100_WhereYourLivesAreBasedSubmit") {
      exec(http("Where Your Lives Are Based Submit")
        .post(BaseURL + "/where-your-lives-are-based")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("yourLifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .formParam("partnersLifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .check(substring("You can use English or Welsh courts to apply for a divorce")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_110_CanUseEnglishWelshCourt") {
      exec(http("You can use English or Welsh courts to apply for a divorce")
        .post(BaseURL + "/you-can-use-english-welsh-courts")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Enter your name")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_120_EnterYourName") {
      exec(http("Enter your name")
        .post(BaseURL + "/enter-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("yourFirstNames", "Testfirstname")
        .formParam("yourMiddleNames", "")
        .formParam("yourLastNames", "Testlastname")
        .check(substring("Enter your wife’s name")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_130_EnterTheirName") {
      exec(http("Enter Their name")
        .post(BaseURL + "/enter-their-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("theirFirstNames", "Testtheirfirstname")
        .formParam("theirMiddleNames", "")
        .formParam("theirLastNames", "Testtheirlastname")
        .check(substring("Your names on your marriage certificate")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_140_NamesOnYourMarriageCertificate?") {
      exec(http("Your names on your marriage certificate")
        .post(BaseURL + "/your-names-on-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("fullNameOnCertificate", "TestFirstName TestLastName")
        .formParam("partnersFullNameOnCertificate", "TestTheirFirstName TestTheirLastName")
        .check(substring("Changes to your name")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_150_ChangesToYourName?") {
      exec(http("Changes to your name")
        .post(BaseURL + "/changes-to-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("lastNameChangeWhenRelationshipFormed", Case.YesOrNo.No)
        .formParam("anyNameChangeSinceRelationshipFormed", Case.YesOrNo.No)
        .check(substring("How the court will contact you")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_160_HowTheCourtWillContactYou?") {
      exec(http("How the court will contact you")
        .post(BaseURL + "/how-the-court-will-contact-you")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("agreeToReceiveEmails", Case.Checkbox.Checked)
        .formParam("phoneNumber", "")
        .check(substring("What language do you want to receive emails and documents in?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_170_LanguageToReceiveEmailsAndDocumentsIn?") {
      exec(http("What language do you want to receive emails and documents in?")
        .post(BaseURL + "/english-or-welsh")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("englishOrWelsh", "english")
        .check(substring("Do you need your contact details kept private from your wife?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_180_DetailsKeptPrivate?") {
      exec(http("Do you need your contact details kept private from your wife?")
        .post(BaseURL + "/address-private")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("addressPrivate", Case.YesOrNo.No)
        .check(substring("Enter your postal address")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_190_EnterYourPostalAddress") {
      exec(http("Enter your postal address")
        .post(BaseURL + "/enter-your-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("isYourAddressInternational", Case.YesOrNo.No)
        .formParam("yourAddress1", "Address, Road")
        .formParam("yourAddress2", "")
        .formParam("yourAddressTown", "Town")
        .formParam("yourAddressCounty", "County")
        .formParam("yourAddressPostcode", "E1 1AA")
        .formParam("yourInternationalAddress", "")
        .check(regex("Enter your wife(&.+;)s email address")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_200_EnterTheirEmailAddress") {
      exec(http("Enter your wife's email address")
        .post(BaseURL + "/their-email-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("respondentEmailAddress", "test@test.com")
        .check(regex("Do you have your wife(&.+;)s postal address?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_210_DoYouKnowTheirAddress") {
      exec(http("Do you have your wife's postal address?")
        .post(BaseURL + "/do-you-have-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("knowPartnersAddress", Case.YesOrNo.Yes)
        .check(substring("Enter your wife’s postal address")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_220_TheirAddress") {
      exec(http("Enter your wife’s postal address")
        .post(BaseURL + "/enter-their-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("isTheirAddressInternational", Case.YesOrNo.No)
        .formParam("theirAddress1", "2 Address, Road")
        .formParam("theirAddressTown", "Town")
        .formParam("theirAddressCounty", "County")
        .formParam("theirAddressPostcode", "E1 1AB")
        .check(substring("Other court cases")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_230_OtherCourtCases") {
      exec(http("Other court cases")
        .post(BaseURL + "/other-court-cases")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("legalProceedings", Case.YesOrNo.No)
        .check(substring("Dividing your money and property")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_240_DividingYourMoneyAndProperty") {
      exec(http("Dividing your money and property")
        .post(BaseURL + "/dividing-money-property")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Do you want to apply for a financial order?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_250_ApplyForFinancialOrder") {
      exec(http("Do you want to apply for a financial order?")
        .post(BaseURL + "/do-you-want-to-apply-financial-order")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applyForFinancialOrder", Case.YesOrNo.No)
        .check(substring("Upload your documents")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_260_DocumentUpload") {
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
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("DivorceApp_270_DocumentUploadSubmit") {
      exec(http("Upload your documents")
        .post(BaseURL + "/upload-your-documents")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("uploadedFiles", "[{\"id\":\"1234\",\"name\":\"2MB.pdf\"}]")
        .check(substring("Check your answers")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
