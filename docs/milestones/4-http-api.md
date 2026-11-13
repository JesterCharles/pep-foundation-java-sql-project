# Milestone 4: HTTP API (Week 4)

Due Friday of Week 4. It checks what was taught from Thursday of Week 3 through Wednesday of Week 4: exceptions,
logging, configuration, HTTP with Javalin, JUnit, Mockito and coverage.

The desk staff want to work from a browser or `curl`, so the lending rules move into a service behind a small JSON API.
Data still lives in memory this week; Week 5 swaps in the database without changing the service.

## What you hand in

| TODO | File | What to do |
| --- | --- | --- |
| 4A | `lending/LendingException.java` and its three subclasses | An unchecked exception family that keeps message and cause. |
| 4B | `config/AppConfig.java` | Read `LANTERN_PORT` and `LANTERN_ADMIN_TOKEN`; never print the token. |
| 4C | `lending/InMemoryToolRepository.java`, `lending/InMemoryLoanRepository.java` | Repositories on collections. |
| 4D | `lending/LendingService.java` | The lending rules, logging, and exceptions below. |
| 4E | `api/LanternApi.java` | Exception handlers and the routes below. |
| 4F | `pom.xml` | Add `org.mockito:mockito-core` with `<scope>test</scope>`, at the version the TODO comment in `pom.xml` gives. |
| 4G | `src/test/java/com/pep/toolshare/learner/LendingServiceTest.java` | Your own tests (see below). |
| 4W | `WORKLOG.md` | Your `### M4` entry, including `Coverage:`. |

Java paths are under `src/main/java/com/pep/toolshare/`. `InMemoryMemberRepository` is a finished worked example of a
repository; copy its style. Checks: `ExceptionsAndConfigCompletionTest`, `InMemoryRepositoryCompletionTest`,
`LendingServiceCompletionTest`, `LanternApiCompletionTest`, `LearnerTestsCompletionTest` and
`M4DeliveryCompletionTest`.

## Settings (4B)

| Variable | Rule | Default |
| --- | --- | --- |
| `LANTERN_PORT` | Whole number 0-65535 (0 = any free port). Otherwise `IllegalArgumentException` naming the variable, with the parsing error as its cause. | `7070` |
| `LANTERN_ADMIN_TOKEN` | Any text. Empty means admin routes are disabled. | empty |

Values are stripped and a blank value counts as missing. `toString()` shows `adminToken=<set>` or
`adminToken=<not set>`, never the token itself.

## Lending rules (4D)

| Request | Rule | Exception |
| --- | --- | --- |
| any | blank member id or tool code; tool code that is not valid after `normalizeToolCode`; unknown category | `InvalidRequestException` |
| any | unknown member, tool or loan | `NotFoundException` |
| checkout | member not active; tool not `AVAILABLE`; member already has 3 open loans | `RuleViolationException` |
| checkout | due date is today plus the category's maximum loan days | |
| checkin | loan already returned | `RuleViolationException` |
| checkin | fee from `FeePolicies.forTier(member tier)`; a damaged tool goes to `IN_REPAIR`, otherwise `AVAILABLE` | |
| retire | tool `ON_LOAN` or already `RETIRED` | `RuleViolationException` |
| receipt | loan not returned yet | `RuleViolationException` |

Logging with SLF4J: a successful checkout logs at INFO with the loan id, tool code and member id; a refused checkout
logs at WARN with the reason. Never log a member's name or email.

## Routes (4E)

Every error response is JSON `{"error": "..."}`: `InvalidRequestException` 400, `NotFoundException` 404,
`RuleViolationException` 409, anything else 500 (log it at ERROR). `GET /health` is supplied.

| Route | Body | Success |
| --- | --- | --- |
| `GET /tools?category=garden` | | 200, list of tools (`category` optional) |
| `GET /tools/{code}` | | 200, one tool |
| `GET /members/{id}/loans` | | 200, the member's open loans |
| `POST /loans` | `{"memberId":"M-0006","toolCode":"LT-1007-8"}` | 201, the new loan |
| `POST /loans/{id}/return` | `{"damaged":true}` (optional) | 200, the closed loan |
| `GET /loans/{id}/receipt` | | 200, plain-text receipt |
| `POST /tools/{code}/retire` | header `X-Admin-Token` | 204; 403 when no token is configured, 401 when the header is missing or wrong |

A missing or malformed JSON body is a 400, and so is a loan id that is not a number. JSON shapes are the records
`ToolView`, `LoanView` and `ErrorBody` in the `api` package.

Try it by hand:

```bash
set -a; source .env; set +a          # load your local settings
mvn -q compile exec:java -Dexec.args=serve
curl -i http://127.0.0.1:7070/tools/LT-1009-1
curl -i -X POST http://127.0.0.1:7070/loans -H 'Content-Type: application/json' \
  -d '{"memberId":"M-0006","toolCode":"LT-1007-8"}'
```

## Your own tests (4F, 4G)

After you add Mockito to `pom.xml` (4F), run `./scripts/prefetch.sh` once while online so Maven downloads it; offline
mode cannot fetch a new dependency.

Write at least four `@Test` methods in `learner/LendingServiceTest.java`, each arranged, acted and asserted:

- one that uses `assertThrows` for a refused request that `LendingService` itself refuses;
- one that gives `LendingService` the in-memory repositories (`SampleData.inMemory()`) as a fake and calls no mock;
- one that uses a Mockito mock with `mock(...)`, `when(...).thenReturn(...)` and `verify(...)`.

Then look at coverage: `mvn -q -Pcoverage verify` and open `target/site/jacoco/index.html`. In your work log, note
one branch of `LendingService` your tests do not cover yet.

## Steps

1. `git switch main && git pull`, then `git switch -c m4/http-api`.
2. Work in the table order: exceptions, settings, repositories, service, routes, then your tests.
   `mvn -q -Pcompletion test -Dtest='LendingServiceCompletionTest'` runs one check class.
3. Commit in small steps and push the branch. Fill in `### M4` in `WORKLOG.md`, commit, merge into `main`, push.

## Check

```bash
./scripts/verify-milestones.sh 4
```
