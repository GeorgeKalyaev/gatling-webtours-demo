# Описание составляющей части проекта (Gatling)

**[← К документации](../../README.md)** · **[English version](../en/03-gatling-project-structure.md)**

### Структура проекта («дерево скриптов»)

**Описание составляющей части проекта (Gatling)**

**Оглавление**

| Раздел | О чём |
|--------|--------|
| **[Дерево `src/test`](#ru-structure-layout)** | `resources`, `scala/NewScripts`, `gatlingautomation-master` |
| **[Роли каталогов](#ru-structure-roles)** | Пулы/конфиг vs симуляции и сценарии |
| **[Паттерн крупного проекта](#ru-structure-pattern)** | `*Action`, `*CommonScenario`, `*Feeder` на бизнес-поток |
| **[Общие `Protocols`](#ru-structure-protocols)** | `HttpProtocolBuilder`, `baseUrl`, общие проверки |
| **[Общий `FeederGlobe`](#ru-structure-feederglobe)** | Централизованные `csv(...)` из `resources/` |
| **[Точка входа `Simulation` (`Debug`)](#ru-structure-simulation)** | `setUp`, `VariablesOfCycles`, `inject` |

<a id="ru-structure-layout"></a>

#### Этот репозиторий (`src/test`)

```text
src/test/
├── resources/                      ← пулы данных и конфиг Gatling
│   ├── City.csv
│   ├── Users.csv
│   ├── gatling.conf
│   └── logback-test.xml
├── scala/NewScripts/
│   ├── Debug.scala                 ← Simulation: setUp(), inject, протокол
│   ├── HttpSberMarket.scala        ← общие Protocols + FeederGlobe (шаблон)
│   └── WebTours/
│       ├── WebToursAction.scala    ← HTTP-шаги и проверки
│       ├── WebToursCommonScenario.scala
│       └── WebToursFeeder.scala
└── gatlingautomation-master/       ← shell-автоматизация Linux ([документ](02-shell-automation-linux.md))
```

<a id="ru-structure-roles"></a>

#### Роли каталогов

- **`resources/`** — CSV-фидеры («пулы»), `gatling.conf`, `logback-test.xml`. Имена файлов для `csv(...)` резолвятся относительно этой папки.
- **`scala/...`** — классы `Simulation` и сценарии. В SBT это обычно `src/test/scala`; в bundle Gatling — `user-files/simulations`.

<a id="ru-structure-pattern"></a>

#### Типичный паттерн в крупных проектах (много UC)

Часто делают **отдельный пакет на бизнес-поток** (например `UC26_...`) и три типа файлов:

| Часть | Типичное имя | Назначение |
|-------|----------------|------------|
| **Действия** | `*Action` | `HttpRequestBuilder`: путь, заголовки, тело, `check` / корреляция (`regex`, `jsonPath`, …). |
| **Сценарий** | `*CommonScenario` / `*CommonScena` | `scenario(...)`: `feed`, `group`, `exec`, вызовы `*Action` в нужном порядке. |
| **Фидер** | `*Feeder` | `csv(...)` / итераторы для этого потока или обёртки над общими пулами. |

Общие вещи выносят на уровень пакета — как здесь **`Debug`** (точка входа симуляции), **`Protocols`** (HTTP по умолчанию) и **`FeederGlobe`** (общие CSV).

<a id="ru-structure-protocols"></a>

#### Общие HTTP-настройки — `Protocols`

В [`HttpSberMarket.scala`](../../src/test/scala/NewScripts/HttpSberMarket.scala) объявлен `package object Protocols` с одним или несколькими `HttpProtocolBuilder` (`baseUrl`, заголовки, общий `check(status.in(...))`). Это приём «одно место для окружений» из больших наборов сценариев (Web / B2B / API и т.д.).

<a id="ru-structure-feederglobe"></a>

#### Общие пулы — `FeederGlobe`

В том же файле — `object FeederGlobe` с строками вида `csv("Имя.csv").circular`. Файлы ожидаются в **`resources/`**. В **этом демо-репозитории** часть имён задана как **шаблон** без всех CSV в Git; в боевом проекте имена совпадают с реальными пулами.

<a id="ru-structure-simulation"></a>

#### Точка входа `Simulation` — `Debug` и профиль нагрузки

[`Debug.scala`](../../src/test/scala/NewScripts/Debug.scala) расширяет `Simulation` и задаёт `setUp(...)`. Объект [`VariablesOfCycles`](../../src/test/scala/NewScripts/Debug.scala) в том же файле — константы настройки сценария (здесь `CityCount`). В полноценном профиле часто добавляют коэффициенты интенсивности по UC и `inject(rampUsersPerSec(...), constantUsersPerSec(...))`; здесь оставлен минимальный пример `atOnceUsers(1)` и прокси.

---

Учебный репозиторий, не продакшен-код.
