# Objective map

What each milestone practices, what you need before you start, and what the project adapts or leaves out. It holds no
assessments or answers.

## Week by week

Each milestone is due on Friday and checks only lessons taught through Wednesday of that week. Lessons taught on
Thursday and Friday are checked by the next milestone.

| Milestone (due) | Lessons it checks | Practiced in the project | Evidence |
| --- | --- | --- | --- |
| 1 Team workflow (Friday, Week 1) | Week 1 Monday-Wednesday | Shell navigation, files and permissions, environment variables and secrets, pipes and `grep`; Git commits, diffs, `.gitignore`, branches, remotes and push, merge conflicts, revert; user stories with acceptance criteria; reviewing an assistant's change against its diff; stand-ups and pull-request notes | Script output tests, ignore rules, your Git history, `WORKLOG.md` |
| 2 Lending basics (Friday, Week 2) | Week 1 Thursday-Week 2 Wednesday | Types and operators, `if`/`switch`, loops, strings, methods, recursion, arrays, binary search, `ArrayList`, `HashMap` | Unit tests of `LendingMath` |
| 3 Domain model (Friday, Week 3) | Week 2 Thursday-Week 3 Wednesday | Classes, constructors, encapsulation, validation, `null` handling, equality, enums with fields, inheritance and abstract classes, interfaces and polymorphism, composition, records, functional interfaces with anonymous classes, `StringBuilder`, Maven project structure | Unit tests of the domain classes |
| 4 HTTP API (Friday, Week 4) | Week 3 Thursday-Week 4 Wednesday | Unchecked exception hierarchies with causes, SLF4J logging levels without personal data, configuration from the environment, collection-backed repositories, constructor injection, lambdas as handlers, Javalin routes, path and query parameters, JSON bodies, status codes and exception handlers; JUnit tests, fakes, Mockito mocks, coverage reading | Service tests, real HTTP requests to your API, your own tests, `WORKLOG.md` |
| 5 Database (Friday, Week 5) | Week 4 Thursday-Week 5 Wednesday | Tables, keys, relationships, data types, constraints and defaults; CRUD; joins, anti-joins and self-joins; aggregates with `GROUP BY` and `HAVING`; `COALESCE` and `CASE`; subqueries and CTEs; window functions for top N per group; indexes and `EXPLAIN`; JDBC with prepared statements, generated keys, row mapping and try-with-resources; transactions with commit and rollback; swapping a repository implementation; testing against an isolated database | Schema, repository, transaction and report tests; your own database tests |
| 6 Final delivery (Friday, Week 6: Checkpoint day) | Week 5 Thursday-Week 6 Wednesday | `equals`/`hashCode`/`compareTo` contracts; `HashSet`, `TreeMap`, queues and stacks with `ArrayDeque`; generics; lambdas, method references, `Optional`, stream pipelines and collectors; choosing SQL or streams; time complexity; an integrated, persistent application over HTTP | Collection and stream tests, report routes, an end-to-end run with restart, the final report |

Every week's `WORKLOG.md` entry records a three-line stand-up and pull-request notes: the user story, a security
check, what an AI assistant produced, and how you verified it.

## Prerequisites

A computer that runs Git 2.38 or newer and `bash` from Day 1; JDK 25 and Maven 3.9 or newer from Thursday of Week 1,
when the course installs them; and `curl` from Week 4. Maven needs network access for `./scripts/prefetch.sh`, once at
the start and again after you add a dependency (Milestone 4 adds Mockito); in between, the Maven commands and project
scripts run offline with `MAVEN_ARGS=-o`. Pushing to the course server always needs a network connection. The Milestone
1 work needs only the shell and Git; its check runs once Java and Maven are installed, before Friday. Each milestone
assumes the lessons taught up to it; nothing beyond the course is required.

## Schedule and completion time

- **Scheduled class time:** 280 minutes in the course schedule. That is 160 minutes of milestone sessions (Milestones 1
  and 6: 20 minutes each; Milestones 2-5: 30 minutes each) and the 120-minute Day 29 session to finish Milestone 6 and
  record the walkthrough. Milestone 6 is due on Day 30 (the Checkpoint) and adds no project minutes to that day.
- **Completion time:** these sessions are touchpoints, not the time needed to finish. The planning range for
  completing all six milestones is 1,350-2,355 minutes (about 22.5-39 hours), and individual pace is expected to vary
  widely. The platform provides additional completion and recovery time. Learner feedback on actual time per milestone
  is used to validate and adjust this range.
- **Why cumulative:** each week builds on the last, so sustained, complete work stands out. This helps distinguish
  strongly engaged learners, and the finished project with its walkthrough is part of the Checkpoint score used for
  Fast Track selection.

## Adapted for a local-only project

- **Database:** an embedded H2 database in PostgreSQL mode replaces a PostgreSQL server, `psql` and the PostgreSQL
  JDBC driver. The SQL shell is `./scripts/sql.sh`; `psql` backslash commands, `\copy` and `generate_series` are not
  used, and `EXPLAIN` output has a different format.
- **Pull requests:** pull-request notes go in `WORKLOG.md`. If your course Git server offers pull requests, you may
  also open them, but the checks read only your local Git history.
- **Not checked automatically:** using an IDE debugger, `git stash` and rebase, and your recorded or live walkthrough.
  Practise them as the guides suggest; your trainer reviews them.
- **Shell control flow:** the missing-file `if` block in `bin/loan-summary.sh` is supplied, because shell conditionals
  are not part of the Week 1 lessons; Milestone 1 asks only for pipes, `grep`, `wc` and variables.
- **API address:** the API listens on `127.0.0.1` only, so it is reachable from your own computer and not from the
  network.

## Left out

Frameworks other than Javalin, containers, cloud services, external APIs, authentication beyond a single admin token,
concurrency, and database administration.
