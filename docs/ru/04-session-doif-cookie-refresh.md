# Проверка кода на наличие любой ошибки в предыдущем шаге через `doIf(session => session("ifFailed").as[Boolean])`

**[← К документации](../../README.md)** · **[English version](../en/04-session-doif-cookie-refresh.md)**

Дополнительно разобран пример **обновления cookie** и записи в CSV при ветвлении сценария.

---

<a id="section-doif-boolean"></a>

## 1. Проверка предыдущего шага: `doIf` и флаг ошибки

Задача: «если последний запрос завершился с ошибкой — дописать пользователя в файл на доработку и выйти; иначе продолжить».

В Gatling уже есть **`session.isFailed`**. Удобнее опираться на него, а не класть в сессию строку `"true"`/`"false"` и потом парсить.

### Частая ошибка

```scala
.exec { session =>
  session.set("ifFailed", session.isFailed.toString) // строка "true" / "false"
}
.doIf(session => session("ifFailed").as[Boolean]) { // неверно: в сессии String, не Boolean
  // ...
}
```

У `as[Boolean]` ожидается в сессии именно **Boolean**, а не строка.

### Надёжные варианты

**A — хранить Boolean**

```scala
.exec { session =>
  session.set("ifFailed", session.isFailed)
}
.doIf(session => session("ifFailed").as[Boolean]) {
  // ...
}
```

**B — без промежуточной переменной**

```scala
.doIf(_.isFailed) {
  // ...
}
```

**C — после критичного шага использовать **`exitHereIfFailed`**, чтобы дальше не шли шаги при провале корреляции.

---

## 2. Пример: обновление cookie (маркетплейс / веб-проект)

Упрощённый поток:

1. **`feed`** — строка из CSV (аккаунт / старый cookie).
2. **`addCookie`** — выставить **текущую** сессионную cookie.
3. **`getMain`** (или главная) — заход на сайт; при невалидной сессии `check` помечают сессию как failed.
4. Если **ошибка** — дописать id пользователя (или cookie) в CSV «нужна повторная выдача»; **`exitHereIfFailed`**.
5. Снова **`addCookie`** — подставить **новое** значение сессии (из фидера, из другого шага или из офлайн-обработки).
6. Запрос **проверки авторизации** с новой cookie; снова ветвление.
7. При **успехе** — дописать id в CSV «успешно обновлённые».

В учебных материалах встречались конкретные имена файлов и `scala.reflect.io.File.appendAll`. Для нового кода лучше **`java.nio.file`** или потоки с явной кодировкой; `scala.reflect.io.File` устаревает, но часто остаётся в старых скриптах.

### Иллюстративный фрагмент (имена обобщены)

```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._

// Feeder.UsersForUpdate, Action.getMain, Action.apiSessionCheck — из вашего проекта

scenario("Обновление session cookie")
  .feed(Feeder.UsersForUpdate)
  .exec(addCookie(Cookie("APP_SESSION", "#{accounts}")))
  .exec(Action.getMain)
  .exec { session =>
    session.set("ifFailed", session.isFailed)
  }
  .doIf(session => session("ifFailed").as[Boolean]) {
    exec { session =>
      // например дописать user id в список на повтор (в проде — java.nio)
      session
    }
  }
  .exitHereIfFailed
  .exec(addCookie(Cookie("APP_SESSION", "#{NewSession}")))
  .exec(Action.apiSessionCheck)
  .exec { session =>
    session.set("ifFailed", session.isFailed)
  }
  .doIf(session => session("ifFailed").as[Boolean]) {
    exec { session =>
      // снова залогировать неуспешное обновление
      session
    }
  }
  .exitHereIfFailed
  .doIf(session => !session("ifFailed").as[Boolean]) {
    exec { session =>
      // сохранить id пользователей с успешно обновлённой сессией
      session
    }
  }
```

Имя cookie, ключи фидера и **`Action.*`** подставляются под ваше приложение. В этом репозитории в **WebTours** есть родственный, но более короткий фрагмент (`markAsSucceeded`, `exitHereIfFailed`) в `WebToursCommonScenario.scala` — имеет смысл сравнить с вариантом с **Boolean** выше.

---

## 3. Фрагмент из конспекта (обновление cookie, `doIf`, CSV)

Ниже — **тот же смысл**, что в ваших материалах: сценарий обновления `_Instamart_session`, ветки по `ifFailed`, запись в CSV. В **этом репозитории исходник не лежит**; блок приведён для портфолио.

**Важно:**

- Здесь снова видно сочетание **`session.set("ifFailed", session.isFailed.toString)`** и **`as[Boolean]`** — см. [раздел 1 выше](#section-doif-boolean): для корректной работы лучше класть **`Boolean`** или использовать **`doIf(_.isFailed)`**.
- В PDF/скриншотах строки с **`appendAll(session("id_polizovat…`** и **`as[S session`** были **обрезаны**. В коде ниже они **дописаны до валидного Scala** (предполагается ключ сессии `id_polizovatelya`); если у вас другое имя колонки фидера — замените.

```scala
package UpdateCookie.UpdateCookies

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.{Cookie, addCookie}

object CSVUsersUpdateCookie {
  def apply(): ScenarioBuilder = scenario("CSVUsersUpdateCookie")
    .exec(new CSVUsersUpdateCookie().scn)
}

class CSVUsersUpdateCookie {
  val scn: ScenarioBuilder = scenario("RedisUsersUpdateCookie")
    .feed(Feeder.UsersForUpdate)
    .exec(addCookie(Cookie("_Instamart_session", "#{accounts}")))
    .exec(Action.getMain)
    .exec { session =>
      session.set("ifFailed", session.isFailed.toString)
    }
    .doIf(session => session("ifFailed").as[Boolean]) {
      exec { session =>
        scala.reflect.io.File("OldCookie_Need_Again_b2b_21_12_2023.csv")
          .appendAll(session("id_polizovatelya").as[String]) // было: обрыв «id_polizovat session»
        session
      }
    }
    .exitHereIfFailed
    .exec(addCookie(Cookie("_Instamart_session", "#{NewInstamart_session}")))
    .exec(Action.api_shopping_session_test_auth)
    .exec { session =>
      session.set("ifFailed", session.isFailed.toString)
    }
    .doIf(session => session("ifFailed").as[Boolean]) {
      exec { session =>
        scala.reflect.io.File("OldCookie_Need_Again_b2b_21_12_2023.csv")
          .appendAll(session("id_polizovatelya").as[String])
        session
      }
    }
    .exitHereIfFailed
    .doIf(session => !session("ifFailed").as[Boolean]) {
      exec { session =>
        scala.reflect.io.File("NewCokies_b2b_21_12_2023.csv")
          .appendAll(session("id_polizovatelya").as[String]) // было: обрыв «as[S session»
        session
      }
    }
}
```

`Feeder.UsersForUpdate`, `Action.getMain`, `Action.api_shopping_session_test_auth` должны быть определены в вашем проекте.

---

Учебный репозиторий, не продакшен-код.
