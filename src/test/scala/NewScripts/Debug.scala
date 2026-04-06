package NewScripts

import NewScripts.Protocols.httpProtocolWebTours
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object VariablesOfCycles {

  //WebTours
  val CityCount = 2 // колличество захватываемых городов

}

class Debug extends Simulation {

  val port = 8882

  //Длительность разгона в секундах
  val rumpUpNext = 3600
  //Длительность ступени в секундах
  val constantNext = 1

  //Процент интенсивности (0,01 это 1%; 1 это 100%)
  val rate1 = 0.01
  val rate2 = 14

  setUp(

    WebTours.WebTours().inject(atOnceUsers(1)).protocols(httpProtocolWebTours.proxy(Proxy("127.0.0.1", port).httpsPort(port)))

  )
}
