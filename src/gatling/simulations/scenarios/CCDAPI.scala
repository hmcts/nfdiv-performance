package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import ccd._

object CCDAPI {

  val GetMarriageDetails = {

    CcdHelper.getCase(
      "#{cw-user}",
      "#{cw-password}",
      CcdCaseTypes.DIVORCE_NFD,
      "#{caseId}",
      additionalChecks = Seq(
        jsonPath("$.case_data.marriageDate").saveAs("marriageDate"),
        jsonPath("$.case_data.marriageApplicant1Name").saveAs("marriageApplicant1Name"),
        jsonPath("$.case_data.marriageApplicant2Name").saveAs("marriageApplicant2Name")
      ))
  }

  val GetAccessCode =

    CcdHelper.searchCasesAsCitizen(
      "#{emailAddress}",
      "#{password}",
      CcdCaseTypes.DIVORCE_NFD,
      additionalChecks = Seq(
        jsonPath("$[0].case_data.accessCode").saveAs("accessCode")
      ))

}
