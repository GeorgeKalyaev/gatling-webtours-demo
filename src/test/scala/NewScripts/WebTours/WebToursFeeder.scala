package NewScripts.WebTours

import io.gatling.core.Predef._

object WebToursFeeder {

  val Users = csv("Users.csv").circular
  val City = csv("City.csv").random

  //  val ProductsMetro = csv("ProductsMetro.csv").random
  //  val Promokod = csv("Promocodes.csv").circular
  //  val poolAdres = csv("poolAdres.csv").circular

  //  val device_id = Iterator.continually(Map("device_id" -> UUID.randomUUID().toString.replaceAll("-", "").substring(0, 16))) //e42c086bd89a948b
  //  //	println("device_id: " + device_id.next())
  //
  //  val installation_id = Iterator.continually(Map("installation_id" -> UUID.randomUUID().toString.toLowerCase)) //1aa100fc-5ff2-4c4a-8d15-92c8bff04821
  //  //  println("installation_id: " + installation_id.next())
  //
  //  val req_id = Iterator.continually(Map("req_id" -> UUID.randomUUID().toString.toLowerCase)) //c09b7a36-ed34-4359-892a-738557931f03
  //  //	println("req_id: " + req_id.next())
  //
  //  val appsflyer_id = Iterator.continually(Map("appsflyer_id" -> UUID.randomUUID().toString.toLowerCase))
  //  //	println("appsflyer_id: " + appsflyer_id.next())
}
