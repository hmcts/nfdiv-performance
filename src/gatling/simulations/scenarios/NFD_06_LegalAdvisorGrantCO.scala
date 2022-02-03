package scenarios

import io.gatling.core.Predef._

object NFD_06_LegalAdvisorGrantCO {

  val GrantConditionalOrder =

    exec(CCDAPI.CreateEvent("Legal", "legal-advisor-make-decision", "bodies/events/MakeDecision.json"))

}
