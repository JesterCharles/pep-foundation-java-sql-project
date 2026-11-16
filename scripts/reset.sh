#!/usr/bin/env bash
# Recreates the local database in data/ with the synthetic sample data.
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q compile exec:java -Dexec.args=reset
