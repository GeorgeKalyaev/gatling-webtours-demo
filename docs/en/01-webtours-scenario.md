# WebTours scenario (Gatling)

**[← Documentation home](../../README.md)** · **[Русская версия](../ru/01-webtours-scenario.md)**

A load-test style scenario for the **HP WebTours** demo app: HTTP steps, response checks, session handling, feeders, generated data, and a dynamic request body.

### Code layout

- `NewScripts.WebTours.WebToursAction` — request definitions and `check`s.
- `NewScripts.WebTours.WebTours` (scenario in `WebToursCommonScenario.scala`) — step groups, feeders, session data generation.
- `NewScripts.WebTours.WebToursFeeder` and CSVs under `src/test/resources` — virtual user and city data.
- `src/test/resources/logback-test.xml` — optional detailed HTTP response logging to a file.

See also: **[Gatling project structure](03-gatling-project-structure.md)** (folders `resources` / `scala`, `Action` / `Scenario` / `Feeder` pattern).

---

### Response checks (`check`)

#### Correlation: `userSession` from HTML

After the home step, the hidden `userSession` field is extracted from the HTML and stored in the session for login.

```scala
.check(regex(""""userSession" value="(.*?)"""").saveAs("userSession"))
```

#### After login: username in HTML and allowed status

Checks that the response contains the session username and the status is 200 or 302.

```scala
.check(substring("<b>#{name}</b>").exists)
.check(status.in(302, 200))
```

#### City list from markup (all matches)

All `option value="..."` values are collected into `CityFromResponse`. In this scenario, flight cities later come from a feeder—a separate exercise in unique values.

```scala
.check(regex("""option value="(.*?)"""").findAll.saveAs("CityFromResponse"))
```

---

### Session and failure handling

Illustrates saving an error flag, marking the session as succeeded, a mandatory correlation step, and stopping the scenario on failure.

```scala
.exec { session => session.set("ifFailed", session.isFailed.toString) }
.exec { session => session.markAsSucceeded }
.exec(WebToursAction.home).exitHereIfFailed
```

Conditional logic with `doIf` is in the same group `UC01_S01_Open_MainPage`.

---

### Unique values from a feeder

`doWhile` + `feed`: build a `Seq` of **unique** cities in the session (count from `NewScripts.VariablesOfCycles.CityCount`), then store the first two as `selectedCityDepart` / `selectedCityArrive`.

```scala
.doWhile(session => session("selectedCity").as[Seq[String]].length < NewScripts.VariablesOfCycles.CityCount) {
  feed(WebToursFeeder.City)
    .exec(session => {
      val value = session("City").as[String]
      val selectedCity = session("selectedCity").as[Seq[String]]
      val updated = if (!selectedCity.contains(value)) selectedCity :+ value else selectedCity
      session.set("selectedCity", updated)
    })
}
```

---

### Time and dates in the session

**Unix time (ms)** → session key `unixTimestamp` (also printed for debugging).

```scala
exec { session =>
  val unixTimestamp: Long = System.currentTimeMillis()
  println("unixTimestamp", unixTimestamp)
  session.set("unixTimestamp", unixTimestamp)
}
```

**Dates** as `MM/dd/yyyy` for +1 and +2 days → `plusOneDate` / `plusTwoDate` for the flight search form.

```scala
exec { session =>
  val t = LocalDateTime.now
  val f1 = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  val plusOneDate = f1.format(t.plusDays(1))
  val plusTwoDate = f1.format(t.plusDays(2))
  session.set("plusOneDate", plusOneDate).set("plusTwoDate", plusTwoDate)
}
```

---

### UUID without hyphens

Generate, strip `-`, take first 21 characters, save as `UUID_RND`.

```scala
val UUID_RND = UUID.randomUUID().toString.replaceAll("-", "").substring(0, 21)
session.set("UUID_RND", UUID_RND)
```

---

### URL encoding

Pick a random string from a list, encode with `URLEncoder.encode(..., "UTF-8")`, store original and encoded values in the session.

```scala
val dataList = List("https://www.google.com/search?q=geeks for geeks", "geeks for geeks")
val randomData = random.nextInt(dataList.size)
val urlEncoded = URLEncoder.encode(dataList(randomData), "UTF-8")
session
  .set("dataList_put", dataList(randomData))
  .set("dataList_urlEncoded_put", urlEncoded)
```

---

### Random `queryParam`

Store `itinerary` or `search` in the session, then pass it to the `page` query parameter via a `session` function.

```scala
.queryParam("page", session => {
  val randomNameProduct = session("randomNameProduct").as[String]
  s"""$randomNameProduct"""
})
```

---

### Variable-size JSON and request body

1. Random number of elements **from 1 to 5**, non-repeating indices, fields from predefined arrays.
2. Random suffixes for `lon` / `lat`.
3. Final JSON string in session `body`, then a request with `StringBody` from the session.

```scala
val numBlocks = Random.nextInt(5) + 1
val uniqueIndexes = Random.shuffle(retailerSkus.indices.toList).distinct
val blocks = uniqueIndexes.take(numBlocks).map { index =>
  val id = retailerSkus(index)
  val price = prices(index)
  val discount = discounts(index)
  s"""{"id":"$id","quantity":1,"price":$price,"discount":$discount,"promo_total":0}"""
}.mkString(",")
val body =
  s"""{"retailer_id":"610","location":{"lon":92.556$randomLon,"lat":67.14$randomLat},"items":[$blocks]}"""
session.set("body", body)
```

```scala
.body(StringBody(session => session("body").as[String]))
```

For WebTours this is a **demo** request: the server returns HTML, but a proxy (Fiddler, etc.) shows the JSON and how it changes between runs.

#### Example JSON shape

Session `body` is one JSON object. **`items`** is an array of length **1–5** (random, no duplicate `id` in one request). **`location.lon` / `lat`** are built with a fixed prefix plus `Random` suffixes. **`retailer_id`** is the string `"610"` in the interpolated body.

**Two `items`:**

```json
{
  "retailer_id": "610",
  "location": {
    "lon": 92.556752,
    "lat": 67.14516
  },
  "items": [
    {
      "id": "44444",
      "quantity": 1,
      "price": 200,
      "discount": 3,
      "promo_total": 0
    },
    {
      "id": "22222",
      "quantity": 1,
      "price": 500,
      "discount": 4,
      "promo_total": 0
    }
  ]
}
```

**Five `items` (script maximum):**

```json
{
  "retailer_id": "610",
  "location": {
    "lon": 92.556103,
    "lat": 67.14763
  },
  "items": [
    { "id": "22222", "quantity": 1, "price": 500, "discount": 4, "promo_total": 0 },
    { "id": "77777", "quantity": 1, "price": 900, "discount": 6, "promo_total": 0 },
    { "id": "44444", "quantity": 1, "price": 200, "discount": 3, "promo_total": 0 },
    { "id": "88888", "quantity": 1, "price": 300, "discount": 8, "promo_total": 0 },
    { "id": "33333", "quantity": 1, "price": 400, "discount": 7, "promo_total": 0 }
  ]
}
```

In real runs, `items` order and ids depend on `Random.shuffle` and `take(numBlocks)`; `location` numbers change with different random suffixes.

#### Fiddler screenshots (JSON inspector)

Generated request body in **Fiddler Classic** when running through the proxy (`Debug.scala`): **JSON** tab for `127.0.0.1:1080/cgi-bin/welcome.pl?page=search`.

**Two `items`:**

![Fiddler: JSON with two items](../images/fiddler-json-two-items.png)

**Five `items`:**

![Fiddler: JSON with five items](../images/fiddler-json-five-items.png)

---

### HTTP debugging

`logback-test.xml` sets logger `io.gatling.http.engine.response` to **DEBUG** and writes to `debug-<timestamp>.log`—useful alongside captured traffic.

---

### Proxy in `Debug.scala`

`NewScripts.Debug` uses proxy `127.0.0.1:8882` for traffic capture (e.g. Fiddler).

For a **normal run without a proxy**, remove `.proxy(...)` and use:

```scala
.protocols(httpProtocolWebTours)
```

Or adjust host/port for your tool.

---

### Running the WebTours scenario

You need **WebTours** locally (base URL in code: `http://127.0.0.1:1080`). Build and run with your **SBT / Gatling** setup in the IDE.

---

*Educational repository, not production code.*
