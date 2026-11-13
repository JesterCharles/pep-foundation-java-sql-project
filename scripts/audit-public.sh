#!/usr/bin/env bash
set -euo pipefail

root=$(cd "$(dirname "$0")/.." && pwd -P)
cd "$root"

# This audit reads Git history. A copy without .git (for example a downloaded archive) has nothing to audit, so
# refuse instead of reporting a scan that never ran.
if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1 ||
    [[ $(cd "$(git rev-parse --show-toplevel)" && pwd -P) != "$root" ]]; then
    echo "audit-public.sh needs a Git checkout of this project; no Git history was found, so nothing was audited." >&2
    exit 2
fi

if git ls-files | grep -Eq '(^|/)(target|data|\.local-tools|\.fresh-copy)/|\.(mv|trace)\.db$'; then
    echo "Tracked generated or local-only artifact found." >&2
    exit 1
fi

if git ls-files | grep -Eq '(^|/)milestone-[0-9]+/'; then
    echo "Tracked milestone overlay directory found; solved overlays must stay out of version control." >&2
    exit 1
fi

sensitive='(/Users/[^/]+/|BEGIN (RSA|OPENSSH|EC) PRIVATE KEY|gh[pousr]_[A-Za-z0-9]{20,}|AKIA[0-9A-Z]{16})'
if git grep -I -n -E "$sensitive" -- . ':!scripts/audit-public.sh'; then
    echo "Potential private path, key, or credential found in tracked files." >&2
    exit 1
fi

while read -r object path; do
    [[ -n ${path:-} ]] || continue
    [[ $path == scripts/audit-public.sh ]] && continue
    [[ $(git cat-file -t "$object") == blob ]] || continue
    if git cat-file blob "$object" | grep -E -q "$sensitive"; then
        echo "Potential private material found in reachable blob: $path" >&2
        exit 1
    fi
done < <(git rev-list --objects --all)

echo "Tracked paths and reachable blobs passed the public-material scan."
