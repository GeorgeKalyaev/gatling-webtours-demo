# gatling-webtours-demo

Учебный сценарий на **Gatling** для демо-приложения **HP WebTours**: HTTP-шаги, проверки ответов, сессия, фидеры, генерация данных и динамическое тело запроса.

**Репозиторий:** [github.com/GeorgeKalyaev/gatling-webtours-demo](https://github.com/GeorgeKalyaev/gatling-webtours-demo)

---

## Структура кода

- `NewScripts.WebTours.WebToursAction` — описание запросов и `check`.
- `NewScripts.WebTours.WebTours` (сценарий в `WebToursCommonScenario.scala`) — группы шагов, фидеры, генерация данных в сессии.
- `NewScripts.WebTours.WebToursFeeder` и CSV в `src/test/resources` — данные для виртуальных пользователей и городов.
- `src/test/resources/logback-test.xml` — при необходимости подробный лог HTTP-ответов в файл.

---

## Проверки ответов (`check`)

### Корреляция: `userSession` из HTML

После открытия домашней страницы из ответа вытаскивается скрытое поле `userSession` и сохраняется в сессию для логина.

```scala
.check(regex(""""userSession" value="(.*?)"""").saveAs("userSession"))
```

### После логина: имя в HTML и допустимый статус

Проверяется, что в ответе есть имя пользователя из сессии, и статус — 200 или 302.

```scala
.check(substring("<b>#{name}</b>").exists)
.check(status.in(302, 200))
```

### Список городов из разметки (все совпадения)

С страницы выбора рейса собираются все `option value="..."` в список `CityFromResponse`. В текущем сценарии для полёта дальше используются города из фидера — отдельный учебный блок про уникальность.

```scala
.check(regex("""option value="(.*?)"""").findAll.saveAs("CityFromResponse"))
```

---

## Сессия и обход при ошибках

Иллюстрация: сохранение признака ошибки, сброс состояния сессии как успешной, обязательный шаг с корреляцией и выход из сценария при провале.

```scala
.exec { session => session.set("ifFailed", session.isFailed.toString) }
.exec { session => session.markAsSucceeded }
.exec(WebToursAction.home).exitHereIfFailed
```

Условное выполнение по значению в сессии (`doIf`) — в той же группе `UC01_S01_Open_MainPage`.

---

## Уникальные значения из фидера

Цикл `doWhile` + `feed`: набираем в сессии `Seq` из **уникальных** городов (количество задаётся в `NewScripts.VariablesOfCycles.CityCount`), затем первый и второй элементы кладутся в `selectedCityDepart` / `selectedCityArrive`.

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

## Время и даты в сессии

**Unix time (мс)** — в сессию `unixTimestamp` (плюс вывод в консоль для отладки).

```scala
exec { session =>
  val unixTimestamp: Long = System.currentTimeMillis()
  println("unixTimestamp", unixTimestamp)
  session.set("unixTimestamp", unixTimestamp)
}
```

**Даты** в формате `MM/dd/yyyy` на +1 и +2 дня — в `plusOneDate` / `plusTwoDate` для формы поиска рейса.

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

## UUID без дефисов

Генерация строки, удаление `-`, укорочение до 21 символа, сохранение в `UUID_RND`.

```scala
val UUID_RND = UUID.randomUUID().toString.replaceAll("-", "").substring(0, 21)
session.set("UUID_RND", UUID_RND)
```

---

## URL encoding

Случайная строка из списка кодируется `URLEncoder.encode(..., "UTF-8")`; оригинал и закодированный вариант сохраняются в сессии.

```scala
val dataList = List("https://www.google.com/search?q=geeks for geeks", "geeks for geeks")
val randomData = random.nextInt(dataList.size)
val urlEncoded = URLEncoder.encode(dataList(randomData), "UTF-8")
session
  .set("dataList_put", dataList(randomData))
  .set("dataList_urlEncoded_put", urlEncoded)
```

---

## Случайный `queryParam`

В сессию пишется `itinerary` или `search`, затем значение подставляется в параметр `page` функцией от `session`.

```scala
.queryParam("page", session => {
  val randomNameProduct = session("randomNameProduct").as[String]
  s"""$randomNameProduct"""
})
```

---

## JSON переменного размера и подстановка в body

1. Случайное число элементов **от 1 до 5**, индексы без повторов, поля из заранее заданных массивов.
2. Случайные хвосты для `lon` / `lat`.
3. Итоговая строка JSON в сессии `body`, затем запрос с `StringBody` из сессии.

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

Для WebTours это **демонстрационный** запрос: сервер отвечает HTML, зато в прокси (Fiddler и т.п.) видно сформированный JSON и его изменение от прогона к прогону.

### Как выглядит итоговый JSON

Строка в сессии `body` — один JSON-объект. Поле **`items`** — массив объектов; длина массива **от 1 до 5** (случайно, без повторяющихся `id` в одном запросе). У **`location`** координаты **`lon`** / **`lat`** — числа с «хвостом» из `Random` (в коде конкатенация `92.556` + `randomLon` и `67.14` + `randomLat`). **`retailer_id`** в теле — строка `"610"`, как в `s"""..."""`.

Ниже — примеры того же формата, что видно в инспекторе JSON (два прогона: короткий и максимальный по числу позиций).

**Вариант с двумя позициями в `items`:**

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

**Вариант с пятью позициями (максимум в скрипте):**

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

В реальном прогоне порядок элементов в `items` и набор `id` зависят от `Random.shuffle` и `take(numBlocks)`; числа в `location` будут другими при других случайных суффиксах.

### Скриншоты Fiddler (инспектор JSON)

Так выглядит сгенерированное тело запроса в **Fiddler Classic** при прогоне через прокси (`Debug.scala`): вкладка **JSON** для запроса к `127.0.0.1:1080/cgi-bin/welcome.pl?page=search`.

**Два элемента в `items`:**

![Fiddler: JSON с двумя позициями в items](docs/images/fiddler-json-two-items.png)

**Пять элементов в `items` (максимум в скрипте):**

![Fiddler: JSON с пятью позициями в items](docs/images/fiddler-json-five-items.png)

---

## Отладка HTTP

В `logback-test.xml` для логгера `io.gatling.http.engine.response` задан уровень **DEBUG** с выводом в файл `debug-<timestamp>.log` — удобно сопоставлять с перехваченным трафиком.

---

## Прокси в `Debug.scala`

Симуляция `NewScripts.Debug` задаёт прокси `127.0.0.1:8882` для записи трафика (например, Fiddler).

Для **обычного прогона без прокси** уберите `.proxy(...)` у протокола и оставьте только:

```scala
.protocols(httpProtocolWebTours)
```

Либо измените хост и порт прокси под ваш перехватчик.

---

## Запуск

Нужен локальный **WebTours** (в коде базовый URL `http://127.0.0.1:1080`). Сборка и запуск — через ваш SBT/Gatling-проект, как настроено в IDE.

### Shell-автоматизация на Linux

В каталоге [`src/test/gatlingautomation-master`](src/test/gatlingautomation-master) лежат **bash-скрипты** для сценария «Git → Gatling bundle на Linux»: `init.sh` клонирует репозиторий в `projectGit/`, `updateGatlingScripts.sh` делает `git pull` и копирует `src/test/resources` и `src/test/scala` в `user-files` установленного Gatling, `launchGatling.sh` / `stopGatling.sh` / `viewGatlingOutput.sh` управляют прогоном и логом, `collectLastResult.sh` собирает HTML-отчёты и zip с артефактами в `results/`. URL и ветка Git, пути к исходникам внутри клона и имя класса симуляции задаются в `setVars.sh`.

Пошаговая установка, таблица скриптов, ограничения (не запускать от root, выбор «последнего» результата) — в **[README автоматизации](src/test/gatlingautomation-master/README.md)**.

Учебный репозиторий, не продакшен-код.
