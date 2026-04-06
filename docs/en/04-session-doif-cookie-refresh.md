# Checking for any error in the previous step via `doIf(session => session("ifFailed").as[Boolean])`

**[← Documentation home](../../README.md)** · **[Русская версия](../ru/04-session-doif-cookie-refresh.md)**

Also covers a **cookie refresh** example and CSV logging when branching the scenario.

---

<a id="section-doif-boolean"></a>

## 1. Checking the previous step: `doIf` and a failure flag

You often want logic like: “if the last request failed, append this user id to a retry file and stop; otherwise continue.”

Gatling already exposes **`session.isFailed`**. Prefer that instead of re-storing it as a string and parsing it back.

### Typical mistake

```scala
.exec { session =>
  session.set("ifFailed", session.isFailed.toString) // "true" / "false" as String
}
.doIf(session => session("ifFailed").as[Boolean]) { // ClassCastException or wrong behaviour
  // ...
}
```

`as[Boolean]` expects a **Boolean** in the session, not the string `"true"`.

### Safer options

**A — store a Boolean**

```scala
.exec { session =>
  session.set("ifFailed", session.isFailed)
}
.doIf(session => session("ifFailed").as[Boolean]) {
  // ...
}
```

**B — use `isFailed` directly**

```scala
.doIf(_.isFailed) {
  // ...
}
```

**C — combine with `exitHereIfFailed`** after a critical step so later steps do not run when correlation failed.

---

## 2. Example idea: cookie refresh on a marketplace-style project

Flow (simplified):

1. **`feed`** — user/account row from CSV.
2. **`addCookie`** — set the **current** session cookie (e.g. value from feeder).
3. **`getMain`** (or home) — hit the site; checks may mark the session failed if the session is invalid.
4. If **failed** — append user id (or cookie) to a **“needs refresh”** CSV for offline analysis; **`exitHereIfFailed`**.
5. **`addCookie`** again — set **new** session value (e.g. saved from a prior refresh job or correlation).
6. Call an **auth check** request; again branch on failure / success.
7. On **success** — append the user id to a **“valid new cookies”** CSV.

The original teaching material used concrete CSV names and `scala.reflect.io.File.appendAll`. For new code, prefer **`java.nio.file`** or explicit writers with encoding; `scala.reflect.io.File` is dated but appears in older scripts.

### Illustrative fragment (names generalized)

```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._

// Feeder.UsersForUpdate, Action.getMain, Action.apiSessionCheck — your project types

scenario("Update session cookie")
  .feed(Feeder.UsersForUpdate)
  .exec(addCookie(Cookie("APP_SESSION", "#{accounts}")))
  .exec(Action.getMain)
  .exec { session =>
    session.set("ifFailed", session.isFailed)
  }
  .doIf(session => session("ifFailed").as[Boolean]) {
    exec { session =>
      // e.g. append user id to a retry list (use java.nio in production code)
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
      // log again for failed refresh
      session
    }
  }
  .exitHereIfFailed
  .doIf(session => !session("ifFailed").as[Boolean]) {
    exec { session =>
      // persist id for successfully refreshed users
      session
    }
  }
```

Adjust cookie **name**, **feeder keys**, and **`Action.*`** to your application. The **WebTours** demo in this repo uses a related but smaller pattern (`markAsSucceeded`, `exitHereIfFailed`) in `WebToursCommonScenario.scala` — compare with the Boolean approach above.

---

## 3. Excerpt from notes (cookie refresh, `doIf`, CSV)

Below is the **same idea** as in your course/work notes: refresh `_Instamart_session`, branch on `ifFailed`, append to CSV. It is **not** checked into this repository’s `src`; included here for the portfolio write-up.

**Notes:**

- The combination **`session.set("ifFailed", session.isFailed.toString)`** and **`as[Boolean]`** is again visible — see [section 1 above](#section-doif-boolean): prefer storing a **Boolean** or **`doIf(_.isFailed)`**.
- Lines with **`appendAll(session("id_polizovat…`** and **`as[S session`** were **truncated** in the PDF/screenshot. Below they are **completed to valid Scala** (session key assumed `id_polizovatelya`); rename if your feeder column differs.

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
          .appendAll(session("id_polizovatelya").as[String]) // source had truncated "id_polizovat session"
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
          .appendAll(session("id_polizovatelya").as[String]) // source had truncated "as[S session"
        session
      }
    }
}
```

`Feeder.UsersForUpdate`, `Action.getMain`, and `Action.api_shopping_session_test_auth` must exist in your own project.

---

*Educational repository, not production code.*
