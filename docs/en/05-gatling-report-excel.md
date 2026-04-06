# Gatling reporting: log archive, Excel, two load generators

**[← Documentation](../../README.md)** · **[Русская версия](../ru/05-gatling-report-excel.md)**

This page summarizes a practical workflow: on **Linux**, package a **`<run>_full`** folder (logs **with** and **without** Gatling groups) and a zip; on **Windows**, build an **`.xls`** workbook with **Errors** and **Requests per min**; and how to merge runs from **two generators**.

**Scripts in the repo:** [`tools/reporting/`](../../tools/reporting/) — `reportsZip.sh`, `combineB2C.py`, `combineB2C_NOZIP.py`, `MergeSimulation.py`, [`requirements.txt`](../../tools/reporting/requirements.txt). The full PDF with screenshots stays in your local “Инструкция по построению отчета для Гатлинга” folder.

---

<a id="section-1-reportszip"></a>

## 1. Single generator: `reportsZip.sh`

The script is **interactive**. Run it on the host where the **Gatling bundle** lives (example: `gatling-charts-highcharts-bundle-3.9.5`) and **`results/`** contains the run folder (e.g. `debug-20231002164043692` with `simulation.log`).

1. Place `reportsZip.sh` next to the bundle (same level as `gatling-charts-highcharts-bundle-3.9.5`).

   ![Home directory on the load generator: Gatling bundle folder and `reportsZip.sh` side by side](../images/reportsZip-same-level-as-bundle.png)

2. Run the script, e.g. `/home/g_kalyaev/reportsZip.sh` (if execute permission is missing, use `sh /home/g_kalyaev/reportsZip.sh`; adjust user and path).
3. When prompted for the **full path to the Gatling folder**, enter e.g. `/home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5`.
4. When prompted for the **full path to the results folder**, enter e.g. `/home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5/results/`.
5. When prompted for the **run folder name** (no trailing slash), enter the directory that contains **`simulation.log`**, e.g. `debug-20231002164043692`.

6. After the script finishes, under `…/gatling-charts-highcharts-bundle-3.9.5/results/` you should see a **`<name>_full` directory** and a **`<name>_full.zip`** archive (screenshot below for the same example run).

   ![Results directory: `…_full` folder and `…_full.zip` archive](../images/results-full-folder-and-zip.png)

7. Download **`…_full.zip`** to your PC. The archive holds **`simulation.log`** in two forms: **with** Gatling groups and **without** (see **`with_groups`** and **`without_groups`** inside `_full`). The **grouped** report may **fail to build** if the log is very large and the generator runs **out of memory**.

Inside `<name>_full`:

- **`with_groups/`** — artifacts and **`simulation.log`** with groups;
- **`without_groups/`** — log **without** `GROUP` lines (filtered + second **reports-only** Gatling pass).

**Path caveat:** the “with groups” branch calls `sh ./gatling-charts-highcharts-bundle-3.9.5/bin/gatling.sh` from the **current working directory** after internal `cd` steps. The bundle folder name and location must match your install; change that line in `reportsZip.sh` or mirror the original layout. The “without groups” branch uses `${path}/bin/gatling.sh`.

Continue with section 2 on the PC.

---

## 2. Excel from one zip: `combineB2C.py`

**OS:** intended for **Windows** (log subpaths use `\\`).

1. Create a folder, put the downloaded **`…_full.zip`** archive and **`combineB2C.py`** inside (from [`tools/reporting/`](../../tools/reporting/) in the repo or your own copy).

2. In **CMD**, `cd` to that folder and run: `python combineB2C.py`. You need **Python 3** plus **`xlwt`** and **`pandas`**:

   ```text
   pip install xlwt pandas
   ```

   Or from the repo root: `pip install -r tools/reporting/requirements.txt` — see [`requirements.txt`](../../tools/reporting/requirements.txt).

3. When prompted for the **output file name** (Russian prompt: «Введите имя результирующего файла:»), enter a name **without extension** (example below: `statistika` → `statistika.xls`).

   ![CMD: running combineB2C.py and entering the output file name](../images/combineB2C-cmd-output-filename.png)

4. At the next prompt (**«Введите имя папки:»**), enter the **same base name as the zip file without the `.zip` extension** — one token, **no spaces** (e.g. `debug-20231002164043692_full` for `debug-20231002164043692_full.zip`). The script unpacks it and resolves `with_groups` / `without_groups` paths.

While running, it reads:

- `…/with_groups/simulation.log` — errors matching `error_codes_to_track`;
- `…/without_groups/simulation_without_groups.log` — successful **REQUEST** lines for hard-coded B2C endpoints.

**Sheets:** **Errors** (Error Code, Group, Endpoint, Count) and **Requests per min** (time bucket + four API columns). For **another API**, edit the `request` comparisons and column headers.

5. **CMD output:** after both answers, the script prints a line like `Объединенные результаты сохранены в statistika.xls` (file name from step 3). Below is a full session with both prompts and inputs.

   ![CMD: both interactive prompts for combineB2C.py](../images/combineB2C-cmd-both-prompts.png)

6. **In the working folder**, after a successful run you get the **`.xls` report** (example: `statistika.xls` from step 3) and a **directory** with the **extracted** archive contents (e.g. `debug-20231002164043692_full` — same base name as `…_full.zip` without the extension). `combineB2C.py` and the original zip remain alongside.

   ![File Explorer: script, zip, extracted `_full` folder, and `statistika.xls`](../images/combineB2C-folder-result-explorer.png)

7. Open the generated **Excel** file (e.g. `statistika.xls`) and confirm the sheets contain data.

8. The **Errors** sheet shows **error statistics for the whole run**: **Error Code**, **Group**, **Endpoint**, and **Count** — log error text, Gatling group, endpoint, and how often it occurred.

   ![Excel, Errors sheet: error statistics for the full test](../images/combineB2C-xls-errors-sheet.png)

9. The **Requests per min** sheet is **requests-per-minute statistics** (**OK** lines from the no-groups log): a **time** column plus four tracked endpoints (app/web order completion, app/web shipment cancellation — headers match `combineB2C.py`).

   ![Excel, Requests per min sheet](../images/combineB2C-xls-requests-per-min.png)

---

## 3. Two generators: merge logs, then `combineB2C_NOZIP.py`

Build **`…_full.zip` on each** of the two generators ([§3.0](#section-3-0-reportszip)), **download both** archives, lay out logs on the PC, **merge** pairs, rename for `combineB2C_NOZIP.py`, then produce one `.xls`.

<a id="section-3-0-reportszip"></a>

### 3.0. On each generator: `reportsZip.sh` (error table from two hosts)

To merge statistics later, run the packaging steps **on every generator** (same flow as [section 1](#section-1-reportszip); the run folder name `debug-…` will be **different** on the second host).

1. Place **`reportsZip.sh`** next to **`gatling-charts-highcharts-bundle-3.9.5`** (see the screenshot in section 1).
2. Run e.g. `/home/g_kalyaev/reportsZip.sh` or `sh /home/g_kalyaev/reportsZip.sh`.
3. Gatling root path: `/home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5`.
4. Results path: `/home/g_kalyaev/gatling-charts-highcharts-bundle-3.9.5/results/`.
5. On **this** generator, enter the folder name that contains **`simulation.log`** (example: `debug-20231002164043692` — on the other generator use **its** folder for the paired run).
6. Under **`…/results/`** you get a **`<name>_full` directory** and **`<name>_full.zip`**. The screenshot shows another run id (`debug-20231002164039914`); your names match step 5.

   ![results: `_full` folder and zip after reportsZip.sh (one generator example)](../images/reportsZip-results-two-generators-step6.png)

Repeat steps **1–6 on the second generator**, then download **both** `…_full.zip` files to your PC.

### 3.1. Inside `_full`, download logs, PC folder layout (steps 7–15)

7. Under each **`debug-…_full`** directory (from the zip or on the host) you have **`without_groups`** and **`with_groups`**; each contains its own **`simulation.log`** (with and without Gatling groups).

8. Repeat on the **second** generator. You end up with **four** log files: one in **`with_groups`** and one in **`without_groups`** on **each** machine.

9. **Copy / download** all four logs to your PC (SFTP, WinSCP, etc.).

10. Create a working folder, e.g. **`task2`**, and place **[`MergeSimulation.py`](../../tools/reporting/MergeSimulation.py)** there.

11. Inside **`task2`**, create **`test1`**, then **another nested** folder also named **`test1`**, so the path is **`Report1\task2\test1\test1`** (top-level `Report1` is optional, match your layout).

12. Inside the **inner** **`test1`**, create **`with_groups`** and **`without_groups`**.

13. In **`with_groups`**, add **`simulation.log`** from generator **1** (`…_full/with_groups/`) and **`simulation.log`** from generator **2** (same path on the other host). Because names collide, **rename one** (example below: `simulation1.log`).

14. In **`without_groups`**, do the same for both **`without_groups`** logs; rename the second file if needed (e.g. **`simulation2.log`**).

15. **`with_groups`** should look like this: two log files side by side (**`simulation.log`** and **`simulation1.log`** in the example).

   ![Explorer: Report1\task2\test1\test1\with_groups — two logs from two generators](../images/two-generators-with-groups-two-logs.png)

Summary:

```text
Report1/task2/MergeSimulation.py
Report1/task2/test1/test1/with_groups/     simulation.log + simulation1.log
Report1/task2/test1/test1/without_groups/ simulation.log + simulation2.log   (secondary names — your choice)
```

### 3.2. `MergeSimulation.py`

1. Set **`input_folder`** in [`MergeSimulation.py`](../../tools/reporting/MergeSimulation.py) to the directory that holds the `.log` files (the original guide used **forward slashes** in paths).
2. Run: `python MergeSimulation.py` — creates **`output_file`** (default in repo: `merged_simulation.log`).

Repeat for **`with_groups`** and **`without_groups`**, then rename as required:

- **`with_groups`:** merged file → **`simulation.log`**;
- **`without_groups`:** merged file → **`simulation_without_groups.log`**.

### 3.3. `combineB2C_NOZIP.py`

Unlike `combineB2C.py`, this script **does not** unpack a zip — it expects an on-disk tree (e.g. folder `test1` with `with_groups` and `without_groups`).

1. Place the script next to the **parent** of that folder (example: `Report1/task2/` beside `test1`).
2. `python combineB2C_NOZIP.py` → output **`.xls` name** → **folder name** (not `test1/test1` unless that is where the two subfolders really live).

The result workbook has **Errors** and **Requests per min** for the **combined** two-generator logs.

---

## 4. Summary HTML for endpoints without groups (two generators, on server)

Instead of Excel only: under `…_full/`, create a folder (e.g. `svodnaia_table`), copy `without_groups/simulation_without_groups.log` from this host, copy the peer log from the other host (`scp` or WinSCP), then:

```bash
/path/to/gatling-charts-highcharts-bundle-3.9.5/bin/gatling.sh -nr -ro /path/to/results/debug-…_full/svodnaia_table/
```

File names inside `svodnaia_table` must match what **reports-only** mode expects for your Gatling version; if your layout differs from the internal guide, check the docs for **`-ro`**.

---

## 5. Limits and reusing for other projects

| Item | Note |
|------|------|
| Bundle path in `reportsZip.sh` | One branch uses a **fixed** relative bundle directory — adjust for your install. |
| `combineB2C*.py` | Error strings and **URLs** are B2C-specific; change for WebTours or other APIs. |
| `MergeSimulation.py` | Concatenation order follows **`os.walk`**; sort/rename files if order matters. |
| `.xls` | Uses **xlwt**; switching to xlsx needs a different library. |

---

*Educational repository, not production code.*
