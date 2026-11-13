# Objective map

What each milestone practices, what you need before you start, and what the project adapts or leaves out. It holds no
assessments or answers.

## Week by week

| Milestone | Practiced in the project | Evidence |
| --- | --- | --- |
| 1 Team workflow | Shell navigation, files and permissions, environment variables and secrets, pipes and `grep`; Git commits, diffs, `.gitignore`, branches, remotes and push, merge conflicts, revert; user stories with acceptance criteria; reviewing an assistant's change against its diff; stand-ups and pull-request notes | Script output tests, ignore rules, your Git history, `WORKLOG.md` |
| 2 Lending basics | Types and operators, `if`/`switch`, loops, strings, methods, recursion, arrays, binary search, `ArrayList`, `HashMap` | Unit tests of `LendingMath` |
| 3 Domain model | Classes, constructors, encapsulation, validation, `null` handling, equality, enums with fields, inheritance and abstract classes, interfaces and polymorphism, composition, records, functional interfaces with anonymous classes, `StringBuilder`, Maven project structure | Unit tests of the domain classes |
| 4 HTTP API | Unchecked exception hierarchies with causes, SLF4J logging levels without personal data, configuration from the environment, collection-backed repositories, constructor injection, lambdas as handlers, Javalin routes, path and query parameters, JSON bodies, status codes and exception handlers; JUnit tests, fakes, Mockito mocks, coverage reading | Service tests, real HTTP requests to your API, your own tests, `WORKLOG.md` |
| 5 Database | Tables, keys, relationships, data types, constraints and defaults; CRUD; joins, anti-joins and self-joins; aggregates with `GROUP BY` and `HAVING`; `COALESCE` and `CASE`; subqueries and CTEs; window functions for top N per group; indexes and `EXPLAIN`; JDBC with prepared statements, generated keys, row mapping and try-with-resources; transactions with commit and rollback; swapping a repository implementation; testing against an isolated database | Schema, repository, transaction and report tests; your own database tests |
| 6 Final delivery | `equals`/`hashCode`/`compareTo` contracts; `HashSet`, `TreeMap`, queues and stacks with `ArrayDeque`; generics; lambdas, method references, `Optional`, stream pipelines and collectors; choosing SQL or streams; time complexity; an integrated, persistent application over HTTP | Collection and stream tests, report routes, an end-to-end run with restart, the final report |

Every week's `WORKLOG.md` entry records a three-line stand-up and pull-request notes: the user story, a security
check, what an AI assistant produced, and how you verified it.

## Prerequisites

A computer that runs JDK 25, Maven 3.9 or newer, Git 2.38 or newer, `bash` and `curl`, and network access for the
first Maven build. Each milestone assumes the lessons taught up to it; nothing beyond the course is required.

## Adapted for a local-only project

- **Database:** an embedded H2 database in PostgreSQL mode replaces a PostgreSQL server, `psql` and the PostgreSQL
  JDBC driver. The SQL shell is `./scripts/sql.sh`; `psql` backslash commands, `\copy` and `generate_series` are not
  used, and `EXPLAIN` output has a different format.
- **Pull requests:** pull-request notes go in `WORKLOG.md`. If your course Git server offers pull requests, you may
  also open them, but the checks read only your local Git history.
- **Not checked automatically:** using an IDE debugger, `git stash` and rebase, and your recorded or live walkthrough.
  Practise them as the guides suggest; your trainer reviews them.

## Left out

Frameworks other than Javalin, containers, cloud services, external APIs, authentication beyond a single admin token,
concurrency, and database administration.
