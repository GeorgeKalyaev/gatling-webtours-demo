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

## 4. Связь с остальным материалом

- Автоматизация через **`init.sh` / setVars.sh`** и копирование из Git описаны в [Shell-автоматизация (Linux)](02-shell-automation-linux.md).
- Сбор **`…_full.zip`** и Excel — [Отчёт Gatling: архив, Excel, два генератора](05-gatling-report-excel.md).

---

*Учебный репозиторий, не продакшен-код.*
