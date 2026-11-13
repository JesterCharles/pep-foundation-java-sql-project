# Milestone 4: Tool lookup and filtering

**Goal:** tools can be read by id and listed by availability.

| | |
| --- | --- |
| Edit | `src/main/java/com/pep/toolshare/tool/JdbcToolRepository.java` |
| TODO | `TODO LEARNER 4A`–`4B` |
| Contract | `JdbcToolRepositoryCompletionTest` (2 tests) |
| Check | `./scripts/verify-milestones.sh 4` |

## What to build

- **4A:** find one tool by id and map its row, including its `ToolStatus`.
- **4B:** list available tools in one category, or in every category when the argument is `null`, with a prepared
  filter and a deterministic `ORDER BY`.

Leave `TODO LEARNER 5` in the same file for the next milestone.

## Done when

`./scripts/verify-milestones.sh 4` reports milestones 1 through 4 as `PASS` and exits with status 0. Running
`mvn -q compile exec:java` now lists the available tools.

Previous: [Milestone 3](3-member-crud.md) · Next: [Milestone 5: Category report](5-category-report.md)
