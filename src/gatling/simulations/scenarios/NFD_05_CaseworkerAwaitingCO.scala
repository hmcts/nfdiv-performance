package scenarios

import io.gatling.core.Predef._

object NFD_05_CaseworkerAwaitingCO {

  val AwaitingConditionalOrder =

    exec(CCDAPI.CreateEvent("Caseworker", "system-progress-held-case", "bodies/events/AwaitingConditionalOrder.json"))

}
