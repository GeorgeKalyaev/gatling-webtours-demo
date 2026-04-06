package NewScripts.WebTours

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object WebToursAction {


  //Открытие главной страницы сайта

  val webtours: HttpRequestBuilder = http("/webtours/")
    .get("/webtours/")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")

  val signOff_true: HttpRequestBuilder = http("/cgi-bin/welcome.pl?signOff=true")
    .get("/cgi-bin/welcome.pl?signOff=true")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")

  val home: HttpRequestBuilder = http("/cgi-bin/nav.pl?in=home")
    .get("/cgi-bin/nav.pl?in=home")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .check(regex(""""userSession" value="(.*?)"""").saveAs("userSession"))

  //  Логинимся на сайте

  val login: HttpRequestBuilder = http("/cgi-bin/login.pl")
    .post("/cgi-bin/login.pl")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .formParam("userSession", "#{userSession}")
    .formParam("username", "#{name}")
    .formParam("password", "#{pass}")

  val menu_home: HttpRequestBuilder = http("/cgi-bin/nav.pl?page=menu&in=home")
    .get("/cgi-bin/nav.pl?page=menu&in=home")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")

  val intro: HttpRequestBuilder = http("/cgi-bin/login.pl?intro=true")
    .get("/cgi-bin/login.pl?intro=true")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .check(substring("<b>#{name}</b>").exists)
    .check(status.in(302, 200))

  //  Нажимаем полеты

  val page_search: HttpRequestBuilder = http("/cgi-bin/welcome.pl?page=search")
    .get("/cgi-bin/welcome.pl?page=search")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")

  val menu_flights: HttpRequestBuilder = http("/cgi-bin/nav.pl?page=menu&in=flights")
    .get("/cgi-bin/nav.pl?page=menu&in=flights")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")

  val page_welcome: HttpRequestBuilder = http("/cgi-bin/reservations.pl?page=welcome")
    .get("/cgi-bin/reservations.pl?page=welcome")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .check(regex("""option value="(.*?)"""").findAll.saveAs("CityFromResponse"))


  //  Заполняем данные куда летим и откуда

  val reservations: HttpRequestBuilder = http("/cgi-bin/reservations.pl")
    .post("/cgi-bin/reservations.pl")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .formParam("advanceDiscount", "0")
    .formParam("depart", "#{selectedCityDepart}")
    .formParam("departDate", "#{plusOneDate}")
    .formParam("arrive", "#{selectedCityArrive}")
    .formParam("returnDate", "#{plusTwoDate}")
    .formParam("numPassengers", "1")
    .formParam("seatPref", "Window")
    .formParam("seatType", "Business")
    .formParam("findFlights.x", "56")
    .formParam("findFlights.y", "10")
    .formParam(".cgifields", "roundtrip")
    .formParam(".cgifields", "seatType")
    .formParam(".cgifields", "seatPref")

  //  Выбираем самолет

  val reservationsChosePlane: HttpRequestBuilder = http("/cgi-bin/reservations.pl")
    .post("/cgi-bin/reservations.pl")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .formParam("outboundFlight", "282;1975;#{plusOneDate}")
    .formParam("numPassengers", "1")
    .formParam("advanceDiscount", "0")
    .formParam("seatType", "Business")
    .formParam("seatPref", "Window")
    .formParam("reserveFlights.x", "39")
    .formParam("reserveFlights.y", "9")


  //      Заполняем Payment_Details

  val reservationsPaymentDetails: HttpRequestBuilder = http("/cgi-bin/reservations.pl")
    .post("/cgi-bin/reservations.pl")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .formParam("firstName", "Jojo")
    .formParam("lastName", "Bean")
    .formParam("address1", "pyshkin_street")
    .formParam("address2", "Moscow_city")
    .formParam("pass1", "Jojo Bean")
    .formParam("creditCard", "1234 4564 1236 2535")
    .formParam("expDate", "2028")
    .formParam("oldCCOption", "")
    .formParam("numPassengers", "1")
    .formParam("seatType", "Business")
    .formParam("seatPref", "Business")
    .formParam("outboundFlight", "282;1975;#{plusOneDate}")
    .formParam("advanceDiscount", "0")
    .formParam("returnFlight", "")
    .formParam("JSFormSubmit", "off")
    .formParam("buyFlights.x", "23")
    .formParam("buyFlights.y", "6")
    .formParam(".cgifields.y", "saveCC")


//  Дополнительный запрос. Чтобы показать как можно подставлять сюда значения.
//  Подставляется itinerary(прошлые заказы) или search(выбор или же оформление нового рейса)
val choose: HttpRequestBuilder = http("/cgi-bin/welcome.pl?page=search")
  .get("/cgi-bin/welcome.pl?")
  .header("Upgrade-Insecure-Requests", "1")
  .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
  .queryParam("page", (session => {
    val randomNameProduct = session("randomNameProduct").as[String]
    s"""$randomNameProduct"""
  }))

// Тестовый запрос для демострации как можно подставить в боди данные формата Json
  val setBody: HttpRequestBuilder = http("/cgi-bin/welcome.pl?page=search")
    .get("/cgi-bin/welcome.pl?page=search")
    .header("Upgrade-Insecure-Requests", "1")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .body(StringBody(session => {
      val body = session("body").as[String]
      s"""$body"""
    }))
}

