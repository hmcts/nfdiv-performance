package scenarios

import io.gatling.core.Predef._

object NFD_08_CaseworkerMakeEligibleForFO {

  val MakeEligibleForFinalOrder =

    exec(CCDAPI.CreateEvent("Caseworker", "system-link-with-bulk-case", "bodies/events/LinkWithBulkCase.json"))

    //set case hearing and decision dates to a date in the past
    .exec(CCDAPI.CreateEvent("Caseworker", "system-update-case-court-hearing", "bodies/events/UpdateCaseWithCourtHearing.json"))

    //set judge details, CO granted and issued dates in the past
    .exec(CCDAPI.CreateEvent("Caseworker", "caseworker-amend-case", "bodies/events/AmendCase.json"))

    .exec(CCDAPI.CreateEvent("Caseworker", "system-pronounce-case", "bodies/events/PronounceCase.json"))

    .exec(CCDAPI.CreateEvent("Caseworker", "system-progress-case-awaiting-final-order", "bodies/events/AwaitingFinalOrder.json"))

}
