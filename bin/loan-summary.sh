#!/usr/bin/env bash
# Summarizes a Lantern loan log. Run it from the repository root:
#   ./bin/loan-summary.sh                     summarize fixtures/loan-log.txt
#   ./bin/loan-summary.sh path/to/other.txt   summarize another log
# The exact output format is in docs/milestones/1-team-workflow.md.
set -euo pipefail

log=${1:-fixtures/loan-log.txt}

# Supplied: a missing log file is an error (message on standard error, exit status 2).
if [[ ! -f $log ]]; then
    echo "No such log file: $log" >&2
    exit 2
fi

# TODO LEARNER 1C: replace these two lines with the summary (use grep, grep -i, grep -v, pipes and wc -l).
echo "Loan log: $log"
echo "TODO LEARNER 1C: summary not written yet" >&2
exit 1
