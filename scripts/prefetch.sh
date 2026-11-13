#!/usr/bin/env bash
# Run once while you are online. It runs every Maven command this project uses, so Maven downloads all the
# libraries and build plugins they need. Afterwards the project also works without a network connection:
#   export MAVEN_ARGS=-o      # Maven offline mode: use only what is already downloaded
# Test failures (for example unfinished milestones) are expected here and do not stop it; download or compile
# errors do, and the last lines of the log are shown.
set -euo pipefail

unset MAVEN_ARGS   # downloading needs Maven online, even if offline mode is set in this terminal
root=$(cd "$(dirname "$0")/.." && pwd -P)
cd "$root"
log=$(mktemp "${TMPDIR:-/tmp}/prefetch.XXXXXX")
trap 'rm -f "$log"' EXIT

step() {
    if ! mvn -q -Dmaven.test.failure.ignore=true "$@" >>"$log" 2>&1; then
        echo "Download step failed: mvn $*" >&2
        tail -n 30 "$log" >&2
        exit 1
    fi
}

echo "Downloading everything the project needs (the first time can take a few minutes)..."
step clean verify
step -Pcompletion test
step -Pcoverage verify
step compile exec:java -Dexec.args=help
echo "Done. Everything is downloaded; without a network connection, run: export MAVEN_ARGS=-o"
