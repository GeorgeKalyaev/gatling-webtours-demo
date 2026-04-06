# Manual Gatling load test run (Linux)

**[← Documentation](../../README.md)** · **[Русская версия](../ru/06-gatling-manual-run-linux.md)**

How to deploy data and simulations into the official Gatling **bundle** and start a run **by hand** (without the `gatlingautomation-master` script set). Full PDF with screenshots: **`Инструкция по запуску теста на Гатлинге (2).pdf`** in your Downloads folder.

Paths and class name (**`NewScripts.Debug`**, folder **`3.9.5_new`**) come from a real example — replace with your bundle path, user, and simulation FQN.

---

## 1. `user-files`: where resources and scripts go

1. On the load generator, open **`/home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5_new/user-files/`** (or your **`…/user-files/`** path inside the bundle).

   ![WinSCP: user-files — lib, resources, simulations](../images/gatling-manual-user-files.png)

2. Copy **data pools** (CSV and other feeder assets) into **`user-files/resources/`**.

   ![WinSCP: resources — data pools and config](../images/gatling-manual-resources.png)

3. Copy **Scala simulations** into **`user-files/simulations/NewScripts/`**, including **`Debug.scala`**, **`HttpSberMarket.scala`**, and the rest of your project layout.

   ![WinSCP: simulations/NewScripts — Debug, HttpSberMarket, UCs](../images/gatling-manual-simulations-newscripts.png)

The **`-s NewScripts.Debug`** argument must match **`package` + object name** in **`Debug.scala`**.

---

## 2. Background launch and monitoring

4. Start **in the background** and write console output to a timestamped file:

```bash
nohup /home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5_new/bin/gatling.sh -bm -rm local -s NewScripts.Debug > $(date +%s)-g.out &
```

Adjust the path to **`gatling.sh`** and the simulation class (**`-s …`**).

5. Find the newest **`*-g.out`** file (from the directory where you launched the command, often home):

```bash
ls -t
```

The latest log is usually listed first.

   ![Terminal: ls -t — bundle dir and *-g.out files](../images/gatling-manual-ls-t-g-out.png)

6. **Watch** progress (replace **`1693350446-g.out`** with your file name):

```bash
tail -f 1693350446-g.out -n1000
```

- **`tail -f`** streams new lines as they appear;
- **`-n1000`** prints the **last 1000** lines first, then keeps following.

Press **Ctrl+C** to stop watching — Gatling **keeps running**.

   ![Terminal: Gatling progress while tail -f (OK/KO by scenario)](../images/gatling-manual-tail-f.png)

---

## 3. Stopping the test (emergency)

To **forcibly** stop Gatling processes on the host:

1. Become root: **`sudo -s`** (enter password when asked).

2. List matching processes:

```bash
ps aux | grep gatling | grep -v grep
```

   ![Terminal: gatling.sh / GatlingCLI / JVM processes](../images/gatling-stop-ps-running.png)

3. Kill them:

```bash
pkill -f 'gatling'
```

**Warning:** this stops **every** process whose command line contains **`gatling`**, including other users’ runs. On shared hosts prefer **`kill <PID>`** from step 2 for a surgical stop.

4. Run **`ps aux | grep gatling | grep -v grep`** again — there should be **no output**.

   ![Terminal: after pkill, no Gatling processes](../images/gatling-stop-ps-empty.png)

---

## 4. Related docs

- Git-driven automation: [Shell automation (Linux)](02-shell-automation-linux.md).
- Report zip / Excel workflow: [Gatling report: zip, Excel, two generators](05-gatling-report-excel.md).

---

*Educational repository, not production code.*
