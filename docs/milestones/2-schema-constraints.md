# Milestone 2: Relational constraints

**Goal:** the database itself rejects rows that break the library's rules.

| | |
| --- | --- |
| Edit | `src/main/resources/db/schema.sql` |
| TODO | `TODO LEARNER 2A`–`2D` |
| Contract | `SchemaCompletionTest` (4 tests) |
| Check | `./scripts/verify-milestones.sh 2` |

## What to build

- **2A:** member emails are unique.
- **2B:** a tool status is one of `AVAILABLE`, `ON_LOAN`, or `MAINTENANCE`.
- **2C:** every loan references an existing member and an existing tool.
- **2D:** a due date or return date is never earlier than the borrowed date.

Keep the existing columns, types, and seed data unchanged. After editing the schema, run `./scripts/reset.sh` so your
local database uses it.

## Done when

`./scripts/verify-milestones.sh 2` reports milestones 1 and 2 as `PASS` and exits with status 0.

Previous: [Milestone 1](1-member-validation.md) · Next: [Milestone 3: Member CRUD and mapping](3-member-crud.md)
