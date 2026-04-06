# Shell-автоматизация (Linux)

**[← К документации](../../README.md)** · **[English version](../en/02-shell-automation-linux.md)**

> **Файлы в репозитории:** [`src/test/gatlingautomation-master/`](../../src/test/gatlingautomation-master) — все `.sh` и [`setVars.sh`](../../src/test/gatlingautomation-master/setVars.sh). Для работы на машине их копируют в **домашний каталог** пользователя (см. установку ниже).

Набор **bash**-скриптов для **Linux**: из **Git** подтягиваются сценарии и ресурсы, копируются в **официальный Gatling bundle** (`gatling/user-files`), запускается симуляция, собираются отчёты и zip с артефактами.

**Связь с Git:** `init.sh` делает **`git clone`** в `./projectGit/` (ветка `GIT_BRANCH`). `updateGatlingScripts.sh` выполняет **`git pull`** и снова копирует файлы из путей `PROJECTGIT_RESOURCES_PATH` и `PROJECTGIT_SCRIPTS_PATH` в `gatling/user-files/`. Адрес репозитория — в `setVars.sh` (**GitHub**, **GitLab** или другой HTTPS remote).

---

### 2.1. Установка

1. Скопировать **все** `.sh` из `gatlingautomation-master` в **домашний каталог** пользователя (там появятся `gatling/`, `results/`, `projectGit/`).
2. Отредактировать **`setVars.sh`**:
   - `GIT_BRANCH`, `GIT_URL`, при необходимости `GIT_USER` / `GIT_PASS`;
   - `USE_GIT_LOGPASS` — вшивать ли логин/пароль в URL при `clone`/`pull` (для публичного репозитория обычно `false`);
   - `GATLING_MAINFILE` — класс симуляции, формат `пакет.ИмяКласса` (например `NewScripts.Debug`);
   - **`PROJECTGIT_RESOURCES_PATH`** и **`PROJECTGIT_SCRIPTS_PATH`** — пути **от корня клона** до `src/test/resources` и `src/test/scala` (в шаблоне задано под этот репозиторий: `./projectGit/src/test/...`).
3. Запуск преднастройки: `sh init.sh`
4. При необходимости — **sudo** (установка `git`, `zip`), **учётные данные Git** (если репозиторий приватный или `USE_GIT_LOGPASS=false`).
5. Распаковать **Gatling bundle** (в подсказке `init.sh` — пример `gatling-charts-highcharts-bundle-3.9.5`) в каталог **`gatling/`**, чтобы существовал `./gatling/bin/gatling.sh`.
6. Права на запуск: `chmod +x ./gatling/bin/gatling.sh` (или `chmod 777`, если так принято в окружении).

**Не запускать скрипты от root** — иначе съедут владельцы файлов в `gatling/` и `projectGit/`.

---

### 2.2. Описание скриптов

| Скрипт | Назначение |
|--------|------------|
| **`init.sh`** | Каталоги `gatling`, `gatling/output`, `results`, `projectGit`; при отсутствии — `apt-get install git zip`; **`git clone`** в `./projectGit/`. |
| **`updateGatlingScripts.sh`** | При смене ветки — `git checkout`, затем **`git pull`**; очистка `gatling/user-files/resources` и `simulations`; копирование ресурсов и Scala из путей из `setVars.sh`. |
| **`launchGatling.sh`** | `nohup` + `gatling.sh -bm -rm local -s $GATLING_MAINFILE`, лог в `gatling/output/<timestamp>-g.out`, затем вызов просмотра лога. |
| **`viewGatlingOutput.sh`** | `tail -f` последнего файла в `gatling/output/`. Выход: **Ctrl+C** (только просмотр, не останавливает Gatling). |
| **`stopGatling.sh`** | `pgrep -f gatling` и `kill -9`. |
| **`collectLastResult.sh`** | Последняя по имени папка в `gatling/results/`, HTML с группами и вариант **без групп**, копия `user-files`, zip в **`./results/<имя>_full.zip`**. |
| **`deleteAllResultsAndLogs.sh`** | Удаление логов и содержимого `output/` и `results/` (в текущей версии **без** интерактивного подтверждения). |

#### Важно для `collectLastResult.sh`

Выбор «последнего» результата — через `ls | tail -n 1`. Вызывать **после первого успешного прогона**. Не переименовывать и не плодить вручную каталоги в `gatling/results/`, если полагаетесь на автоматику.

---

### 2.3. Возможные проблемы

1. Запуск **от root** — портятся права; работать от обычного пользователя.
2. Файлы с владельцем root — `chown`/`chmod` или заново развернуть bundle и повторить шаги от пользователя.
3. Пути в **`setVars.sh`** должны совпадать со структурой **вашего** клона; при смене репозитория обновить `PROJECTGIT_*` и `GATLING_MAINFILE`.

---

### 2.4. Схема потока

```text
setVars.sh     →  URL, ветка, пути к scala/resources внутри projectGit/
      ↓
init.sh        →  clone в projectGit/ + каталоги gatling, results
      ↓
(вручную)      →  Gatling bundle в gatling/
      ↓
update...      →  pull + копирование в gatling/user-files/
      ↓
launch...      →  прогон + tail лога
      ↓
collect...     →  отчёты + zip в results/
```

---

Учебный репозиторий, не продакшен-код.
