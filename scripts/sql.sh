#!/usr/bin/env bash
# Opens an interactive SQL shell on the local database (the embedded H2 shell; type "exit" to leave).
# Run ./scripts/reset.sh first if the database does not exist yet. Stop the API server first: the file can only be
# opened by one program at a time.
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q compile exec:java -Dexec.args=sql
