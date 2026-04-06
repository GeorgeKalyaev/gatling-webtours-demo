package NewScripts

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

package object Protocols {


  val httpProtocolWebTours: HttpProtocolBuilder = http
    .disableCaching
    .baseUrl("http://127.0.0.1:1080")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0")
    .check(status.in(200, 204))

  val httpProtocolWeb: HttpProtocolBuilder = http
    .disableCaching
    //.hostNameAliases(Map("sbermarket.ru" -> List("130.193.56.45")))
    .baseUrl("https://sbermarket.ru")
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("ru,en;q=0.9")
    .header("referer", "https://sbermarket.ru") // Для запуска с локал хостами
    .check(status.in(200, 204))
    .header("ANONYMOUSID", "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")
    .header("sbm-forward-testing-entity", "performance")
    .userAgentHeader("PerfTest/1.0.0 (IBS;Gatling)")


}

object FlagLocalhost {
  val flag = false
}

object FeederGlobe {
  val accountsB2cMobCherkessk = csv("AccountsFullB2cMOBCherkessk.csv").circular
  val accountsB2cWebCherkessk = csv("AccountsFullB2cWebCherkessk.csv").circular
  val accountsFullB2cMobWeb = csv("AccountsFullB2cMobWeb.csv").circular
  val WebAccounts_B2B = csv("WebAccountsFull_B2B.csv").circular
  val AppAccounts_B2B = csv("AppAccountsFull_B2B.csv").circular
  val accountsShopperB2C = csv("AccountsShopperB2C.csv").circular
  val poolAdres = csv("ShopperPoolAdres.csv").circular
}
