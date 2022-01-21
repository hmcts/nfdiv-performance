package scenarios

import io.gatling.core.Predef._

object NFD_03_CaseworkerIssueApplication {

  val IssueApplication =

    exec(CCDAPI.GetMarriageDetails)
    .exec(CCDAPI.CreateEvent("Caseworker", "caseworker-issue-application", "bodies/events/IssueApplication.json"))

}
