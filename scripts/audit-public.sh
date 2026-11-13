#!/usr/bin/env bash
set -euo pipefail

if git ls-files | grep -Eq '(^|/)(target|data|\.local-tools|\.fresh-copy)/|\.(mv|trace)\.db$'; then
    echo "Tracked generated or local-only artifact found." >&2
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
