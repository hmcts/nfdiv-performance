package simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scenarios._
import utils.Environment

class NFD_Pipeline extends Simulation {

  val BaseURL = Environment.baseURL

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    .inferHtmlResources()
    .silentResources

  val NFDSimulation = scenario( "NFDSimulation")
    .exec(NFD_GetCall.NFD_API_GET_CALL)
    .exec(NFD_PostCall.NFD_API_POST_CALL)
    .exec(NFD_DivorceApplication.DivorceApplication)

  setUp(
    NFDSimulation.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))

}
