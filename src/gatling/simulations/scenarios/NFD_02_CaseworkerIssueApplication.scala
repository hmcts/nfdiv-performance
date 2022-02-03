package scenarios

import io.gatling.core.Predef._

object NFD_02_CaseworkerIssueApplication {

  val IssueApplication =

    exec(CCDAPI.GetMarriageDetails)
    .exec(CCDAPI.CreateEvent("Caseworker", "caseworker-issue-application", "bodies/events/IssueApplication.json"))

}
