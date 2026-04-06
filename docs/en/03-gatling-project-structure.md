# Gatling project structure

**[← Documentation home](../../README.md)** · **[Русская версия](../ru/03-gatling-project-structure.md)**

### Project structure (“script tree”)

**Gatling project components (overview)**

**Contents**

| Section | Topics |
|--------|--------|
| **[Repository tree](#en-structure-layout)** | `src/test` layout: `resources`, `scala/NewScripts`, `gatlingautomation-master` |
| **[Folder roles](#en-structure-roles)** | Feeders/config vs `Simulation` / scenarios |
| **[Large-suite pattern](#en-structure-pattern)** | `*Action`, `*CommonScenario`, `*Feeder` per business flow |
| **[Shared `Protocols`](#en-structure-protocols)** | `HttpProtocolBuilder`, `baseUrl`, default checks |
| **[Shared `FeederGlobe`](#en-structure-feederglobe)** | Central `csv(...)` pools under `resources/` |
| **[`Simulation` entry (`Debug`)](#en-structure-simulation)** | `setUp`, `VariablesOfCycles`, `inject` |

<a id="en-structure-layout"></a>

#### Layout of this repo (`src/test`)

```text
src/test/
├── resources/                      ← data pools & Gatling config
│   ├── City.csv
│   ├── Users.csv
│   ├── gatling.conf
│   └── logback-test.xml
├── scala/NewScripts/
│   ├── Debug.scala                 ← Simulation: setUp(), inject, protocol
│   ├── HttpSberMarket.scala        ← shared Protocols + FeederGlobe (template)
│   └── WebTours/
│       ├── WebToursAction.scala    ← HTTP steps + checks
│       ├── WebToursCommonScenario.scala
│       └── WebToursFeeder.scala
└── gatlingautomation-master/       ← Linux shell automation ([doc](02-shell-automation-linux.md))
```

<a id="en-structure-roles"></a>

#### What lives where

- **`resources/`** — CSV feeders (“pools”), `gatling.conf`, `logback-test.xml`. Gatling resolves feeder file names relative to this folder.
- **`scala/...`** — `Simulation` subclasses and scenarios. In SBT projects this is typically under `src/test/scala` (Gatling bundle: `user-files/simulations`).

<a id="en-structure-pattern"></a>

#### Pattern common in large suites (many use cases)

Teams often add **one package per business flow** (e.g. `UC26_...`) with three recurring file roles:

| Piece | Typical name | Role |
|-------|----------------|------|
| **Actions** | `*Action` | `HttpRequestBuilder`s: path, headers, body, `check` / correlation (`regex`, `jsonPath`, …). |
| **Scenario** | `*CommonScenario` / `*CommonScena` | `scenario(...)`: `feed`, `group`, `exec`, calling `*Action` values in order. |
| **Feeder** | `*Feeder` | `csv(...)` / iterators for that flow, or thin wrappers over shared pools. |

Cross-cutting pieces are placed at package level, similar to this repo’s **`Debug`** (simulation entry), **`Protocols`** (HTTP defaults), and **`FeederGlobe`** (shared CSV feeders).

<a id="en-structure-protocols"></a>

#### Shared HTTP defaults — `Protocols`

[`HttpSberMarket.scala`](../../src/test/scala/NewScripts/HttpSberMarket.scala) defines `package object Protocols` with one or more `HttpProtocolBuilder` values (`baseUrl`, headers, global `check(status.in(...))`). That is the “single place for environment-specific HTTP” pattern from larger Gatling codebases (Web / B2B / API variants in one object).

<a id="en-structure-feederglobe"></a>

#### Shared data pools — `FeederGlobe`

The same file defines `object FeederGlobe` with `csv("SomeFile.csv").circular` lines. Those files are expected under `resources/`. **This demo repo** lists several names as **templates** without committing every CSV; in a full project the names match real pool files.

<a id="en-structure-simulation"></a>

#### `Simulation` entry — `Debug` and load profile

[`Debug.scala`](../../src/test/scala/NewScripts/Debug.scala) extends `Simulation` and runs `setUp(...)`. [`VariablesOfCycles`](../../src/test/scala/NewScripts/Debug.scala) (same file) holds scenario tuning constants (here `CityCount`). A full profile often adds per-UC intensity coefficients and `inject(rampUsersPerSec(...), constantUsersPerSec(...))`; this repository keeps a minimal `atOnceUsers(1)` example plus proxy.

---

*Educational repository, not production code.*
