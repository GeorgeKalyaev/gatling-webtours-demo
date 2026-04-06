# Gatling reporting: log archive, Excel, two load generators

**[← Documentation](../../README.md)** · **[Русская версия](../ru/05-gatling-report-excel.md)**

This page summarizes a practical workflow: on **Linux**, package a **`<run>_full`** folder (logs **with** and **without** Gatling groups) and a zip; on **Windows**, build an **`.xls`** workbook with **Errors** and **Requests per min**; and how to merge runs from **two generators**.

**Scripts in the repo:** [`tools/reporting/`](../../tools/reporting/) — `reportsZip.sh`, `combineB2C.py`, `combineB2C_NOZIP.py`, `MergeSimulation.py`, [`requirements.txt`](../../tools/reporting/requirements.txt). The full PDF with screenshots stays in your local “Инструкция по построению отчета для Гатлинга” folder.

---

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

4. At the next prompt, enter the **archive base name without `.zip`**, matching the zip in the folder (e.g. `debug-20231002164043692_full`). The script unpacks it and resolves `with_groups` / `without_groups` paths.

It then reads:

- `…/with_groups/simulation.log` — errors matching `error_codes_to_track`;
- `…/without_groups/simulation_without_groups.log` — successful **REQUEST** lines for hard-coded B2C endpoints.

**Sheets:** **Errors** (Error Code, Group, Endpoint, Count) and **Requests per min** (time bucket + four API columns). For **another API**, edit the `request` comparisons and column headers.

---

## 3. Two generators: merge logs, then `combineB2C_NOZIP.py`

Run **section 1** on **each** generator, download both **`…_full.zip`** archives, lay out logs on the PC, **merge** pairs, rename for `combineB2C_NOZIP.py`, then produce one `.xls`.

### 3.1. Example folder layout

```text
Report1/task2/test1/test1/with_groups/     ← simulation.log + simulation1.log (one per host)
Report1/task2/test1/test1/without_groups/  ← simulation.log + simulation2.log
```

Secondary file names may vary; you need **one** merged log per branch in the end.

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
