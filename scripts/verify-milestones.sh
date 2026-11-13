#!/usr/bin/env bash
# Checks the cumulative milestone contracts listed in docs/milestones/manifest.tsv.
# Output is aggregate only: pass/fail per milestone and test counts, never source code.
set -euo pipefail

root=$(cd "$(dirname "$0")/.." && pwd -P)
manifest="$root/docs/milestones/manifest.tsv"
reports_subdir=target/surefire-reports
test_package=com.pep.toolshare

usage() {
    cat >&2 <<'EOF'
Usage:
  ./scripts/verify-milestones.sh <N>           check milestones 1 through N (1-6) in this working copy
  ./scripts/verify-milestones.sh starter       confirm the untouched starter: health passes, every milestone fails
  ./scripts/verify-milestones.sh states <dir>  maintainers: rebuild every milestone state from this snapshot plus
                                               solved overlays kept outside version control, and check each state
EOF
    exit 2
}

rows() {
    awk -F'\t' '$1 ~ /^[0-9]+$/' "$manifest"
}

field() {
    rows | awk -F'\t' -v m="$1" -v c="$2" '$1 == m { print $c }'
}

milestone_count=$(rows | wc -l | tr -d ' ')
if [[ $milestone_count -lt 1 ]]; then
    echo "No milestones found in $manifest." >&2
    exit 1
fi

attr() {
    # attr <testsuite-tag> <name>
    printf '%s\n' "$1" | sed -n "s/.* $2=\"\([0-9]*\)\".*/\1/p"
}

# Results of the last check_tree call, indexed by milestone number.
passed=()
totals=()
health_ok=0
final_run_ok=0
all_tests=0
all_failures=0
all_errors=0

run_logged() {
    # run_logged <log> <command...>; returns the command status without stopping the script.
    local log=$1
    shift
    set +e
    "$@" >"$log" 2>&1
    local status=$?
    set -e
    return $status
}

markers_present() {
    # markers_present <dir> <milestone>: succeeds when every TODO marker of the milestone is still present.
    local dir=$1 m=$2 file id ids found
    ids=$(field "$m" 6)
    for id in ${ids//,/ }; do
        found=0
        for file in $(field "$m" 5 | tr ',' ' '); do
            if grep -Fq "TODO LEARNER $id:" "$dir/$file"; then
                found=1
            fi
        done
        [[ $found -eq 1 ]] || return 1
    done
    return 0
}

any_marker_left() {
    local dir=$1 m file
    for m in $(rows | cut -f1); do
        for file in $(field "$m" 5 | tr ',' ' '); do
            if grep -Fq "TODO LEARNER" "$dir/$file"; then
                return 0
            fi
        done
    done
    return 1
}

check_tree() {
    # check_tree <project-dir> <log-dir> <run-final-check:0|1>
    local dir=$1 logs=$2 final=$3 m class report suite t f e s mt mf me ms ok
    passed=()
    totals=()
    health_ok=0
    final_run_ok=0
    all_tests=0
    all_failures=0
    all_errors=0
    mkdir -p "$logs"

    if (cd "$dir" && run_logged "$logs/health.log" mvn -q clean verify); then
        health_ok=1
    fi
    (cd "$dir" && run_logged "$logs/completion.log" mvn -q -Pcompletion test) || true

    for m in $(rows | cut -f1); do
        ok=1
        mt=0 mf=0 me=0 ms=0
        for class in $(field "$m" 7 | tr ',' ' '); do
            report="$dir/$reports_subdir/TEST-$test_package.$class.xml"
            if [[ ! -f $report ]]; then
                ok=0
                continue
            fi
            suite=$(grep -o '<testsuite [^>]*>' "$report" | head -1)
            t=$(attr "$suite" tests)
            f=$(attr "$suite" failures)
            e=$(attr "$suite" errors)
            s=$(attr "$suite" skipped)
            mt=$((mt + ${t:-0}))
            mf=$((mf + ${f:-0}))
            me=$((me + ${e:-0}))
            ms=$((ms + ${s:-0}))
        done
        if [[ $mt -eq 0 || $mf -ne 0 || $me -ne 0 || $ms -ne 0 ]]; then
            ok=0
        fi
        passed[m]=$ok
        totals[m]="tests $mt, failures $mf, errors $me, skipped $ms"
        all_tests=$((all_tests + mt))
        all_failures=$((all_failures + mf))
        all_errors=$((all_errors + me))
    done

    if [[ $final -eq 1 ]]; then
        if (cd "$dir" && run_logged "$logs/reset.log" mvn -q compile exec:java -Dexec.args=reset) &&
            (cd "$dir" && run_logged "$logs/run.log" mvn -q exec:java) &&
            grep -qx 'Available tools: 3' "$logs/run.log" &&
            grep -qx 'Category report rows: 3' "$logs/run.log" &&
            ! any_marker_left "$dir"; then
            final_run_ok=1
        fi
    fi
}

word() {
    if [[ $1 -eq 1 ]]; then echo PASS; else echo FAIL; fi
}

print_results() {
    local m
    echo "health (mvn -q clean verify): $(word "$health_ok")"
    for m in $(rows | cut -f1); do
        echo "milestone $m $(field "$m" 2): $(word "${passed[m]}") (${totals[m]})"
    done
}

learner_mode() {
    local target=$1 m failed=0 final=0 logs
    if ! [[ $target =~ ^[0-9]+$ ]] || [[ $target -lt 1 || $target -gt $milestone_count ]]; then
        usage
    fi
    [[ $target -eq $milestone_count ]] && final=1
    logs=$(mktemp -d "${TMPDIR:-/tmp}/milestone-check.XXXXXX")
    check_tree "$root" "$logs" "$final"
    mkdir -p "$root/target/milestone-check"
    cp "$logs"/*.log "$root/target/milestone-check/"
    rm -rf "$logs"

    print_results
    [[ $health_ok -eq 1 ]] || failed=1
    for ((m = 1; m <= target; m++)); do
        [[ ${passed[m]} -eq 1 ]] || failed=1
    done
    if [[ $final -eq 1 ]]; then
        echo "final run (reset, report, no TODO markers left): $(word "$final_run_ok")"
        [[ $final_run_ok -eq 1 ]] || failed=1
    fi
    if [[ $failed -ne 0 ]]; then
        echo "Milestones 1-$target are not complete yet. Logs: target/milestone-check/ and $reports_subdir/." >&2
        exit 1
    fi
    echo "Milestones 1-$target complete."
}

starter_mode() {
    local m failed=0 logs
    logs=$(mktemp -d "${TMPDIR:-/tmp}/milestone-check.XXXXXX")
    check_tree "$root" "$logs" 0
    mkdir -p "$root/target/milestone-check"
    cp "$logs"/*.log "$root/target/milestone-check/"
    rm -rf "$logs"

    print_results
    [[ $health_ok -eq 1 ]] || failed=1
    for m in $(rows | cut -f1); do
        if [[ ${passed[m]} -ne 0 ]]; then
            echo "Expected milestone $m to fail in the untouched starter." >&2
            failed=1
        fi
        if ! markers_present "$root" "$m"; then
            echo "Expected every TODO LEARNER marker of milestone $m to be present." >&2
            failed=1
        fi
    done
    if [[ $failed -ne 0 ]]; then
        exit 1
    fi
    echo "Confirmed the untouched starter: health passes and all $milestone_count milestones fail as expected."
}

validate_overlays() {
    local dir=$1 m k path allowed tracked
    for ((m = 1; m <= milestone_count; m++)); do
        if [[ ! -d $dir/milestone-$m ]]; then
            echo "Missing overlay directory: milestone-$m" >&2
            exit 2
        fi
        allowed=""
        for ((k = 1; k <= m; k++)); do
            allowed="$allowed,$(field "$k" 5),"
        done
        while IFS= read -r path; do
            path=${path#./}
            if [[ $allowed != *",$path,"* ]]; then
                echo "Overlay milestone-$m may only replace learner-owned files of milestones 1-$m: $path" >&2
                exit 2
            fi
        done < <(cd "$dir/milestone-$m" && find . -type f ! -name .DS_Store)
    done
    if git -C "$root" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        tracked=$(git -C "$root" ls-files -- "$dir" 2>/dev/null || true)
        if [[ -n $tracked ]]; then
            echo "Solved overlays must stay out of version control; tracked overlay files were found." >&2
            exit 2
        fi
    fi
}

states_mode() {
    local overlays=$1 work n m k state failed=0 ok expected
    [[ -d $overlays ]] || usage
    overlays=$(cd "$overlays" && pwd -P)
    validate_overlays "$overlays"

    work="$root/target/milestone-states"
    rm -rf "$work"
    mkdir -p "$work"
    # Solved sources are copied only into disposable state directories and removed on exit.
    states_dir=$work
    trap 'rm -rf "$states_dir"/state-*' EXIT

    for ((n = 0; n <= milestone_count; n++)); do
        state="$work/state-$n"
        mkdir -p "$state"
        (cd "$root" && tar -cf - pom.xml README.md src scripts docs) | (cd "$state" && tar -xf -)
        for ((k = 1; k <= n; k++)); do
            cp -R "$overlays/milestone-$k/." "$state/"
        done

        check_tree "$state" "$work/logs/state-$n" "$([[ $n -eq $milestone_count ]] && echo 1 || echo 0)"

        ok=1
        [[ $health_ok -eq 1 ]] || ok=0
        for m in $(rows | cut -f1); do
            expected=0
            [[ $m -le $n ]] && expected=1
            [[ ${passed[m]} -eq $expected ]] || ok=0
            if [[ $m -gt $n ]] && ! markers_present "$state" "$m"; then
                ok=0
            fi
        done
        if [[ $n -eq $milestone_count && $final_run_ok -ne 1 ]]; then
            ok=0
        fi
        echo "state $n: $(word "$ok") (health $(word "$health_ok"); milestones passing:$(
            for m in $(rows | cut -f1); do [[ ${passed[m]} -eq 1 ]] && printf ' %s' "$m"; done
        ); completion tests $all_tests, failures $all_failures, errors $all_errors$(
            [[ $n -eq $milestone_count ]] && printf '; final run %s' "$(word "$final_run_ok")"
        ))"
        [[ $ok -eq 1 ]] || failed=1
    done

    if [[ $failed -ne 0 ]]; then
        echo "At least one milestone state did not match its contract. Logs: target/milestone-states/logs/." >&2
        exit 1
    fi
    echo "All $((milestone_count + 1)) states (starter and milestones 1-$milestone_count) match their contracts."
}

[[ $# -ge 1 ]] || usage
case $1 in
    starter) [[ $# -eq 1 ]] || usage; starter_mode ;;
    states) [[ $# -eq 2 ]] || usage; states_mode "$2" ;;
    *) [[ $# -eq 1 ]] || usage; learner_mode "$1" ;;
esac
