# Shell automation (Linux)

**[← Documentation home](../../README.md)** · **[Русская версия](../ru/02-shell-automation-linux.md)**

> **Files in this repo:** [`src/test/gatlingautomation-master/`](../../src/test/gatlingautomation-master) — all `.sh` and [`setVars.sh`](../../src/test/gatlingautomation-master/setVars.sh). On a Linux machine, copy them to the user’s **home directory** before running `init.sh`.

**Bash** scripts for **Linux**: pull scenarios and resources from **Git**, copy them into the **official Gatling bundle** (`gatling/user-files`), run the simulation, build reports and a zip of artifacts.

**Git flow:** `init.sh` **`git clone`**s into `./projectGit/` (branch `GIT_BRANCH`). `updateGatlingScripts.sh` **`git pull`**s and recopies from `PROJECTGIT_RESOURCES_PATH` and `PROJECTGIT_SCRIPTS_PATH` into `gatling/user-files/`. Repo URL is in `setVars.sh` (**GitHub**, **GitLab**, or any HTTPS remote).

---

### 2.1. Setup

1. Copy **all** `.sh` files from `gatlingautomation-master` to the user’s **home directory** (`gatling/`, `results/`, `projectGit/` will appear there).
2. Edit **`setVars.sh`**: `GIT_BRANCH`, `GIT_URL`, optional `GIT_USER` / `GIT_PASS`; `USE_GIT_LOGPASS` (usually `false` for public repos); `GATLING_MAINFILE` (e.g. `NewScripts.Debug`); **`PROJECTGIT_RESOURCES_PATH`** / **`PROJECTGIT_SCRIPTS_PATH`** relative to the clone root (template matches this repo: `./projectGit/src/test/...`).
3. Run `sh init.sh`
4. You may need **sudo** (install `git`, `zip`) and **Git credentials** for private repos or when `USE_GIT_LOGPASS=false`.
5. Unpack the **Gatling bundle** (see `init.sh` hint, e.g. `gatling-charts-highcharts-bundle-3.9.5`) into **`gatling/`** so `./gatling/bin/gatling.sh` exists.
6. `chmod +x ./gatling/bin/gatling.sh` (or `chmod 777` if required by policy).

**Do not run these scripts as root**—ownership under `gatling/` and `projectGit/` will break.

---

### 2.2. Scripts

| Script | Purpose |
|--------|---------|
| **`init.sh`** | Creates `gatling`, `gatling/output`, `results`, `projectGit`; installs `git`/`zip` if missing; **`git clone`** into `./projectGit/`. |
| **`updateGatlingScripts.sh`** | `git checkout` if branch changed, then **`git pull`**; wipes `gatling/user-files/resources` and `simulations`; copies resources and Scala from `setVars.sh` paths. |
| **`launchGatling.sh`** | `nohup` + `gatling.sh -bm -rm local -s $GATLING_MAINFILE`, log under `gatling/output/<timestamp>-g.out`, then tails the log. |
| **`viewGatlingOutput.sh`** | `tail -f` the latest file in `gatling/output/`. **Ctrl+C** stops viewing only, not Gatling. |
| **`stopGatling.sh`** | `pgrep -f gatling` + `kill -9`. |
| **`collectLastResult.sh`** | Uses the **lexicographically last** folder in `gatling/results/`, builds HTML with and **without** groups, copies `user-files`, zips to **`./results/<name>_full.zip`**. |
| **`deleteAllResultsAndLogs.sh`** | Deletes logs and clears `output/` and `results/` (**no** interactive confirm in the current script). |

#### `collectLastResult.sh` note

“Latest” result is chosen with `ls | tail -n 1`. Run **after** the first successful Gatling run. Avoid manual renames/extra folders under `gatling/results/` if you rely on this logic.

---

### 2.3. Troubleshooting

1. Running as **root** breaks permissions—use a normal user.
2. Root-owned files—fix with `chown`/`chmod` or redeploy the bundle and repeat steps as the user.
3. **`setVars.sh` paths** must match **your** clone layout; change `PROJECTGIT_*` and `GATLING_MAINFILE` when switching repos.

---

### 2.4. Flow diagram

```text
setVars.sh     →  URL, branch, scala/resources paths inside projectGit/
      ↓
init.sh        →  clone into projectGit/ + gatling, results dirs
      ↓
(manual)       →  Gatling bundle into gatling/
      ↓
update...      →  pull + copy into gatling/user-files/
      ↓
launch...      →  run + tail log
      ↓
collect...     →  reports + zip in results/
```

---

*Educational repository, not production code.*
