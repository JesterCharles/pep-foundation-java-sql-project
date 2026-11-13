#!/usr/bin/env bash
set -euo pipefail

mvn -q exec:java -Dexec.args=reset
