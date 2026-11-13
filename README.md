# Lantern Tool Library: Foundation Project 1

> **Fictional scenario:** Lantern Tool Library is an invented neighborhood tool-lending library. All names, emails,
> dates and records in this repository are synthetic.

Over six weeks you build the library's lending system: a team Git workflow and shell tooling, Java business rules,
a JSON HTTP API, a relational database with JDBC repositories and reports, and an integrated final delivery. Each
Friday milestone adds to the same project.

Everything runs on your own machine: Java, Maven, an embedded database and a local web server. There is no container,
cloud account, external database server or real credential.

## Prerequisites

- From Day 1: Git 2.38 or newer (`git --version`) and a terminal with `bash`.
- From Thursday of Week 1, when the course installs them: JDK 25 (`java -version`) and Apache Maven 3.9 or newer
  (`mvn -v`).
- From Week 4: `curl` for trying the API.

## Start

You already cloned your own copy of this project from the course Git server. The Milestone 1 work needs only Git and
the shell, so start with its [guide](docs/milestones/1-team-workflow.md) on Day 1.

Once Java and Maven are installed, go to the project's top folder (the one with this `README.md`) and run:

```bash
./scripts/prefetch.sh                       # once, while online
mvn -q verify                               # starter health: passes
./scripts/verify-milestones.sh progress     # every milestone shows FAIL at the start
```

`mvn -q verify` passing while every milestone fails is the expected starting point, not a broken setup.

`./scripts/prefetch.sh` runs every Maven command of this project once, so Maven downloads all the libraries and build
plugins they use from Maven Central. After that, every command in this README also works without a network
connection: first run `export MAVEN_ARGS=-o` (Maven offline mode) in that terminal. Run `./scripts/prefetch.sh` again,
online, whenever you add a dependency to `pom.xml` (Milestone 4 adds Mockito).

## Milestones

Each milestone is due on Friday and checks only what was taught through Wednesday of that week: the lessons from the
previous Thursday through that Wednesday, plus everything earlier. Milestone N is complete when
`./scripts/verify-milestones.sh N` passes, which includes milestones 1 through N.

| Due | Milestone | Checks lessons taught | Check |
| --- | --- | --- | --- |
| Friday, Week 1 | [Team workflow: Linux, Git and AI](docs/milestones/1-team-workflow.md) | Week 1 Monday-Wednesday | `./scripts/verify-milestones.sh 1` |
| Friday, Week 2 | [Lending basics: Java, methods and arrays](docs/milestones/2-lending-basics.md) | Week 1 Thursday-Week 2 Wednesday | `./scripts/verify-milestones.sh 2` |
| Friday, Week 3 | [Domain model: classes, interfaces and inheritance](docs/milestones/3-domain-model.md) | Week 2 Thursday-Week 3 Wednesday | `./scripts/verify-milestones.sh 3` |
| Friday, Week 4 | [HTTP API: exceptions, Javalin and testing](docs/milestones/4-http-api.md) | Week 3 Thursday-Week 4 Wednesday | `./scripts/verify-milestones.sh 4` |
| Friday, Week 5 | [Database: SQL design, reports and JDBC](docs/milestones/5-database.md) | Week 4 Thursday-Week 5 Wednesday | `./scripts/verify-milestones.sh 5` |
| Friday, Week 6 (Checkpoint day) | [Final delivery: collections, streams and integration](docs/milestones/6-final-delivery.md) | Week 5 Thursday-Week 6 Wednesday | `./scripts/verify-milestones.sh 6` |

The check prints PASS or FAIL with test counts for every milestone. Fix the earliest FAIL first; details are in
`target/milestone-check/` and `target/surefire-reports/`.

Every milestone is delivered the same way: work on a branch named `m<N>/<topic>` (for example `m2/lending-basics`),
commit in small steps, push the branch, merge it into `main`, push `main`, and fill in that week's entry in
`WORKLOG.md`. The checks read your local Git history, so they work with any remote name or server address.

## Time and pace

The course schedule gives the project 280 minutes of class time: 160 minutes of milestone sessions (20-30 minutes per
milestone) and a 120-minute session on Day 29 to finish Milestone 6 and record your walkthrough. Completing the
project takes longer than that: plan on roughly 1,350 to 2,355 minutes (about 22.5 to 39 hours) over the six weeks, and expect your own pace to differ
from anyone else's. That is intended. The platform gives you additional time to complete and catch up on milestones,
and your trainer may ask how long each milestone took; that feedback is used to check and adjust this plan.

Because the project builds on itself every week, steady and complete work shows. Milestone 6 with your walkthrough is
part of the Week 6 Checkpoint score, which is used for Fast Track selection.

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
./scripts/verify-milestones.sh progress             # show PASS/FAIL for every milestone
./scripts/prefetch.sh                               # download everything once, so you can work offline
./scripts/reset.sh                                  # recreate the local database in data/ with sample data
./scripts/sql.sh                                    # SQL shell on the local database
mvn -q compile exec:java -Dexec.args="report 2026-10-15"     # print the database reports
mvn -q compile exec:java -Dexec.args=serve          # start the API on http://127.0.0.1:7070 (this computer only)
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
single snapshot. Nothing else is required: no extra branches, tags, history, secrets or services, and no network access
at run time once `./scripts/prefetch.sh` has downloaded the dependencies (run it again after adding one, as in
Milestone 4). The checks work with any repository owner, URL or remote name.

The included CI workflow (`.github/workflows/ci.yml`) works at every stage of the project: it runs the health build,
prints the milestone progress report (unfinished milestones do not fail it), and runs a privacy scan of the
repository.

## Troubleshooting

- **Wrong Java version:** make JDK 25 your active `JAVA_HOME`, then rerun `java -version` and `mvn -v`.
- **Dependencies cannot download:** connect to the network and run `./scripts/prefetch.sh` once.
- **"Cannot access ... in offline mode":** that file was never downloaded. Run `unset MAVEN_ARGS`, connect, and run
  `./scripts/prefetch.sh` once.
- **`git merge-tree` error in a check:** update Git to 2.38 or newer.
- **"main has no upstream branch":** push once with `git push -u origin main`.
- **Port already in use:** stop the other server, or set `LANTERN_PORT` to another port.
- **Database file is locked:** stop the server or SQL shell that has it open, then retry.
- **Database errors after a schema change:** fix `schema.sql`, then run `./scripts/reset.sh`.
- **A completion test fails in the starter:** expected. Read the failing test and the matching TODO.
