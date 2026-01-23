#!/usr/bin/env python3
"""
Script to test and compare response times between CellBase v5.8 and v6.7
"""

import requests
import json
import time
import argparse
import os
from tabulate import tabulate
from typing import List, Dict, Tuple

# Configuration
BASE_URL_V58 = "https://ws.zettagenomics.com/cellbase/webservices/rest/v5.8/hsapiens/genomic/variant"
BASE_URL_V67 = "https://ws.zettagenomics.com/cellbase/webservices/rest/v6.7/hsapiens/genomic/variant"

VARIANTS = [
    "1:68188386:C:T",
    "1:68188593:T:C",
    "1:68188680:T:A",
    "1:68188690:a:G",
    "1:68189093:T:G",
    "1:68189184:a:G",
    "1:68189946:T:C"
]

INCLUDE_PARAMS = [
    "variation",
    "populationFrequencies",
    "xrefs",
    "conservation",
    "functionalScore",
    "traitAssociation",
    "repeats",
    "cytoband",
    "pharmacogenomics",
    "polygenicScore",
    "genomicContext",
    "hgvs",
    "consequenceType",
    "geneImprinting",
    "geneFusions",
    "cancerHotSpots",
    "cancerGeneAssociation",
    "mirnaTargets",
    "geneConstraints",
    "drugInteraction",
    "geneDisease",
    "expression"
]


def fetch_response_time(base_url: str, variant: str, include_param: str, data_release: str) -> Tuple[int, bool, str, dict, float]:
    """
    Fetch the response time for a given query.

    Args:
        base_url: Base URL for the CellBase version
        variant: Variant string
        include_param: Include parameter value
        data_release: Data release version

    Returns:
        Tuple of (response_time_ms, success_flag, full_url, response_json, response_size_kb)
    """
    url = f"{base_url}/{variant}/annotation"
    params = {
        "dataRelease": data_release,
        "include": include_param
    }

    # Construct full URL with query parameters
    param_str = "&".join([f"{k}={v}" for k, v in params.items()])
    full_url = f"{url}?{param_str}"

    try:
        response = requests.get(url, params=params, timeout=30)
        response.raise_for_status()

        # Calculate response size in KB
        response_size_bytes = len(response.content)
        response_size_kb = response_size_bytes / 1024.0

        data = response.json()

        # Extract the responses[0].time value
        if "responses" in data and len(data["responses"]) > 0:
            response_time = data["responses"][0].get("time", -1)
            return response_time, True, full_url, data, response_size_kb
        else:
            print(f"Warning: No responses found for {variant} with include={include_param}")
            return -1, False, full_url, data, response_size_kb

    except requests.exceptions.RequestException as e:
        print(f"Error fetching {url}: {e}")
        return -1, False, full_url, {}, 0.0
    except (json.JSONDecodeError, KeyError) as e:
        print(f"Error parsing response for {url}: {e}")
        return -1, False, full_url, {}, 0.0


def sanitize_filename(text: str) -> str:
    """
    Convert text to a safe filename by replacing special characters.

    Args:
        text: Text to sanitize

    Returns:
        Safe filename string
    """
    # Replace colons and other special characters with underscores
    safe_text = text.replace(':', '_').replace('/', '_').replace('\\', '_')
    return safe_text


def save_query_json(query_folder: str, include_param: str, variant: str, cb_version: str, json_data: dict):
    """
    Save the JSON response from a query to a file.

    Args:
        query_folder: Folder path to save the JSON files
        include_param: Include parameter value
        variant: Variant string
        cb_version: CellBase version (v5.8 or v6.7)
        json_data: JSON response data to save
    """
    # Create safe filename: include_variant_cbversion.json
    safe_variant = sanitize_filename(variant)
    filename = f"{include_param}_{safe_variant}_{cb_version}.json"
    filepath = os.path.join(query_folder, filename)

    try:
        with open(filepath, 'w') as f:
            json.dump(json_data, f, indent=2)
    except Exception as e:
        print(f"Warning: Failed to save query JSON to {filepath}: {e}")


def run_tests(query_folder: str = None) -> List[Dict]:
    """
    Run all test combinations and collect results.

    Args:
        query_folder: Optional folder path to save individual query JSON responses

    Returns:
        List of result dictionaries
    """
    results = []
    total_tests = len(VARIANTS) * len(INCLUDE_PARAMS)
    current_test = 0

    print(f"Starting tests: {len(VARIANTS)} variants × {len(INCLUDE_PARAMS)} include params = {total_tests} tests per version")
    print("=" * 80)

    for include_param in INCLUDE_PARAMS:
        for variant in VARIANTS:
            current_test += 1
            print(f"[{current_test}/{total_tests}] Testing variant={variant}, include={include_param}")

            # Test v5.8
            time_v58, success_v58, url_v58, json_v58, size_v58 = fetch_response_time(BASE_URL_V58, variant, include_param, "8")
            if success_v58:
                print(f"  v5.8: {time_v58}ms, {size_v58:.2f}KB")
            else:
                print(f"  v5.8: FAILED")

            # Save JSON response if query_folder is provided
            if query_folder and json_v58:
                save_query_json(query_folder, include_param, variant, "v5.8", json_v58)

            # Small delay to avoid overwhelming the server
            time.sleep(0.1)

            # Test v6.7
            time_v67, success_v67, url_v67, json_v67, size_v67 = fetch_response_time(BASE_URL_V67, variant, include_param, "1")
            if success_v67:
                print(f"  v6.7: {time_v67}ms, {size_v67:.2f}KB")
            else:
                print(f"  v6.7: FAILED")

            # Save JSON response if query_folder is provided
            if query_folder and json_v67:
                save_query_json(query_folder, include_param, variant, "v6.7", json_v67)

            # Small delay between test pairs
            time.sleep(0.1)

            results.append({
                "include": include_param,
                "variant": variant,
                "url_v58": url_v58,
                "url_v67": url_v67,
                "time_v58": time_v58 if success_v58 else "ERROR",
                "time_v67": time_v67 if success_v67 else "ERROR",
                "size_v58": round(size_v58, 2) if success_v58 else "ERROR",
                "size_v67": round(size_v67, 2) if success_v67 else "ERROR"
            })

    return results


def display_results(results: List[Dict]):
    """
    Display results in a formatted table.

    Args:
        results: List of result dictionaries
    """
    # Prepare data for tabulate
    table_data = []
    for result in results:
        table_data.append([
            result["include"],
            result["variant"],
            result["time_v58"],
            result["time_v67"],
            result["size_v58"],
            result["size_v67"],
            result["url_v58"],
            result["url_v67"]
        ])

    headers = ["Include", "Variant", "Time v5.8 (ms)", "Time v6.7 (ms)", "Size v5.8 (KB)", "Size v6.7 (KB)", "URL v5.8", "URL v6.7"]

    print("\n" + "=" * 200)
    print("RESULTS SUMMARY")
    print("=" * 200)
    print(tabulate(table_data, headers=headers, tablefmt="grid"))

    # Calculate and display statistics
    print("\n" + "=" * 200)
    print("OVERALL STATISTICS")
    print("=" * 200)

    valid_v58 = [r["time_v58"] for r in results if isinstance(r["time_v58"], int) and r["time_v58"] >= 0]
    valid_v67 = [r["time_v67"] for r in results if isinstance(r["time_v67"], int) and r["time_v67"] >= 0]

    valid_size_v58 = [r["size_v58"] for r in results if isinstance(r["size_v58"], (int, float)) and r["size_v58"] >= 0]
    valid_size_v67 = [r["size_v67"] for r in results if isinstance(r["size_v67"], (int, float)) and r["size_v67"] >= 0]

    if valid_v58:
        print(f"v5.8 Time - Avg: {sum(valid_v58)/len(valid_v58):.2f}ms, Min: {min(valid_v58)}ms, Max: {max(valid_v58)}ms")
    if valid_v67:
        print(f"v6.7 Time - Avg: {sum(valid_v67)/len(valid_v67):.2f}ms, Min: {min(valid_v67)}ms, Max: {max(valid_v67)}ms")

    if valid_size_v58:
        print(f"v5.8 Size - Avg: {sum(valid_size_v58)/len(valid_size_v58):.2f}KB, Min: {min(valid_size_v58):.2f}KB, Max: {max(valid_size_v58):.2f}KB")
    if valid_size_v67:
        print(f"v6.7 Size - Avg: {sum(valid_size_v67)/len(valid_size_v67):.2f}KB, Min: {min(valid_size_v67):.2f}KB, Max: {max(valid_size_v67):.2f}KB")

    # Calculate per-include statistics
    print("\n" + "=" * 200)
    print("STATISTICS BY INCLUDE PARAMETER")
    print("=" * 200)

    # Group results by include parameter
    from collections import defaultdict
    include_stats = defaultdict(lambda: {'v58_time': [], 'v67_time': [], 'v58_size': [], 'v67_size': []})

    for result in results:
        include_param = result["include"]
        if isinstance(result["time_v58"], int) and result["time_v58"] >= 0:
            include_stats[include_param]['v58_time'].append(result["time_v58"])
        if isinstance(result["time_v67"], int) and result["time_v67"] >= 0:
            include_stats[include_param]['v67_time'].append(result["time_v67"])
        if isinstance(result["size_v58"], (int, float)) and result["size_v58"] >= 0:
            include_stats[include_param]['v58_size'].append(result["size_v58"])
        if isinstance(result["size_v67"], (int, float)) and result["size_v67"] >= 0:
            include_stats[include_param]['v67_size'].append(result["size_v67"])

    # Display stats for each include parameter
    stats_table = []
    for include_param in INCLUDE_PARAMS:
        if include_param in include_stats:
            v58_times = include_stats[include_param]['v58_time']
            v67_times = include_stats[include_param]['v67_time']
            v58_sizes = include_stats[include_param]['v58_size']
            v67_sizes = include_stats[include_param]['v67_size']

            v58_avg_time = sum(v58_times)/len(v58_times) if v58_times else 0
            v67_avg_time = sum(v67_times)/len(v67_times) if v67_times else 0
            v58_avg_size = sum(v58_sizes)/len(v58_sizes) if v58_sizes else 0
            v67_avg_size = sum(v67_sizes)/len(v67_sizes) if v67_sizes else 0

            stats_table.append([
                include_param,
                f"{v58_avg_time:.2f}" if v58_times else "N/A",
                f"{v67_avg_time:.2f}" if v67_times else "N/A",
                f"{v67_avg_time - v58_avg_time:+.2f}" if (v58_times and v67_times) else "N/A",
                f"{v58_avg_size:.2f}" if v58_sizes else "N/A",
                f"{v67_avg_size:.2f}" if v67_sizes else "N/A",
                f"{v67_avg_size - v58_avg_size:+.2f}" if (v58_sizes and v67_sizes) else "N/A"
            ])

    stats_headers = ["Include", "Avg Time v5.8 (ms)", "Avg Time v6.7 (ms)", "Time Diff (ms)", "Avg Size v5.8 (KB)", "Avg Size v6.7 (KB)", "Size Diff (KB)"]
    print(tabulate(stats_table, headers=stats_headers, tablefmt="grid"))


def save_results_to_json(results: List[Dict], filename: str):
    """
    Save results to a JSON file.

    Args:
        results: List of result dictionaries
        filename: Output filename
    """
    with open(filename, 'w') as jsonfile:
        json.dump(results, jsonfile, indent=2)

    print(f"\nResults saved to {filename}")


def load_results_from_json(filename: str) -> List[Dict]:
    """
    Load results from a JSON file.

    Args:
        filename: Input filename

    Returns:
        List of result dictionaries
    """
    try:
        with open(filename, 'r') as jsonfile:
            results = json.load(jsonfile)
        print(f"Results loaded from {filename}")
        return results
    except FileNotFoundError:
        print(f"Error: File '{filename}' not found.")
        return []
    except json.JSONDecodeError:
        print(f"Error: File '{filename}' is not valid JSON.")
        return []


def save_results_to_csv(results: List[Dict], filename: str = "cellbase_response_times.csv"):
    """
    Save results to a CSV file.

    Args:
        results: List of result dictionaries
        filename: Output filename
    """
    import csv

    with open(filename, 'w', newline='') as csvfile:
        fieldnames = ['include', 'variant', 'time_v58', 'time_v67', 'size_v58', 'size_v67', 'url_v58', 'url_v67']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

        writer.writeheader()
        for result in results:
            writer.writerow({
                'include': result['include'],
                'variant': result['variant'],
                'time_v58': result['time_v58'],
                'time_v67': result['time_v67'],
                'size_v58': result['size_v58'],
                'size_v67': result['size_v67'],
                'url_v58': result['url_v58'],
                'url_v67': result['url_v67']
            })

    print(f"\nResults also saved to CSV: {filename}")


def cmd_run(args):
    """Execute the run command: perform queries and save results."""
    print("CellBase Response Time Comparison Test")
    print("=" * 80)
    print(f"Testing {len(VARIANTS)} variants with {len(INCLUDE_PARAMS)} include parameters")
    print(f"Total queries: {len(VARIANTS) * len(INCLUDE_PARAMS) * 2}")
    print("=" * 80)
    print()

    # Create queries folder based on results file name
    # Remove .json extension if present and add _queries suffix
    base_name = args.results_file.replace('.json', '')
    query_folder = f"{base_name}_queries"

    # Create the folder if it doesn't exist
    try:
        os.makedirs(query_folder, exist_ok=True)
        print(f"Query JSON responses will be saved to: {query_folder}/")
        print()
    except Exception as e:
        print(f"Warning: Could not create query folder '{query_folder}': {e}")
        query_folder = None

    # Run tests
    results = run_tests(query_folder)

    # Display results
    display_results(results)

    # Save to JSON
    save_results_to_json(results, args.results_file)

    # Also save to CSV with same base name
    csv_filename = args.results_file.replace('.json', '.csv')
    save_results_to_csv(results, csv_filename)


def cmd_display(args):
    """Execute the display command: load and display results from file."""
    print("CellBase Response Time Comparison Test - Display Results")
    print("=" * 80)

    # Load results from file
    results = load_results_from_json(args.results_file)

    if not results:
        print("No results to display.")
        return

    print(f"Loaded {len(results)} results")
    print("=" * 80)
    print()

    # Display results
    display_results(results)


def main():
    """Main execution function with command-line argument parsing."""
    parser = argparse.ArgumentParser(
        description='CellBase Response Time Comparison Test',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Run tests and save results
  python test_response_time.py run --results-file results.json
  
  # Display previously saved results
  python test_response_time.py display --results-file results.json
        """
    )

    subparsers = parser.add_subparsers(dest='command', help='Command to execute')
    subparsers.required = True

    # Run command
    parser_run = subparsers.add_parser('run', help='Execute queries and save results')
    parser_run.add_argument('--results-file',
                           type=str,
                           default='cellbase_response_times.json',
                           help='JSON file to save results (default: cellbase_response_times.json)')
    parser_run.set_defaults(func=cmd_run)

    # Display command
    parser_display = subparsers.add_parser('display', help='Load and display results from file')
    parser_display.add_argument('--results-file',
                                type=str,
                                required=True,
                                help='JSON file containing results to display')
    parser_display.set_defaults(func=cmd_display)

    # Parse arguments and execute command
    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()

