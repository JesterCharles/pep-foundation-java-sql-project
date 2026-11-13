# Milestone 5: Category report

**Goal:** a grouped report summarizes tools and active loans per category.

| | |
| --- | --- |
| Edit | `src/main/java/com/pep/toolshare/tool/JdbcToolRepository.java` |
| TODO | `TODO LEARNER 5` |
| Public tests | `CategoryReportCompletionTest` (1 test) |
| Check | `./scripts/verify-milestones.sh 5` |

## What to build

For each category, report the total tools, the available tools, and the active (not yet returned) loans. Include only
categories with at least `minimumToolCount` tools, ordered by category name. Use a `LEFT JOIN`, `CASE`, `GROUP BY`,
`HAVING`, and `ORDER BY` in one prepared query.

## Done when

`./scripts/verify-milestones.sh 5` reports milestones 1 through 5 as `PASS` and exits with status 0.

Previous: [Milestone 4](4-tool-lookup.md) · Next: [Milestone 6: Atomic checkout and final run](6-checkout-transaction.md)
