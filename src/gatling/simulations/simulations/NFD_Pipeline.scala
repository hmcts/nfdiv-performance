package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
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
    .exitBlockOnFail {
      exec(flushHttpCache)
      .exec(flushCookieJar)
      .exec(
        CreateUser.CreateCitizen,
        Homepage.NFDHomepage,
        Login.NFDLogin,
        NFD_DivorceApplication.DivorceApplication,
        NFD_DivorceApplication_Jurisdiction.Jurisdiction,
        Logout.NFDLogout)
    }
    .exec(DeleteUser.DeleteCitizen)

  setUp(
    NFDSimulation.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))

}
