#!/usr/bin/env python3
"""
Script to get response time (ms) and returned size (KB) for a CellBase variant query.

Usage:
  python get_response_metrics.py --base-url https://ws.zettagenomics.com/cellbase/webservices/rest/ \
                                 --version v6.7 \
                                 --variant 1:68188386:C:T \
                                 --data-release 1 \
                                 --include variation
"""

import requests
import json
import argparse
import sys
from typing import Tuple, Optional


def get_response_metrics(base_url: str, version: str, variant: str,
                         data_release: str, include: str = None, exclude: str = None) -> Tuple[Optional[int], Optional[float], bool, str, dict]:
    """
    Get the response time and size for a CellBase variant query.

    Args:
        base_url: Base URL for CellBase (e.g., https://ws.zettagenomics.com/cellbase/webservices/rest/)
        version: API version (e.g., v6.7)
        variant: Variant string (e.g., 1:68188386:C:T)
        data_release: Data release version (e.g., 1)
        include: Include parameter value (e.g., variation, populationFrequencies, etc.)
        exclude: Exclude parameter value (e.g., studies, transcriptFlags, etc.)

    Returns:
        Tuple of (response_time_ms, response_size_kb, success_flag, full_url, response_json)
    """
    # Clean up base_url - remove trailing slash if present
    base_url = base_url.rstrip('/')

    # Construct the full URL
    url = f"{base_url}/{version}/hsapiens/genomic/variant/{variant}/annotation"

    params = {
        "dataRelease": data_release
    }

    if include:
        params["include"] = include

    if exclude:
        params["exclude"] = exclude

    # Construct full URL with query parameters for display
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
            response_time = data["responses"][0].get("time", None)
            if response_time is not None:
                return response_time, response_size_kb, True, full_url, data
            else:
                print(f"Warning: No 'time' field found in response", file=sys.stderr)
                return None, response_size_kb, False, full_url, data
        else:
            print(f"Warning: No responses found in the JSON response", file=sys.stderr)
            return None, response_size_kb, False, full_url, data

    except requests.exceptions.RequestException as e:
        print(f"Error fetching data: {e}", file=sys.stderr)
        return None, None, False, full_url, {}
    except (json.JSONDecodeError, KeyError) as e:
        print(f"Error parsing response: {e}", file=sys.stderr)
        return None, None, False, full_url, {}


def main():
    """Main execution function with command-line argument parsing."""
    parser = argparse.ArgumentParser(
        description='Get CellBase variant query response time (ms) and size (KB)',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Basic query with include
  python get_response_metrics.py \\
      --base-url https://ws.zettagenomics.com/cellbase/webservices/rest/ \\
      --version v6.7 \\
      --variant 1:68188386:C:T \\
      --data-release 1 \\
      --include variation

  # Query with exclude parameter (mutually exclusive with --include)
  python get_response_metrics.py \\
      --base-url https://ws.zettagenomics.com/cellbase/webservices/rest/ \\
      --version v6.7 \\
      --variant 1:68188386:C:T \\
      --data-release 1 \\
      --exclude studies

  # Query and save JSON response to file
  python get_response_metrics.py \\
      --base-url https://ws.zettagenomics.com/cellbase/webservices/rest/ \\
      --version v5.8 \\
      --variant 1:68188593:T:C \\
      --data-release 8 \\
      --include populationFrequencies \\
      --results-file response.json

Note: --include and --exclude are mutually exclusive. Only one can be provided.

Available include parameters:
  - variation
  - populationFrequencies
  - xrefs
  - conservation
  - functionalScore
  - traitAssociation
  - repeats
  - cytoband
  - pharmacogenomics
  - polygenicScore
  - genomicContext
  - hgvs
  - consequenceType
  - geneImprinting
  - geneFusions
  - cancerHotSpots
  - cancerGeneAssociation
  - mirnaTargets
  - geneConstraints
  - drugInteraction
  - geneDisease
  - expression
        """
    )

    parser.add_argument('--base-url',
                        type=str,
                        required=True,
                        help='Base URL for CellBase (e.g., https://ws.zettagenomics.com/cellbase/webservices/rest/)')

    parser.add_argument('--version',
                        type=str,
                        required=True,
                        help='API version (e.g., v6.7, v5.8)')

    parser.add_argument('--variant',
                        type=str,
                        required=True,
                        help='Variant string (e.g., 1:68188386:C:T)')

    parser.add_argument('--data-release',
                        type=str,
                        required=True,
                        help='Data release version (e.g., 1, 8)')

    # Create mutually exclusive group for include and exclude
    filter_group = parser.add_mutually_exclusive_group(required=False)
    filter_group.add_argument('--include',
                              type=str,
                              help='Include parameter (e.g., variation, populationFrequencies, consequenceType)')

    filter_group.add_argument('--exclude',
                              type=str,
                              help='Exclude parameter (e.g., studies, transcriptFlags)')

    parser.add_argument('--results-file',
                        type=str,
                        required=False,
                        help='Optional JSON file path to save the query response')

    args = parser.parse_args()

    # Get the metrics
    response_time, response_size, success, full_url, response_data = get_response_metrics(
        args.base_url,
        args.version,
        args.variant,
        args.data_release,
        args.include,
        args.exclude
    )

    # Save JSON response to file if requested
    if args.results_file and response_data:
        try:
            with open(args.results_file, 'w') as f:
                json.dump(response_data, f, indent=2)
            print(f"Query response saved to: {args.results_file}\n")
        except Exception as e:
            print(f"Warning: Failed to save response to {args.results_file}: {e}\n", file=sys.stderr)

    # Human-readable output format
    print("=" * 80)
    print("CellBase Query Response Metrics")
    print("=" * 80)
    print(f"URL:          {full_url}")
    print(f"Variant:      {args.variant}")
    print(f"Version:      {args.version}")
    print(f"Data Release: {args.data_release}")
    if args.include:
        print(f"Include:      {args.include}")
    if args.exclude:
        print(f"Exclude:      {args.exclude}")
    print("-" * 80)

    if success and response_time is not None:
        print(f"Response Time: {response_time} ms")
        print(f"Response Size: {response_size:.2f} KB")
        print(f"Status:        SUCCESS")
        sys.exit(0)
    else:
        if response_size is not None:
            print(f"Response Size: {response_size:.2f} KB")
        print(f"Status:        FAILED")
        sys.exit(1)


if __name__ == "__main__":
    main()

