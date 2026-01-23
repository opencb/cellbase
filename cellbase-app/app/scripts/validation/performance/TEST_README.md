# CellBase Response Time Testing

This script compares the response times between CellBase v5.8 and v6.7 for various variants and include parameters.

## Installation

Install the required Python packages:

```bash
pip install -r requirements_test.txt
```

Or install them individually:

```bash
pip install requests tabulate
```

## Usage

The script supports two commands:

### 1. Run Command - Execute Queries

Execute all queries and save results to a file:

```bash
python test_response_time.py run --results-file results.json
```

If `--results-file` is not specified, results are saved to `cellbase_response_times.json` by default.

The run command will:
- Execute all 308 queries (7 variants × 22 include parameters × 2 versions)
- Display progress during execution
- Show a detailed table with results
- Display statistics (overall and per-include parameter)
- Save results to both JSON and CSV files

### 2. Display Command - Show Saved Results

Load and display previously saved results:

```bash
python test_response_time.py display --results-file results.json
```

The display command will:
- Load results from the specified JSON file
- Display the same formatted tables and statistics as the run command
- No queries are executed (useful for reviewing past results)

### Help

To see all available commands and options:

```bash
python test_response_time.py --help
python test_response_time.py run --help
python test_response_time.py display --help
```

## Output Files

The script creates several output files:

### 1. Results Files (from `run` command)

- **JSON file** (e.g., `results.json`): Contains complete test results in JSON format, can be loaded with the display command
- **CSV file** (e.g., `results.csv`): Contains the same data in CSV format for easy import into spreadsheets
- **Queries folder** (e.g., `results_queries/`): Contains individual JSON responses from each query

Both results files include columns: include, variant, time_v58, time_v67, size_v58, size_v67, url_v58, url_v67

The `time` columns show response times in milliseconds (ms).
The `size` columns show response sizes in kilobytes (KB).

**Note on Difference Calculations**: 
All differences in statistics are calculated as `v6.7 - v5.8`:
- Positive values (+) indicate an increase in v6.7 (worse performance or larger response size)
- Negative values (-) indicate a decrease in v6.7 (better performance or smaller response size)

### 2. Query JSON Responses

When running tests, each query's full JSON response is saved to a separate file in a folder named `{results-file}_queries/`. 

For example, if you run:
```bash
python test_response_time.py run --results-file my_test.json
```

The script will create:
- `my_test.json` - Summary results
- `my_test.csv` - Summary results in CSV format
- `my_test_queries/` - Folder containing individual query responses

Each query JSON file is named using the pattern: `{include}_{variant}_{version}.json`

Examples:
- `variation_1_68188386_C_T_v5.8.json`
- `xrefs_1_68188593_T_C_v6.7.json`
- `consequenceType_1_68189093_T_G_v5.8.json`

This allows you to:
- Inspect the full response for any specific query
- Compare responses between v5.8 and v6.7 in detail
- Debug issues with specific variants or include parameters
- Validate the correctness of responses

## Configuration

You can modify the script to:
- Change the tested variants (edit `VARIANTS` list)
- Change the include parameters (edit `INCLUDE_PARAMS` list)
- Adjust the delay between requests (modify `time.sleep()` calls)
- Change the timeout duration (modify `timeout` parameter in `requests.get()`)

## Sample Output

```
Include       Variant          Time v5.8 (ms)  Time v6.7 (ms)  Size v5.8 (KB)  Size v6.7 (KB)  URL v5.8          URL v6.7
------------  ---------------  --------------  --------------  --------------  --------------  ----------------  ----------------
variation     1:68188386:C:T   45              50              1.02            1.15            https://ws...     https://ws...
xrefs         1:68188593:T:C   12              32              0.85            0.92            https://ws...     https://ws...
...
```

## Notes

- The script includes a small delay (0.1s) between requests to avoid overwhelming the server
- Failed requests are marked as "ERROR" in the output
- Response times are extracted from the `responses[0].time` field in the JSON response
- Results are sorted by include parameter for easy comparison

