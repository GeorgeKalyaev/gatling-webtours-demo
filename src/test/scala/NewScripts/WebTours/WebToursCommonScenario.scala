package NewScripts.WebTours

import io.gatling.core.Predef.{doIf, doWhile, exec, _}
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.{Cookie, addCookie}

import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.Random

object WebTours {
  def apply(): ScenarioBuilder = new WebTours().scn
}

class WebTours {
  val random = new Random()
  //  var shipments: Option[Vector[String]] = None
  var changeAddres = false

  val scn: ScenarioBuilder = scenario("BuyWithoutPromocodeAppLight")

    .feed(WebToursFeeder.Users)
    //    .feed(NewScripts.FeederGlobe.accountsB2cMobCherkessk)


    //      Группа самая основная которая будет включать в себя множество других груп
    .group("_UC01_CM_WebTours")(

      //Открытие главной страницы сайта
      group("UC01_S01_Open_MainPage")(

        exec(WebToursAction.webtours)
          .exec(WebToursAction.signOff_true)
          .exec { session =>
            session.set("ifFailed", session.isFailed.toString)
            // markAsSucceeded Используется для установки состояния сессии как успешная.
            // Это означает, что даже если предыдущие запросы завершились с ошибкой,
            // текущая сессия будет обозначена как успешная.
          }
          .exec { session => session.markAsSucceeded }

          //          exitHereIfFailed в основном проставляется у запроса где необходимо захватить какие-то данные например через регулярку
          .exec(WebToursAction.home).exitHereIfFailed

          .doIf(session => session("ifFailed").as[Boolean]) {
            //            Внутри группы .doIf(session => session("ifFailed").as[Boolean]) { ... }
            //            проверяется значение сессионной переменной "ifFailed".
            //              Если значение равно true (то есть предыдущие запросы завершились с ошибкой),
            //            то выполняется содержимое блока { ... }

            //    При условии, что предыдущие запросы завершились с ошибкой (session.isFailed = true), выполняются указанные действия внутри группы
            exec { session =>
              changeAddres = true
              session
            }
          }

      )
        //  Логинимся на сайте
        .group("UC01_S02_Login")(
          //          exec(addCookie(Cookie("userSession", "#{userSession}")))
          exec(WebToursAction.login)
            .exec(WebToursAction.menu_home)
            .exec(WebToursAction.intro)

        )

        // Переходим в Билеты илиже Полеты
        .group("UC01_S03_Go_Flight")(
          exec(WebToursAction.page_search)
            .exec(WebToursAction.menu_flights)
            .exec(WebToursAction.page_welcome)

            .exec(session => {
              val selectedCity = Seq.empty[String]
              session.set("selectedCity", selectedCity)
            })

            // Пример того как можно из фидера взять любое колличество значений и они все будут уникальные
            .doWhile(session => session("selectedCity").as[Seq[String]].length < NewScripts.VariablesOfCycles.CityCount) {
              feed(WebToursFeeder.City)
                .exec(session => {
                  val value = session("City").as[String]

                  val selectedCity = session("selectedCity").as[Seq[String]]
                  val updatedselectedCity = if (!selectedCity.contains(value)) {
                    selectedCity :+ value
                  } else {
                    selectedCity
                  }
                  session.set("selectedCity", updatedselectedCity)
                })
            }
            // Сохраняем города в разные сессионные переменные
            .exec(session => {
              val selectedCity = session("selectedCity").as[Seq[String]]
              val selectedCityDepart = selectedCity.headOption.getOrElse("")
              val selectedCityArrive = selectedCity.drop(1).headOption.getOrElse("")
              session.set("selectedCityDepart", selectedCityDepart)
                .set("selectedCityArrive", selectedCityArrive)
            })
            .exec(session => {
              println("selectedCityDepart: " + session("selectedCityDepart").as[String])
              println("selectedCityArrive: " + session("selectedCityArrive").as[String])
              session
            })

          //          Напирмер бывает что иногда нужно что то положить в одно место несколько раз например товары в корзину.
          //          Если представить что у нас не города, а товары, то можно было бы использовать следующий код:
          //            doWhile (session => session("counter").as[Int] < session("selectedCity").as[Seq[String]].length, "counter") {
          //            exec { session =>
          //              val counter = session("counter").as[Int]
          //              val selectedCity = session("selectedCity").as[Seq[String]]
          //              val value = selectedCity(counter)
          //              var newSession = session.set("products_id", value)
          //              newSession
          //            }
          //              .exec(BuyWithoutPromocodeAppLightAction.line_items1)
          //          }
          // Здесь через цикл подставлялись бы значении по одному в сессионную переменную products_id и затем она бы подставлялась
          // например в мтоде post в блоке line_items1 в WebToursAction через такую констукцию #{products_id} и так мы бы могли подставить
          // через цикл все значения которые были бы в массиве
        )

        //  Заполняем данные куда летим и откуда
        .group("UC01_S04_Fill_Data")(
          //          Отдельный пример того как можем получить unixTimestamp
          exec { session =>
            val unixTimestamp: Long = System.currentTimeMillis()
            println("unixTimestamp", unixTimestamp)
            session.set("unixTimestamp", unixTimestamp)
          }

            //            Отдельный пример того как можем получить даты по заданному формату 03/23/2024
            .exec { session =>
              val t = LocalDateTime.now
              val f1 = DateTimeFormatter.ofPattern("MM/dd/yyyy")

              val plusOneDate = f1.format(t.plusDays(1)) // Увеличение текущей даты на 1 день
              val plusTwoDate = f1.format(t.plusDays(2)) // Увеличение текущей даты на 2 дня

              println("plusOneDate", plusOneDate)
              println("plusTwoDate", plusTwoDate)

              session.set("plusOneDate", plusOneDate)
                .set("plusTwoDate", plusTwoDate)
            }

            .exec(WebToursAction.reservations)
        )

        //  Выбираем самолет
        .group("UC01_S05_ChosePlane")(
          exec(WebToursAction.reservationsChosePlane)

            //            Пример как генерируем UUID и убираем у него "-"
            .exec { session =>
              val UUID_RND = UUID.randomUUID().toString.replaceAll("-", "").substring(0, 21)
              println("UUID_RND", UUID_RND)
              session.set("UUID_RND", UUID_RND)
            }


        )


        //      Заполняем Payment_Details
        .group("UC01_S06_Payment_Details")(
          exec(WebToursAction.reservationsPaymentDetails)
//            Пример как сделать urlEncoded
            .exec(session => {
              val dataList = List("https://www.google.com/search?q=geeks for geeks", "geeks for geeks")
              val randomData = random.nextInt(dataList.size)
              val urlEncoded = URLEncoder.encode(dataList(randomData), "UTF-8")

              println("dataList_put", dataList(randomData))
              println("dataList_urlEncoded_put", urlEncoded)

              session
                .set("dataList_put", dataList(randomData))
                .set("dataList_urlEncoded_put", urlEncoded)

            })


            // Массив словами itinerary(прошлые заказы) и search(выбор или же оформление нового рейса)
            .exec(session => {
              val names = Array("itinerary", "search")
              val randomIndex = Random.nextInt(names.length)
              val randomName = names(randomIndex)
              session.set("randomNameProduct", randomName)
            })
            .exec(WebToursAction.choose)


            //            Пример то как можно сгенерировать Json разной величины и рандомные значения в нем и подставлять это все в body в запросе
            .exec(session => {
              //              val retailerSkus = session("retailer_sku_proc").as[Seq[String]]
              //              val prices = session("price_proc").as[Seq[String]]
              //              val discounts = session("discount_proc").as[Seq[String]]

              val retailerSkus = Array("55555", "44444", "33333", "22222", "88888", "77777").toSeq
              val prices = Array("100", "200", "400", "500", "300", "900").toSeq
              val discounts = Array("1", "3", "7", "4", "8", "6").toSeq

              val numBlocks = Random.nextInt(5) + 1
              val uniqueIndexes = Random.shuffle(retailerSkus.indices.toList).distinct

              val blocks = uniqueIndexes.take(numBlocks).map(index => {
                val id = retailerSkus(index)
                //              val quantity = Random.nextInt(10) + 1
                val price = prices(index)
                val discount = discounts(index)
                //              val promoTotal = Random.nextInt(100)
                s"""{"id":"$id","quantity":1,"price":$price,"discount":$discount,"promo_total":0}"""
              }).mkString(",")

              val randomLon = Random.nextInt(999) + 100
              val randomLat = Random.nextInt(999) + 100

              val body =
                s"""{"retailer_id":"610","location":{"lon":92.556$randomLon,"lat":67.14$randomLat},"items":[$blocks]}"""

              session.set("body", body)

            })
            .exec(WebToursAction.setBody)
        )

    )
}

