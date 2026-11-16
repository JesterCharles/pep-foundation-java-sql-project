#!/usr/bin/env bash
# Checks the cumulative milestones listed in docs/milestones/manifest.tsv.
# Prints PASS or FAIL with test counts per milestone; details go to target/milestone-check/ and
# target/surefire-reports/.
set -euo pipefail

root=$(cd "$(dirname "$0")/.." && pwd -P)
cd "$root"
manifest=docs/milestones/manifest.tsv
reports=target/surefire-reports
test_package=com.pep.toolshare
logs=target/milestone-check
work=$(mktemp -d "${TMPDIR:-/tmp}/milestone-check.XXXXXX")
trap 'rm -rf "$work"' EXIT

usage() {
    cat >&2 <<'USAGE'
Usage:
  ./scripts/verify-milestones.sh <N>        check milestones 1 through N (1-6) in this repository
  ./scripts/verify-milestones.sh progress   show every milestone's PASS/FAIL; fails only if the health build fails
  ./scripts/verify-milestones.sh starter    course maintainers: confirm the untouched starter snapshot
USAGE
    exit 2
}

rows() { awk -F'\t' '$1 ~ /^[0-9]+$/' "$manifest"; }
field() { rows | awk -F'\t' -v m="$1" -v c="$2" '$1 == m { print $c }'; }
attr() { printf '%s\n' "$1" | sed -n "s/.* $2=\"\([0-9]*\)\".*/\1/p"; }
word() { if [[ $1 -eq 1 ]]; then echo PASS; else echo FAIL; fi; }

milestone_count=$(rows | wc -l | tr -d ' ')

has_marker() {
    # has_marker <file> <id>: the file still contains the TODO LEARNER marker <id>
    grep -Eq "TODO LEARNER $2([^0-9A-Za-z]|\$)" "$1"
}

markers_present() {
    # every TODO id of milestone $1 is still present in one of its files
    local m=$1 id file found
    for id in $(field "$m" 6 | tr ',' ' '); do
        found=0
        for file in $(field "$m" 5 | tr ',' ' '); do
            if [[ -f $file ]] && has_marker "$file" "$id"; then found=1; fi
        done
        [[ $found -eq 1 ]] || return 1
    done
}

markers_left() {
    # lists TODO LEARNER markers still present in the learner files of milestones 1..$1
    local m file
    for ((m = 1; m <= $1; m++)); do
        for file in $(field "$m" 5 | tr ',' ' '); do
            [[ -f $file ]] && grep -Hn "TODO LEARNER" "$file" || true
        done
    done | sort -u
}

passed=()
totals=()
health_ok=0
final_ok=0
final_ran=0
stopped=0

check() {
    # check <final-run:0|1> <last milestone for the final run>
    local final=$1 upto=$2 m class report suite t f e s mt mf me ms ok missing slow
    set +e
    mvn -q clean verify >"$work/health.log" 2>&1 && health_ok=1
    mvn -q -Pcompletion test >"$work/completion.log" 2>&1
    set -e
    # Maven stops the whole test run when it goes over its time limit; the checks it had not reached have no report.
    if grep -q 'There was a timeout in the fork' "$work/health.log" "$work/completion.log"; then stopped=1; fi
    for m in $(rows | cut -f1); do
        ok=1 mt=0 mf=0 me=0 ms=0 missing=0
        for class in $(field "$m" 7 | tr ',' ' '); do
            report="$reports/TEST-$test_package.$class.xml"
            if [[ ! -f $report ]]; then ok=0; missing=1; continue; fi
            suite=$(grep -o '<testsuite [^>]*>' "$report" | head -1)
            t=$(attr "$suite" tests) f=$(attr "$suite" failures) e=$(attr "$suite" errors) s=$(attr "$suite" skipped)
            mt=$((mt + ${t:-0})) mf=$((mf + ${f:-0})) me=$((me + ${e:-0})) ms=$((ms + ${s:-0}))
        done
        [[ $mt -gt 0 && $mf -eq 0 && $me -eq 0 && $ms -eq 0 ]] || ok=0
        passed[m]=$ok
        totals[m]="tests $mt, failures $mf, errors $me, skipped $ms"
        [[ $stopped -eq 1 && $missing -eq 1 ]] && passed[m]=2
    done
    local all_passed=1
    for m in $(rows | cut -f1); do
        [[ ${passed[m]} -eq 1 ]] || all_passed=0
    done
    if [[ $final -eq 1 && $all_passed -eq 1 ]]; then
        final_ran=1
        # The final run uses its own database under target/, so your data/ folder is left alone.
        local url="jdbc:h2:file:./target/final-run/lantern;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        set +e
        LANTERN_DB_URL=$url mvn -q compile exec:java -Dexec.args=reset >"$work/reset.log" 2>&1 &&
            LANTERN_DB_URL=$url mvn -q exec:java -Dexec.args="report 2026-10-15" >"$work/report.log" 2>&1 &&
            grep -qx 'Categories: 5' "$work/report.log" &&
            grep -qx 'Overdue loans: 3' "$work/report.log" &&
            grep -qx 'Top tool per category: 4' "$work/report.log" &&
            grep -qx 'Members without loans: 1' "$work/report.log" &&
            [[ -z $(markers_left "$upto") ]] && final_ok=1
        set -e
    fi
    rm -rf "$logs"
    mkdir -p "$logs"
    cp "$work"/*.log "$logs/"
    echo "health (mvn -q clean verify): $(word "$health_ok")"
    for m in $(rows | cut -f1); do
        if [[ ${passed[m]} -eq 2 ]]; then
            echo "milestone $m $(field "$m" 2): NOT RUN (the test run was stopped before all of these checks ran)"
        else
            echo "milestone $m $(field "$m" 2): $(word "${passed[m]}") (${totals[m]})"
        fi
    done
    if [[ $stopped -eq 1 ]]; then
        echo "The test run was stopped at its time limit because some code never finished: look for a loop that" \
            "never ends in your latest change."
        slow=$(grep -l 'timed out after' "$reports"/*.txt 2>/dev/null | sed 's|.*/||; s|\.txt$||' | tr '\n' ' ') || true
        [[ -z $slow ]] || echo "Checks with a test that ran too long: $slow"
    fi
}

learner_mode() {
    local target=$1 m failed=0 final=0 left
    [[ $target =~ ^[0-9]+$ ]] && [[ $target -ge 1 && $target -le $milestone_count ]] || usage
    [[ $target -eq $milestone_count ]] && final=1
    check "$final" "$target"
    [[ $health_ok -eq 1 ]] || failed=1
    for ((m = 1; m <= target; m++)); do
        [[ ${passed[m]} -eq 1 ]] || failed=1
    done
    if [[ $final -eq 1 ]]; then
        if [[ $final_ran -eq 1 ]]; then
            echo "final run (reset, report 2026-10-15, no TODO LEARNER markers left): $(word "$final_ok")"
        else
            echo "final run (reset, report 2026-10-15, no TODO LEARNER markers left): NOT RUN (every milestone check must pass first)"
        fi
        [[ $final_ok -eq 1 ]] || failed=1
        left=$(markers_left "$target")
        [[ -z $left ]] || printf 'Markers still present:\n%s\n' "$left"
    fi
    if [[ $failed -ne 0 ]]; then
        echo "Milestones 1-$target are not complete yet. Fix the earliest FAIL first; logs: $logs/ and $reports/." >&2
        exit 1
    fi
    echo "Milestones 1-$target complete."
}

progress_mode() {
    local m done_upto=0
    check 0 0
    for ((m = 1; m <= milestone_count; m++)); do
        [[ ${passed[m]} -eq 1 ]] || break
        done_upto=$m
    done
    if [[ $health_ok -ne 1 ]]; then
        echo "The health build fails. Fix it first; log: $logs/health.log" >&2
        exit 1
    fi
    if [[ $done_upto -eq 0 ]]; then
        echo "Progress: no milestone complete yet."
    else
        echo "Progress: milestones 1-$done_upto pass. Run ./scripts/verify-milestones.sh $done_upto for the full check."
    fi
}

starter_mode() {
    local m failed=0
    check 0 0
    [[ $health_ok -eq 1 ]] || failed=1
    if [[ -x bin/loan-summary.sh ]] ||
        { git rev-parse --is-inside-work-tree >/dev/null 2>&1 && git ls-files -s -- bin/loan-summary.sh | grep -q '^100755'; }; then
        echo "Expected bin/loan-summary.sh to start non-executable; making it executable is a learner task." >&2
        failed=1
    fi
    local assistant='(^|/)(AGENTS\.md|CLAUDE\.md|GEMINI\.md|\.claude/|\.cursor/|\.aider)'
    local files
    if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        files=$(git ls-files)
    else
        files=$(find . -path ./target -prune -o -print | sed 's|^\./||')
    fi
    if grep -Eq "$assistant" <<<"$files"; then
        echo "Expected no assistant-instruction file in the starter snapshot." >&2
        failed=1
    fi
    for m in $(rows | cut -f1); do
        if [[ ${passed[m]} -eq 1 ]]; then
            echo "Expected milestone $m to fail in the untouched starter." >&2
            failed=1
        fi
        if ! markers_present "$m"; then
            echo "Expected every TODO LEARNER marker of milestone $m to be present." >&2
            failed=1
        fi
    done
    [[ $failed -eq 0 ]] || exit 1
    echo "Confirmed the untouched starter: health passes and all $milestone_count milestones fail as expected."
}

[[ $# -eq 1 ]] || usage
case $1 in
    starter) starter_mode ;;
    progress) progress_mode ;;
    *) learner_mode "$1" ;;
esac
