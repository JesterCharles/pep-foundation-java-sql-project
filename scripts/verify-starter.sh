#!/usr/bin/env bash
set -euo pipefail

rm -rf target
mkdir -p target
set +e
mvn -q -Pcompletion test >target/starter-completion.log 2>&1
status=$?
set -e

if [[ $status -eq 0 ]]; then
    echo "Expected the untouched starter completion tests to fail, but they passed." >&2
    exit 1
fi

classes=(
    MemberDomainCompletionTest
    SchemaCompletionTest
    JdbcMemberRepositoryCompletionTest
    JdbcToolRepositoryCompletionTest
    LoanServiceCompletionTest
)

for class in "${classes[@]}"; do
    report=$(find target/surefire-reports -name "TEST-*${class}.xml" -print -quit)
    if [[ -z $report ]] || ! grep -Eq '<(failure|error)' "$report"; then
        echo "Expected a TODO failure in ${class}; see target/starter-completion.log." >&2
        exit 1
    fi
done

echo "Confirmed the five documented learner TODO areas fail in the untouched starter."
