# Lantern Tool Library: Foundation Project 1

> **Fictional scenario:** Lantern Tool Library is an invented neighborhood tool-lending library. All names, emails,
> dates and records in this repository are synthetic.

Over six weeks you build the library's lending system: a team Git workflow and shell tooling, Java business rules,
a JSON HTTP API, a relational database with JDBC repositories and reports, and an integrated final delivery. Each
Friday milestone adds to the same project.

Everything runs on your own machine: Java, Maven, an embedded database and a local web server. There is no container,
cloud account, external database server or real credential.

## Prerequisites

- JDK 25 (`java -version`)
- Apache Maven 3.9 or newer (`mvn -v`)
- Git 2.38 or newer (`git --version`)
- A terminal with `bash`, and `curl` for trying the API

The first Maven build downloads the project's libraries from Maven Central, so it needs network access once. After
that, everything runs offline.

## Start

You already cloned your own copy of this project from the course Git server. In a terminal, go to its top folder
(the one with this `README.md`) and run:

```bash
mvn -q verify                    # starter health: passes
./scripts/verify-milestones.sh starter
```

`mvn -q verify` passing while every milestone fails is the expected starting point, not a broken setup.

## Milestones

Each milestone is due on Friday and uses what was taught through that week. Milestone N is complete when
`./scripts/verify-milestones.sh N` passes, which includes milestones 1 through N.

| Week | Milestone | Check |
| --- | --- | --- |
| 1 | [Team workflow: Linux, Git and AI](docs/milestones/1-team-workflow.md) | `./scripts/verify-milestones.sh 1` |
| 2 | [Lending basics: Java, methods and arrays](docs/milestones/2-lending-basics.md) | `./scripts/verify-milestones.sh 2` |
| 3 | [Domain model: classes, interfaces and inheritance](docs/milestones/3-domain-model.md) | `./scripts/verify-milestones.sh 3` |
| 4 | [HTTP API: exceptions, Javalin and testing](docs/milestones/4-http-api.md) | `./scripts/verify-milestones.sh 4` |
| 5 | [Database: SQL design, reports and JDBC](docs/milestones/5-database.md) | `./scripts/verify-milestones.sh 5` |
| 6 | [Final delivery: collections, streams and integration](docs/milestones/6-final-delivery.md) | `./scripts/verify-milestones.sh 6` |

The check prints PASS or FAIL with test counts for every milestone. Fix the earliest FAIL first; details are in
`target/milestone-check/` and `target/surefire-reports/`.

Every milestone is delivered the same way: work on a branch named `m<N>/<topic>` (for example `m2/lending-basics`),
commit in small steps, push the branch, merge it into `main`, push `main`, and fill in that week's entry in
`WORKLOG.md`. The checks read your local Git history, so they work with any remote name or server address.

## Your files

Edit only the `TODO LEARNER` regions. Each guide lists its TODO ids and files; `docs/milestones/manifest.tsv` lists
them all. To find what is left: `grep -rn "TODO LEARNER" src bin WORKLOG.md .gitignore .env.example pom.xml`.

Do not change the supplied files: `src/test/` outside `src/test/java/com/pep/toolshare/learner/`, `scripts/`, `docs/`,
`fixtures/`, `review/`, `seed.sql`, and the classes without TODO markers. They are the public checks and fixtures;
there are no hidden tests. Keep method signatures and package names unchanged.

## Commands

```bash
mvn -q verify                                       # compile and run the health tests (and your own tests)
mvn -q -Pcompletion test                            # run every milestone check
mvn -q -Pcompletion test -Dtest=LendingMathCompletionTest   # run one check class
./scripts/verify-milestones.sh N                    # check milestones 1 through N
./scripts/reset.sh                                  # recreate the local database in data/ with sample data
./scripts/sql.sh                                    # SQL shell on the local database
mvn -q compile exec:java -Dexec.args="report 2026-10-15"     # print the database reports
mvn -q compile exec:java -Dexec.args=serve          # start the API on http://localhost:7070
mvn -q -Pcoverage verify                            # coverage report in target/site/jacoco/index.html
```

Settings come from environment variables, never from code. Copy `.env.example` to `.env` (Git ignores it), edit it,
and load it with `set -a; source .env; set +a` before starting the server.

## Database note

The project uses H2, an embedded database that runs inside the Java process, in a PostgreSQL-compatible mode. There
is no database server to install, and the database file lives in the ignored `data/` folder. The SQL you write is
standard and works in PostgreSQL too, but a few PostgreSQL tools behave differently here: use `./scripts/sql.sh`
instead of `psql` (no backslash commands such as `\dt`; use `SHOW TABLES;`), and `EXPLAIN` output looks different.

## Platform import

For course platforms that create one repository per learner: import this repository's default branch `main` as a
single snapshot. Nothing else is required: no extra branches, tags, history, secrets, services or network access at
run time. The checks work with any repository owner, URL or remote name.

## Troubleshooting

- **Wrong Java version:** make JDK 25 your active `JAVA_HOME`, then rerun `java -version` and `mvn -v`.
- **Dependencies cannot download:** run the first Maven command once with network access.
- **`git merge-tree` error in a check:** update Git to 2.38 or newer.
- **"main has no upstream branch":** push once with `git push -u origin main`.
- **Port already in use:** stop the other server, or set `LANTERN_PORT` to another port.
- **Database file is locked:** stop the server or SQL shell that has it open, then retry.
- **Database errors after a schema change:** fix `schema.sql`, then run `./scripts/reset.sh`.
- **A completion test fails in the starter:** expected. Read the failing test and the matching TODO.
