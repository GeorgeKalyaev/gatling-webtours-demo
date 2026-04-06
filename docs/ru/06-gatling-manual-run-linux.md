# Ручной запуск нагрузочного теста Gatling (Linux)

**[← К документации](../../README.md)** · **[English version](../en/06-gatling-manual-run-linux.md)**

Инструкция по развёртыванию сценариев и данных в **официальном bundle** Gatling и запуску симуляции **вручную** (без набора `gatlingautomation-master`). Полный PDF со скриншотами: **`Инструкция по запуску теста на Гатлинге (2).pdf`** в вашей папке загрузок.

Пути и имена класса (**`NewScripts.Debug`**, каталог **`3.9.5_new`**) — из рабочего примера; подставьте свой bundle, пользователя и пакет симуляции.

---

## 1. Каталог `user-files`: куда класть ресурсы и скрипты

1. На генераторе перейдите в **`/home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5_new/user-files/`** (или ваш путь к **`…/user-files/`** внутри bundle).

   ![WinSCP: user-files — lib, resources, simulations](../images/gatling-manual-user-files.png)

2. Скопируйте **пулы данных** (CSV и прочие файлы для фидеров) в **`user-files/resources/`**.

   ![WinSCP: resources — пулы и конфиги](../images/gatling-manual-resources.png)

3. Скопируйте **Scala-сценарии** нагрузочного теста в **`user-files/simulations/NewScripts/`**, включая **`Debug.scala`**, **`HttpSberMarket.scala`** и связанные модули/пакеты по структуре вашего проекта.

   ![WinSCP: simulations/NewScripts — Debug, HttpSberMarket, UC](../images/gatling-manual-simulations-newscripts.png)

Имя класса симуляции в команде запуска (**`-s NewScripts.Debug`**) должно совпадать с **`package` + имя объекта** в **`Debug.scala`**.

---

## 2. Запуск в фоне и мониторинг

4. Запуск **в фоне** с перенаправлением вывода в файл с меткой времени в имени:

```bash
nohup /home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5_new/bin/gatling.sh -bm -rm local -s NewScripts.Debug > $(date +%s)-g.out &
```

Подставьте путь к **`gatling.sh`** и класс симуляции (**`-s …`**) под вашу установку.

5. Узнать **имя свежего** файла **`…-g.out`** в домашнем каталоге (или там, где вы запускали команду):

```bash
ls -t
```

Первым в списке обычно оказывается последний созданный лог.

   ![Терминал: ls -t — каталог bundle и файлы *-g.out](../images/gatling-manual-ls-t-g-out.png)

6. **Мониторинг** хода прогона (подставьте своё имя файла вместо **`1693350446-g.out`**):

```bash
tail -f 1693350446-g.out -n1000
```

- **`tail -f`** — потоковый вывод новых строк;
- **`-n1000`** — сначала показать последние **1000** строк файла, затем продолжить следить.

Выход из просмотра: **Ctrl+C** (сам Gatling при этом **не** останавливается).

   ![Терминал: вывод Gatling при tail -f (OK/KO по сценариям)](../images/gatling-manual-tail-f.png)

---

## 3. Завершение теста (экстренная остановка)

Если нужно **принудительно** остановить все процессы Gatling на машине:

1. Войти под привилегиями root: **`sudo -s`** (ввести пароль при запросе).

2. Посмотреть процессы, в командной строке которых есть **`gatling`**:

```bash
ps aux | grep gatling | grep -v grep
```

   ![Терминал: процессы gatling.sh / GatlingCLI / JVM](../images/gatling-stop-ps-running.png)

3. Завершить их одной командой:

```bash
pkill -f 'gatling'
```

**Внимание:** на хосте будут завершены **все** процессы, чья командная строка содержит подстроку **`gatling`**, в том числе чужие прогоны. На общем генераторе используйте точечный **`kill <PID>`** по списку из шага 2, если нужна осторожность.

4. Повторить **`ps aux | grep gatling | grep -v grep`**: вывода быть не должно — прогон остановлен.

   ![Терминал: после pkill список процессов Gatling пуст](../images/gatling-stop-ps-empty.png)

---

## 4. Генерация отчёта вручную после принудительной остановки

Если прогон оборвали **`pkill`** или иначе без штатного завершения, Gatling **сам** HTML-отчёт мог не собрать. Тогда отчёт строят командой **reports-only** (**`-ro`**) по уже записанному **`simulation.log`** в папке результата.

1. Откройте **`…/gatling-charts-highcharts-bundle-3.9.5_new/results/`** (или ваш путь к **`results`** внутри bundle).

   ![WinSCP: каталог results и папки debug-…](../images/gatling-report-manual-results-folder.png)

2. Выберите нужный прогон по имени папки **`debug-…`**. Внутри должна быть **`simulation.log`**.

   ![WinSCP: внутри debug-папки — simulation.log](../images/gatling-report-manual-simulation-log.png)

3. Из каталога bundle (или с корректным **`PATH`**) выполните **`gatling.sh -ro`**, указав **полный путь к выбранной папке `debug-…`** (без слэша в конце или по правилам вашей версии Gatling):

```bash
/home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5_new/bin/gatling.sh -ro /home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5_new/results/debug-20230829215021235
```

Подставьте свой путь и имя папки. Генерация может занять заметное время.

4. После завершения рядом с **`simulation.log`** появятся **`index.html`** и сопутствующие файлы отчёта (группы, запросы и т.д.). Откройте **`index.html`** в браузере.

   ![WinSCP: после -ro — index.html и файлы отчёта](../images/gatling-report-manual-index-html.png)

Точный контракт аргумента **`-ro`** (абсолютный путь vs имя относительно **`results`**) смотрите в документации вашей версии Gatling.

---

## 5. Возможности для анализа логов

### 5.1. Ошибки в **`simulation.log`** (в папке **`debug-…`**)

Рабочий каталог — папка с **`simulation.log`**, например:

```bash
cd ~/gatling-charts-highcharts-bundle-3.9.5_new/results/debug-20230831210522588
```

**1.** Сводка по ответам с нужным текстом ошибки (пример для **`found 500`**): взять 3-ю колонку, убрать числовые суффиксы в путях, посчитать уникальные эндпоинты, сортировка по возрастанию счётчика:

```bash
date; cat simulation.log | grep "found 500" | awk '{print $3}' | sed 's/-[0-9]*//g' | sort | uniq -c | sort -h
```

Тот же приём подходит для **`found 404`**, **`found 429`** и т.д. На скриншоте ниже — **аналогичный** разбор для **`found 429`**.

   ![Терминал: подсчёт found 429 по эндпоинтам](../images/gatling-log-analysis-http-status-pipeline.png)

**2.** Все неуспешные строки **`REQUEST`**: седьмое поле (сообщение об ошибке), частоты:

```bash
cat simulation.log | grep REQUEST | grep -v OK | awk -F'\t' '{print $7}' | sort | uniq -c | sort
```

   ![Терминал: сводка по полю ошибки REQUEST (не OK)](../images/gatling-log-analysis-request-ko.png)

Формат колонок в логе зависит от версии Gatling; при смещении полей поправьте номер в **`awk`**.

### 5.2. Подробный **`debug-…T….log`** в корне bundle

**3.** В корне установки Gatling лежат тяжёлые логи консоли вида **`debug-20230829T215018.484.log`**.

   ![WinSCP: корень bundle — debug-*.log рядом с bin, results, user-files](../images/gatling-bundle-root-debug-logs.png)

Пример: отфильтровать **`found 404`**, сгруппировать одинаковые строки, убрать шум **`DEBUG`** (**`grep -P`** — из **GNU grep**; на macOS может понадобиться **`ggrep`** или другой шаблон):

```bash
cd /home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5_new
grep -P "found 404" debug-20230710T191846.316.log | sort | uniq -c | sort | grep -v DEBUG
```

Имя файла **`debug-….log`** подставьте своё.

   ![Терминал: grep по большому debug-логу (found 404)](../images/gatling-log-analysis-debug-file-404.png)

---

## 6. Связь с остальным материалом

- Автоматизация через **`init.sh` / setVars.sh`** и копирование из Git описаны в [Shell-автоматизация (Linux)](02-shell-automation-linux.md).
- Сбор **`…_full.zip`** и Excel — [Отчёт Gatling: архив, Excel, два генератора](05-gatling-report-excel.md).

---

*Учебный репозиторий, не продакшен-код.*
