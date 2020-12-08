package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import utils.Environment

object FactScenario {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val GetHeader = Environment.getHeader
  val PostHeader = Environment.postHeader

  val postcodeFeeder = csv("postcodes.csv").random
  val searchTermFeeder = csv("searchTerms.csv").random

  val FactNameKnown =

    group("Fact_010_Homepage") {
      exec(http("Load Homepage")
        .get(BaseURL + "/")
        .headers(CommonHeader)
        .headers(GetHeader)
        .check(regex("Use this service to find a court")))
    }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_020_Start") {
        exec(http("Load Start Page")
          .get(BaseURL + "/search-option")
          .headers(CommonHeader)
          .headers(GetHeader)
          .check(regex("Do you know the name")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_030_NameKnownSubmit") {
        exec(http("Name Known Submit")
          .post(BaseURL + "/search-option")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("knowLocation", "yes")
          .check(regex("What is the name or address")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_040_SeachByName") {
        feed(searchTermFeeder)
          .exec(http("Search By Name")
            .get(BaseURL + "/courts?search=${searchTerm}")
            .headers(CommonHeader)
            .headers(GetHeader)
            .check(regex("""govuk-heading-m">\n +?<a class="govuk-link" href="/courts/(.+?)">""").findRandom.saveAs("courtURL"))
            .check(regex("matching your search")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_080_LoadCourtDetailsPage") {
        exec(http("Load Court Page")
          .get(BaseURL + "/courts/${courtURL}")
          .headers(CommonHeader)
          .headers(GetHeader)
          .check(regex("Telephone|Make a complaint:")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)


  val FactNameNotKnown =

    group("Fact_010_Homepage") {
      exec(http("Load Homepage")
        .get(BaseURL + "/")
        .headers(CommonHeader)
        .headers(GetHeader)
        .check(regex("Use this service to find a court")))
    }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_020_Start") {
        exec(http("Load Start Page")
          .get(BaseURL + "/search-option")
          .headers(CommonHeader)
          .headers(GetHeader)
          .check(regex("Do you know the name")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_030_NameNotKnownSubmit") {
        exec(http("Name Not Known Submit")
          .post(BaseURL + "/search-option")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("knowLocation", "no")
          .check(regex("What do you want to do")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_040_NearestSubmit") {
        exec(http("Nearest Submit")
          .post(BaseURL + "/service-choose-action")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("chooseAction", "nearest")
          .check(regex("What do you want to know more about")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_050_NearestMoneySubmit") {
        exec(http("Nearest Money Submit")
          .post(BaseURL + "/services/nearest")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("chooseService", "money")
          .check(regex("What kind of help do you need with money")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_060_NearestMoneyClaimsSubmit") {
        exec(http("Nearest Money Claims Submit")
          .post(BaseURL + "/services/money/service-areas/nearest")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("serviceArea", "money-claims")
          .check(regex("What is your postcode")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_070_SearchByPostcode") {
        feed(postcodeFeeder)
          .exec(http("Search By Postcode")
            .get(BaseURL + "/services/money/money-claims/courts/near?postcode=${postcode}")
            .headers(CommonHeader)
            .headers(GetHeader)
            .check(regex("""govuk-heading-m">\n +?<a class="govuk-link" href="/courts/(.+?)">""").findRandom.saveAs("courtURL"))
            .check(regex("Court or tribunal search results")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("Fact_080_LoadCourtDetailsPage") {
        exec(http("Load Court Page")
          .get(BaseURL + "/courts/${courtURL}")
          .headers(CommonHeader)
          .headers(GetHeader)
          .check(regex("Telephone|Make a complaint:")))
      }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
