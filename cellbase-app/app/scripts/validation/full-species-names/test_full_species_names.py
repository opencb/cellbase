#!/usr/bin/env python3
"""
CellBase API Test Script - Full Species Names Validation

This script tests various CellBase API endpoints using "Homo Sapiens" (with space)
as the species name to verify that species name normalization works correctly.

The fix ensures that species names in various formats (scientific name, common name, ID)
are properly normalized before looking up default data releases.

NOTE: These tests are temporary validation scripts. Once the fix is merged,
these tests will be implemented as JUnit integration tests in the CellBase test suite.

Usage:
    pytho3n test_full_species_names.py [--base-url URL] [--species SPECIES] [--version VERSION] [--verbose]
    python3 test_full_species_names.py --base-url http://localhost:8080/cellbase-6.7.0-SNAPSHOT/webservices --species "Homo Sapiens" --version v6.7

Author: CellBase Team
Date: 2026-02-09
Issue: TASK-8151
"""

import argparse
import json
import sys
import urllib.parse
from typing import List
import urllib.request
import urllib.error


class CellBaseAPITester:
    """Test CellBase API endpoints with full species names."""

    def __init__(self, base_url: str, verbose: bool = False):
        self.base_url = base_url.rstrip('/')
        self.verbose = verbose
        self.results = []
        self.passed = 0
        self.failed = 0

    def test_endpoint(self, name: str, url: str, expected_fields: List[str] = None) -> bool:
        """
        Test a single CellBase API endpoint.

        Args:
            name: Descriptive name for the test
            url: Full URL to test
            expected_fields: Optional list of fields expected in the response

        Returns:
            True if test passed, False otherwise
        """
        if self.verbose:
            print(f"\n{'='*80}")
            print(f"Testing: {name}")
            print(f"URL: {url}")

        try:
            with urllib.request.urlopen(url, timeout=30) as response:
                status_code = response.status
                data = json.loads(response.read().decode('utf-8'))

                # Check HTTP status
                if status_code != 200:
                    self._record_failure(name, url, f"HTTP {status_code}")
                    return False

                # Check for API errors in response
                if 'events' in data:
                    for event in data.get('events', []):
                        if event.get('type') == 'ERROR':
                            self._record_failure(name, url, f"API Error: {event.get('message', 'Unknown error')}")
                            return False

                # Check if response has data
                if 'responses' not in data or not data['responses']:
                    self._record_failure(name, url, "No responses in API result")
                    return False

                # Check expected fields if provided
                if expected_fields and data['responses']:
                    first_response = data['responses'][0]
                    if 'results' in first_response and first_response['results']:
                        first_result = first_response['results'][0]
                        missing_fields = [f for f in expected_fields if f not in first_result]
                        if missing_fields:
                            self._record_failure(
                                name, url,
                                f"Missing expected fields: {', '.join(missing_fields)}"
                            )
                            return False

                # Check data release is set
                if 'dataRelease' not in data or data['dataRelease'] is None:
                    self._record_failure(name, url, "No dataRelease in response")
                    return False

                self._record_success(name, url, data.get('dataRelease'))
                return True

        except urllib.error.HTTPError as e:
            error_msg = f"HTTP {e.code}: {e.reason}"
            try:
                error_data = json.loads(e.read().decode('utf-8'))
                if 'events' in error_data:
                    for event in error_data.get('events', []):
                        if event.get('type') == 'ERROR':
                            error_msg += f" - {event.get('message', '')}"
            except:
                pass
            self._record_failure(name, url, error_msg)
            return False

        except urllib.error.URLError as e:
            self._record_failure(name, url, f"Connection error: {e.reason}")
            return False

        except json.JSONDecodeError as e:
            self._record_failure(name, url, f"Invalid JSON response: {e}")
            return False

        except Exception as e:
            self._record_failure(name, url, f"Unexpected error: {str(e)}")
            return False

    def _record_success(self, name: str, url: str, data_release: int):
        """Record a successful test."""
        self.passed += 1
        result = {
            'name': name,
            'url': url,
            'status': 'PASS',
            'dataRelease': data_release
        }
        self.results.append(result)

        if self.verbose:
            print(f"✓ PASSED (dataRelease: {data_release})")
        else:
            print(f"✓ {name}")

    def _record_failure(self, name: str, url: str, error: str):
        """Record a failed test."""
        self.failed += 1
        result = {
            'name': name,
            'url': url,
            'status': 'FAIL',
            'error': error
        }
        self.results.append(result)

        print(f"✗ {name}")
        print(f"  Error: {error}")
        if self.verbose:
            print(f"  URL: {url}")

    def run_all_tests(self, version: str = "v6.7", species_name: str = "Homo Sapiens"):
        """Run all validation tests."""
        print(f"\n{'='*80}")
        print(f"CellBase API Validation - Full Species Names")
        print(f"{'='*80}")
        print(f"Base URL: {self.base_url}")
        print(f"Version: {version}")
        print(f"Species: {species_name}")
        print(f"{'='*80}\n")

        # URL-encode species name
        species = urllib.parse.quote(species_name)

        # Gene Feature Tests
        print("\n📚 Gene Feature Tests")
        print("-" * 80)

        self.test_endpoint(
            "Gene info - BRCA2",
            f"{self.base_url}/rest/{version}/{species}/feature/gene/BRCA2/info?include=name",
            expected_fields=['name']
        )

        self.test_endpoint(
            "Gene transcripts - BRCA2",
            f"{self.base_url}/rest/{version}/{species}/feature/gene/BRCA2/transcript?include=id,name",
            expected_fields=['id']
        )

        self.test_endpoint(
            "Gene protein - BRCA2",
            f"{self.base_url}/rest/{version}/{species}/feature/gene/BRCA2/protein?include=accession",
            expected_fields=['accession']
        )

        self.test_endpoint(
            "Gene sequence - BRCA2",
            f"{self.base_url}/rest/{version}/{species}/feature/gene/BRCA2/sequence"
        )

        self.test_endpoint(
            "Gene search",
            f"{self.base_url}/rest/{version}/{species}/feature/gene/search?name=BRCA2&include=id,name",
            expected_fields=['id']
        )

        # Protein Feature Tests
        print("\n🧬 Protein Feature Tests")
        print("-" * 80)

        self.test_endpoint(
            "Protein info - P51587",
            f"{self.base_url}/rest/{version}/{species}/feature/protein/P51587/info?include=accession,name",
            expected_fields=['accession']
        )

        self.test_endpoint(
            "Protein sequence - P51587",
            f"{self.base_url}/rest/{version}/{species}/feature/protein/P51587/sequence"
        )

        # Transcript Feature Tests
        print("\n📝 Transcript Feature Tests")
        print("-" * 80)

        self.test_endpoint(
            "Transcript info - ENST00000380152",
            f"{self.base_url}/rest/{version}/{species}/feature/transcript/ENST00000380152/info?include=id,name",
            expected_fields=['id']
        )

        self.test_endpoint(
            "Transcript gene - ENST00000380152",
            f"{self.base_url}/rest/{version}/{species}/feature/transcript/ENST00000380152/gene?include=id,name",
            expected_fields=['id']
        )

        self.test_endpoint(
            "Transcript sequence - ENST00000380152",
            f"{self.base_url}/rest/{version}/{species}/feature/transcript/ENST00000380152/sequence"
        )

        # Variant/Genomic Tests
        print("\n🧪 Variant/Genomic Tests")
        print("-" * 80)

        self.test_endpoint(
            "Variant annotation - 13:32315508:G:T",
            f"{self.base_url}/rest/{version}/{species}/genomic/variant/13:32315508:G:T/annotation?include=consequenceTypes"
        )

        self.test_endpoint(
            "Region gene query",
            f"{self.base_url}/rest/{version}/{species}/genomic/region/13:32315000-32316000/gene?include=id,name",
            expected_fields=['id']
        )

    def print_summary(self):
        """Print test summary."""
        print(f"\n{'='*80}")
        print("Test Summary")
        print(f"{'='*80}")
        print(f"Total tests: {self.passed + self.failed}")
        print(f"✓ Passed: {self.passed}")
        print(f"✗ Failed: {self.failed}")
        print(f"Success rate: {(self.passed / (self.passed + self.failed) * 100):.1f}%")
        print(f"{'='*80}\n")

        if self.failed > 0:
            print("\n❌ Failed Tests:")
            print("-" * 80)
            for result in self.results:
                if result['status'] == 'FAIL':
                    print(f"  • {result['name']}")
                    print(f"    Error: {result['error']}")
                    print(f"    URL: {result['url']}\n")

        return self.failed == 0


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description='Test CellBase API with full species names (e.g., "Homo Sapiens")',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Test against production server with default species (Homo Sapiens)
  python test_full_species_names.py

  # Test with a specific species
  python test_full_species_names.py --species "Mus musculus"

  # Test against custom server and version
  python test_full_species_names.py --base-url http://localhost:8080/cellbase --version v5.8

  # Test with species ID format
  python test_full_species_names.py --species hsapiens

  # Verbose output
  python test_full_species_names.py --verbose --species "Homo Sapiens"
        """
    )
    parser.add_argument(
        '--base-url',
        default='https://ws.zettagenomics.com/cellbase/webservices',
        help='Base URL of CellBase server (default: https://ws.zettagenomics.com/cellbase/webservices)'
    )
    parser.add_argument(
        '--species',
        default='Homo Sapiens',
        help='Species name to test (default: "Homo Sapiens"). Can use scientific name, common name, or species ID'
    )
    parser.add_argument(
        '--version',
        default='v6.7',
        help='CellBase API version (default: v6.7)'
    )
    parser.add_argument(
        '--verbose',
        action='store_true',
        help='Enable verbose output'
    )

    args = parser.parse_args()

    # Create tester and run tests
    tester = CellBaseAPITester(args.base_url, args.verbose)
    tester.run_all_tests(args.version, args.species)
    success = tester.print_summary()

    # Exit with appropriate code
    sys.exit(0 if success else 1)


if __name__ == '__main__':
    main()
