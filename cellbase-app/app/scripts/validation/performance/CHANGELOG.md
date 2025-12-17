# Changelog - CellBase Response Time Test Script

## Latest Changes

### Added Response Size Tracking

The script now calculates and tracks the response size (in KB) for each query:

1. **Response Size Calculation**: Each HTTP response size is calculated in kilobytes
2. **New Columns in Output**:
   - Table: `Size v5.8 (KB)` and `Size v6.7 (KB)` columns
   - CSV: `size_v58` and `size_v67` columns
   - Column order: `include, variant, time_v58, time_v67, size_v58, size_v67, url_v58, url_v67`

3. **Enhanced Statistics**:
   - Overall statistics now show average, min, and max sizes for both versions
   - Per-include statistics show average sizes and size differences
   - Size differences help identify which include parameters return larger responses
   - **Difference Calculation**: All differences computed as `v6.7 - v5.8`
     - Positive values (+) = increase in v6.7 (worse performance or larger size)
     - Negative values (-) = decrease in v6.7 (better performance or smaller size)

### Added Query JSON Response Saving

The script now saves the complete JSON response from each query to individual files:

1. **Query Folder**: Automatically created based on the results file name
   - Pattern: `{results_file}_queries/`
   - Example: If `--results-file test.json`, folder is `test_queries/`

2. **Individual JSON Files**: Each query response is saved separately
   - Naming pattern: `{include}_{variant}_{version}.json`
   - Examples:
     - `variation_1_68188386_C_T_v5.8.json`
     - `xrefs_1_68188593_T_C_v6.7.json`
   - Special characters in variants (`:`) are replaced with underscores (`_`)

3. **Benefits**:
   - Inspect full response data for each query
   - Compare v5.8 vs v6.7 responses in detail
   - Debug specific query issues
   - Validate response correctness

## Previous Changes

### Added Full URLs to Results

The script now includes the complete URLs for both v5.8 and v6.7 in:

1. **Console Output Table**: The results table now displays 6 columns:
   - Include
   - Variant  
   - Time v5.8 (ms)
   - Time v6.7 (ms)
   - URL v5.8
   - URL v6.7

2. **CSV Output File**: The `cellbase_response_times.csv` file now includes columns:
   - include
   - variant
   - time_v58
   - time_v67
   - url_v58
   - url_v67

### Example URLs Generated

The script generates full URLs like:
- `https://ws.zettagenomics.com/cellbase/webservices/rest/v5.8/hsapiens/genomic/variant/1:68188386:C:T/annotation?dataRelease=8&include=variation`
- `https://ws.zettagenomics.com/cellbase/webservices/rest/v6.7/hsapiens/genomic/variant/1:68188386:C:T/annotation?dataRelease=1&include=variation`

This makes it easy to:
- Verify which exact endpoints were tested
- Re-run specific queries manually if needed
- Debug any issues with particular variants or include parameters
- Share exact URLs for reproduction or verification

### Technical Changes

- Modified `fetch_response_time()` to return a tuple of `(response_time, success, full_url)`
- Updated `run_tests()` to capture and store URLs for both versions
- Updated `display_results()` to show URLs in the table output
- Updated `save_results_to_csv()` to include URL columns in the CSV file
- Widened output separators from 120 to 200 characters to accommodate longer URLs

