# Milestone 3: Member CRUD and mapping

**Goal:** members can be created, read, updated, and deleted through JDBC.

| | |
| --- | --- |
| Edit | `src/main/java/com/pep/toolshare/member/JdbcMemberRepository.java` |
| TODO | `TODO LEARNER 3A`–`3D` |
| Contract | `JdbcMemberRepositoryCompletionTest` (2 tests) |
| Check | `./scripts/verify-milestones.sh 3` |

## What to build

- **3A:** insert a member with a `PreparedStatement` and return the generated id.
- **3B:** find a member by id and map the row to `Member`; a missing id returns `Optional.empty()`.
- **3C:** update a member's email and return whether exactly one row changed.
- **3D:** delete a member by id and return whether exactly one row changed.

Bind every value as a parameter, close every JDBC resource with try-with-resources, and wrap `SQLException` in the
supplied `RepositoryException`.

## Done when

`./scripts/verify-milestones.sh 3` reports milestones 1 through 3 as `PASS` and exits with status 0.

Previous: [Milestone 2](2-schema-constraints.md) · Next: [Milestone 4: Tool lookup and filtering](4-tool-lookup.md)
