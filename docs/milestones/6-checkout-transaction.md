# Milestone 6: Atomic checkout and final run

**Goal:** checking out a tool changes two tables as one unit, and the whole project is complete.

| | |
| --- | --- |
| Edit | `src/main/java/com/pep/toolshare/loan/LoanService.java` |
| TODO | `TODO LEARNER 6` |
| Public tests | `LoanServiceCompletionTest` (4 tests) plus the final run check |
| Check | `./scripts/verify-milestones.sh 6` |

## What to build

In one JDBC transaction, mark an available tool `ON_LOAN` and insert its loan, then return the new loan id. Commit
only after both statements succeed. When the tool is not available or any statement fails, roll back and throw
`LoanException`.

## Final run check

Milestone 6 also checks the whole project, exactly like the completion criteria in the README:

- every completion test passes and `mvn -q clean verify` still passes;
- after a reset, `mvn -q exec:java` prints `Available tools: 3` and `Category report rows: 3`;
- no `TODO LEARNER` marker remains in the editable files.

The final run check resets the local `data/` database.

## Done when

`./scripts/verify-milestones.sh 6` reports every milestone and the final run as `PASS` and exits with status 0.

Previous: [Milestone 5](5-category-report.md)
