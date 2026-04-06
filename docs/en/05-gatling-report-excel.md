# Gatling reporting: log archive, Excel, two load generators

**[← Documentation](../../README.md)** · **[Русская версия](../ru/05-gatling-report-excel.md)**

This page summarizes a practical workflow: on **Linux**, package a **`<run>_full`** folder (logs **with** and **without** Gatling groups) and a zip; on **Windows**, build an **`.xls`** workbook with **Errors** and **Requests per min**; and how to merge runs from **two generators**.

**Scripts in the repo:** [`tools/reporting/`](../../tools/reporting/) — `reportsZip.sh`, `combineB2C.py`, `combineB2C_NOZIP.py`, `MergeSimulation.py`, [`requirements.txt`](../../tools/reporting/requirements.txt). The full PDF with screenshots stays in your local “Инструкция по построению отчета для Гатлинга” folder.

---

## 1. Single generator: `reportsZip.sh`

The script is **interactive**. Run it on the host where the **Gatling bundle** lives (example: `gatling-charts-highcharts-bundle-3.9.5`) and **`results/`** contains the run folder (e.g. `debug-20231002164043692` with `simulation.log`).

1. Place `reportsZip.sh` next to the bundle (same level as `gatling-charts-highcharts-bundle-3.9.5`).
2. Run: `sh /home/USER/reportsZip.sh` (use your path).
3. Enter the **full path to the Gatling root**, e.g. `/home/USER/gatling-charts-highcharts-bundle-3.9.5`.
4. Enter the **full path to `results/`**, e.g. `/home/USER/gatling-charts-highcharts-bundle-3.9.5/results/`.
5. Enter the **run folder name** (no trailing slash).

**Output:** under `results/`, a directory `<name>_full` and `<name>_full.zip`. Inside `_full`:

- **`with_groups/`** — artifacts and **`simulation.log`** with groups;
- **`without_groups/`** — log **without** `GROUP` lines (filtered + second **reports-only** Gatling pass).

On a **low-memory** generator, the grouped pass on a huge log may fail; that is an environment limit.

**Path caveat:** the “with groups” branch calls `sh ./gatling-charts-highcharts-bundle-3.9.5/bin/gatling.sh` from the **current working directory** after internal `cd` steps. The bundle folder name and location must match your install; change that line in `reportsZip.sh` or mirror the original layout. The “without groups” branch uses `${path}/bin/gatling.sh`.

Download **`…_full.zip`** to your PC for the next step.

---

## 2. Excel from one zip: `combineB2C.py`

**OS:** intended for **Windows** (log subpaths use `\\`).

**Dependencies:** Python 3 and packages from [`tools/reporting/requirements.txt`](../../tools/reporting/requirements.txt):

```text
pip install -r tools/reporting/requirements.txt
```

1. Put **`combineB2C.py`** and **`…_full.zip`** in one folder (the name without `.zip` matches the folder produced when unpacking).
2. Run: `python combineB2C.py`.
3. Result file name **without extension** (e.g. `statistika` → `statistika.xls`).
4. **Folder / archive base name without `.zip`** — same as the zip (e.g. `debug-20231002164043692_full`).

The script unpacks the zip and reads:

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
