# Lantern Tool Library: Java + SQL/JDBC Foundation Project

> **Fictional scenario:** Lantern Tool Library is an invented neighborhood business. All names, emails, dates, and
> records in this repository are synthetic.

Build the data layer for a small tool-lending backend with plain Java, JDBC, and a local embedded SQL database. There
is no web framework, container, cloud account, external database server, credential, or runtime network service.

## Prerequisites

- JDK 25 (`java -version`)
- Apache Maven 3.9 or newer (`mvn -v`)

The first Maven build downloads declared dependencies. After that, the application itself runs entirely on your
machine.

## Quick start

From the repository root:

```bash
mvn -q verify                                # starter health: expected to pass
mvn -q compile exec:java -Dexec.args=reset   # recreate the local database and synthetic seed data
mvn -q compile exec:java                     # run the report entry point
mvn -q -Pcompletion test                     # learner contract: expected to fail until TODOs are complete
```

The passing health build and failing completion profile are intentional starter behavior, not a broken setup.
`./scripts/verify-milestones.sh starter` confirms that health passes and every milestone fails in the untouched starter.

## Milestones

Work through six cumulative milestones in order. Each guide names the file and TODO markers to edit, the public tests
that define the milestone, and the check to run. Milestone N is complete when `./scripts/verify-milestones.sh N`
passes, which requires milestones 1 through N to pass.

| Milestone | Guide | Check |
| --- | --- | --- |
| 1 | [Member validation](docs/milestones/1-member-validation.md) | `./scripts/verify-milestones.sh 1` |
| 2 | [Relational constraints](docs/milestones/2-schema-constraints.md) | `./scripts/verify-milestones.sh 2` |
| 3 | [Member CRUD and mapping](docs/milestones/3-member-crud.md) | `./scripts/verify-milestones.sh 3` |
| 4 | [Tool lookup and filtering](docs/milestones/4-tool-lookup.md) | `./scripts/verify-milestones.sh 4` |
| 5 | [Category report](docs/milestones/5-category-report.md) | `./scripts/verify-milestones.sh 5` |
| 6 | [Atomic checkout and final run](docs/milestones/6-checkout-transaction.md) | `./scripts/verify-milestones.sh 6` |

The check prints PASS or FAIL with test counts for every milestone; details are in `target/surefire-reports/`.

## What you will practice

- Model and validate Java data with records and enums.
- enforce relational keys, uniqueness, checks, and valid dates;
- implement CRUD with prepared statements and generated keys;
- map `ResultSet` rows into Java objects with try-with-resources;
- write deterministic filtered and aggregate reports with joins, `CASE`, `GROUP BY`, and `HAVING`;
- commit or roll back a multi-statement checkout as one transaction;
- test against a fresh isolated database and reset local data repeatably.

The learning outcomes, expected prerequisites, and topics this project leaves out are listed in
[`docs/objective-map.md`](docs/objective-map.md).

## Your editable files

Edit only these learner-owned TODO regions:

| File | TODO | Milestone |
| --- | --- | --- |
| `src/main/java/com/pep/toolshare/domain/Member.java` | `TODO LEARNER 1`: required-value validation | 1 |
| `src/main/resources/db/schema.sql` | `TODO LEARNER 2A`–`2D`: relational constraints | 2 |
| `src/main/java/com/pep/toolshare/member/JdbcMemberRepository.java` | `TODO LEARNER 3A`–`3D`: member CRUD and mapping | 3 |
| `src/main/java/com/pep/toolshare/tool/JdbcToolRepository.java` | `TODO LEARNER 4A`–`4B`: tool lookup and filtering | 4 |
| `src/main/java/com/pep/toolshare/tool/JdbcToolRepository.java` | `TODO LEARNER 5`: category report | 5 |
| `src/main/java/com/pep/toolshare/loan/LoanService.java` | `TODO LEARNER 6`: atomic checkout transaction | 6 |

Keep method signatures and package names unchanged. Search with `grep -R "TODO LEARNER" src/main` to find every
region.

## Supplied contract files

Do not modify `pom.xml`, `.github/`, `scripts/`, `docs/`, `src/test/`, `src/main/java/com/pep/toolshare/App.java`, the
`db` support classes, repository interfaces, supplied domain types, or `src/main/resources/db/seed.sql`. They provide
the runnable shell, deterministic fixtures, and public executable contract. There are no hidden tests or grading
secrets in this repository.

## Commands

```bash
./scripts/health.sh                    # clean compile + starter health tests
./scripts/reset.sh                     # delete/recreate local schema and seed rows
mvn -q compile exec:java               # print available tools and the category report
mvn -q -Pcompletion test               # run every public learner-completion test
./scripts/verify-milestones.sh N       # check milestones 1 through N (1-6)
```

Reset replaces only the ignored `data/` database. Tests use separate in-memory databases and do not touch local data.

## Completion criteria

You are done when `./scripts/verify-milestones.sh 6` passes. It checks the same criteria:

1. `mvn -q -Pcompletion test` passes;
2. `mvn -q verify` still passes;
3. reset followed by `mvn -q compile exec:java` prints three available tools and three category rows; and
4. every `TODO LEARNER` marker in the five editable files has been replaced by your implementation.

`./scripts/verify-milestones.sh 6` resets the local `data/` database as part of the final run check.

## Database note

This project uses an embedded local database that runs inside the Java process. You do not install PostgreSQL or run
any database server. Local data is stored in the `data/` directory, which Git ignores; run `./scripts/reset.sh` at
any time to recreate it with the supplied seed data. The database runs in a PostgreSQL-compatible mode, so the SQL
you practice here is a portable subset, but the embedded database is not PostgreSQL, and PostgreSQL-only tools and
features are not part of this project.

## Troubleshooting

- **Enforcer rejects Java:** make JDK 25 the active `JAVA_HOME`, then rerun `java -version` and `mvn -v`.
- **A database file is locked:** stop another running copy, then run `./scripts/reset.sh`.
- **Report fails after a schema edit:** fix `schema.sql`, then run `./scripts/reset.sh` to rebuild the local database.
- **Dependencies are unavailable offline:** run the first Maven command once with network access so Maven can fill its
  local cache.
- **Starter completion tests fail:** expected. Read the named public test and the matching TODO; use
  `target/surefire-reports/` for details.
- **A milestone check fails:** the check lists every milestone with its test counts. Fix the earliest failing
  milestone first; logs are in `target/milestone-check/`.
