# Milestone 1: Member validation

**Goal:** a `Member` can only be created with usable values.

| | |
| --- | --- |
| Edit | `src/main/java/com/pep/toolshare/domain/Member.java` |
| TODO | `TODO LEARNER 1` |
| Public tests | `MemberDomainCompletionTest` (1 test) |
| Check | `./scripts/verify-milestones.sh 1` |

## What to build

Reject a null or blank full name and a null, blank, or malformed email address by throwing
`IllegalArgumentException` from the record's compact constructor. A record with an `id` of `null` (not yet saved) is
still valid.

## Done when

`./scripts/verify-milestones.sh 1` reports `milestone 1 member-validation: PASS` and exits with status 0.

Next: [Milestone 2: Relational constraints](2-schema-constraints.md)
