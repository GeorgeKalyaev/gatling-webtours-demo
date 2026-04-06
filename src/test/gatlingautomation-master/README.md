# Shell-автоматизация Gatling (Linux)

Набор `.sh`-скриптов для **Linux**: из **Git** подтягиваются сценарии и ресурсы, кладутся в **официальный Gatling bundle** (`user-files`), запускается симуляция, собираются отчёты и архив артефактов.

**Как это связано с Git:** `init.sh` делает **`git clone`** указанного репозитория в папку `projectGit/` (ветка из `GIT_BRANCH`). `updateGatlingScripts.sh` выполняет **`git pull`** и заново копирует файлы из путей `PROJECTGIT_RESOURCES_PATH` и `PROJECTGIT_SCRIPTS_PATH` в `gatling/user-files/`. URL репозитория задаётся в `setVars.sh` — это может быть GitLab, GitHub или любой другой `git` remote (в примере в `setVars.sh` указан GitLab).

---

## Установка

1. Положить **все** `.sh` скрипты из этой папки в **домашний каталог пользователя** (там же будут создаваться `gatling/`, `results/`, `projectGit/`).
2. Отредактировать **`setVars.sh`**:
   - `GIT_BRANCH`, `GIT_URL`, при необходимости `GIT_USER` / `GIT_PASS`;
   - `USE_GIT_LOGPASS` — автоматическая подстановка логина/пароля в URL при `clone`/`pull`;
   - `GATLING_MAINFILE` — класс симуляции в формате `пакет.ИмяКласса` (без `.scala`);
   - **`PROJECTGIT_RESOURCES_PATH`** и **`PROJECTGIT_SCRIPTS_PATH`** — относительные пути **внутри клонированного репозитория** до каталогов `src/test/resources` и `src/test/scala` (или вашей фактической структуры). В `setVars.sh` по умолчанию заданы пути под корень этого репозитория: `./projectGit/src/test/...`; для другого проекта замените на свои.
3. Запустить преднастройку:  
   `sh init.sh`
4. При выполнении может запроситься **пароль sudo** (установка `git`, `zip` через `apt-get`) и **учётные данные Git** (если `USE_GIT_LOGPASS=false` или для интерактивного clone).
5. Распаковать **Gatling bundle** (в `init.sh` в подсказке указан пример `gatling-charts-highcharts-bundle-3.9.5`) так, чтобы бинарники оказались в созданной папке **`gatling/`** (ожидаются `./gatling/bin/gatling.sh` и т.д.).
6. Выдать права на запуск Gatling, например:  
   `chmod +x ./gatling/bin/gatling.sh`  
   (при необходимости по политике окружения — `chmod 777`, как в вашей инструкции).

**Не запускайте скрипты от root** — иначе исказятся владельцы файлов в `gatling/` и `projectGit/`.

---

## Описание скриптов

| Скрипт | Назначение |
|--------|------------|
| **`init.sh`** | Создаёт каталоги `gatling`, `gatling/output`, `results`, `projectGit`; при отсутствии ставит `git` и `zip`; клонирует репозиторий в `./projectGit/`. |
| **`updateGatlingScripts.sh`** | Переключает ветку при необходимости, делает `git pull`, очищает `gatling/user-files/resources` и `gatling/user-files/simulations`, копирует туда свежие ресурсы и Scala из путей из `setVars.sh`. |
| **`launchGatling.sh`** | Запускает симуляцию через `nohup` и `gatling.sh -bm -rm local -s $GATLING_MAINFILE`, пишет вывод в `gatling/output/<timestamp>-g.out`, затем вызывает просмотр лога. |
| **`viewGatlingOutput.sh`** | `tail -f` последнего файла в `gatling/output/` (последние ~1000 строк). Выход: **Ctrl+C** (останавливает только просмотр, не сам Gatling). |
| **`stopGatling.sh`** | Завершает процессы, найденные по `pgrep -f gatling` (`kill -9`). |
| **`collectLastResult.sh`** | Берёт **последнюю по имени** подпапку в `gatling/results/`, строит HTML-отчёт с группами (если ещё не строился), формирует вариант **без групп** (обработка `simulation.log`), копирует `user-files` в папку артефактов, упаковывает всё в zip и кладёт в **`./results/<имя>_full.zip`** (относительно домашнего каталога, откуда запускаются скрипты). |
| **`deleteAllResultsAndLogs.sh`** | Удаляет логи в `gatling/`, содержимое `gatling/output/` и `gatling/results/` (осторожно: без интерактивного подтверждения в текущей версии скрипта). |

### Важно для `collectLastResult.sh`

Скрипт выбирает последнюю папку результата через `ls | tail -n 1`. Имеет смысл вызывать **после первого успешного прогона** Gatling. Не переименовывать и не создавать вручную лишние каталоги в `gatling/results/`, если полагаетесь на автоматический выбор «последнего» запуска.

---

## Возможные проблемы

1. **Запуск от root** — ломает права на файлы. Работать только от обычного пользователя.
2. Если часть файлов оказалась с владельцем root — поправить `chown`/`chmod` или заново развернуть bundle Gatling и повторить `init`/`update` от пользователя.
3. **Пути в `setVars.sh`** должны совпадать со структурой **вашего** клонированного репозитория; после смены репозитория (например с корпоративного GitLab на другой Git) обновите `PROJECTGIT_*` и при необходимости `GATLING_MAINFILE`.

---

## Краткая схема потока

```text
setVars.sh  →  URL ветки, пути к scala/resources внутри projectGit/
     ↓
init.sh     →  clone в projectGit/  +  каталоги gatling, results
     ↓
(вручную)   →  положить Gatling bundle в gatling/
     ↓
update...   →  pull + копирование в gatling/user-files/
     ↓
launch...   →  прогон + tail лога
     ↓
collect...  →  отчёты + zip в results/
```
