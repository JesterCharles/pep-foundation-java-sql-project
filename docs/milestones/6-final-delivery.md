# Milestone 6: Final delivery (Week 6)

Due Friday of Week 6. It uses the SQL and JDBC review, collections, generics, lambdas, method references, `Optional`
and streams from Weeks 5-6, and brings every earlier milestone together.

The library is ready for its first real week: members can queue for busy tools, returned tools are shelved from a
cart, managers get analytics and reports over HTTP, and everything survives a restart on the database.

## What you hand in

| TODO | File | What to do |
| --- | --- | --- |
| 6A | `domain/ShelfLocation.java` | `equals`, `hashCode` and `compareTo` that keep their contracts. |
| 6B | `reports/WaitList.java`, `reports/ReturnCart.java` | A generic first-in-first-out queue without duplicates, and a stack. |
| 6C | `reports/InventoryAnalytics.java` | Six stream pipelines: no `for` or `while` loops in this class. |
| 6D | `api/LanternApi.java` | The report, analytics and wait-list routes below. |
| 6W | `WORKLOG.md` | Your `### M6` entry, including `SQL or stream:`, `Big-O:` and `Walkthrough:`. |

Java paths are under `src/main/java/com/pep/toolshare/`. Checks: `CollectionsCompletionTest`,
`InventoryAnalyticsCompletionTest`, `FinalApiCompletionTest`, `FinalDeliveryCompletionTest` and
`M6DeliveryCompletionTest`. `FinalDeliveryCompletionTest` drives the whole application over HTTP on a database file,
restarts it, and checks that every change was saved: it passes only when Milestones 1-5 work together.

## Collections (6A, 6B)

- Two `ShelfLocation`s are equal when aisle and bay are equal; equal objects need equal hash codes; natural order is
  aisle, then bay. Then they work as `HashMap` keys, in a `HashSet`, and in a `TreeMap`.
- `WaitList<T>`: pick collections that give first-in-first-out order and a fast "already waiting?" check. Read each
  method's comment for empty and `null` cases.
- `ReturnCart`: last in, first out. Prefer `ArrayDeque` to the old `Stack` class.

## Streams (6C)

Use `stream()`, `filter`, `map`, `sorted` with a `Comparator`, `limit`, `max`, and `Collectors` such as `toMap`,
`groupingBy` (with `counting` or `mapping`) and a map supplier such as `TreeMap::new` (enum keys sort in declaration order). Use at least one method
reference such as `Tool::code`. `longestOverdue` returns an `Optional`. Results must not depend on the order of
the input lists.

## Routes (6D)

| Route | Success | Errors |
| --- | --- | --- |
| `GET /reports/categories` | 200, `categoryUsage()` | 503 when data is in memory (no reports) |
| `GET /reports/overdue?asOf=2026-10-15` | 200, `overdueLoans(asOf)`; `asOf` defaults to today | 400 bad date, 503 in memory |
| `GET /reports/top-tools?limit=3` | 200, `InventoryAnalytics.mostBorrowed` over every loan; default 3 | 400 unless 1-20 |
| `GET /reports/shelves` | 200, object from shelf (`"A-01"`) to sorted tool codes, shelves in order | |
| `POST /tools/{code}/waitlist` | `{"memberId":"M-0003"}`: 201, `{"toolCode","memberId","position"}` | 400 no body; 404 unknown tool or member; 409 tool available or member already waiting |
| `GET /tools/{code}/waitlist` | 200, `{"toolCode","waiting":[...]}` front first | 404 unknown tool |

The 503 body is JSON like every other error. Wait lists live in memory and start empty after a restart.

## Final run

```bash
./scripts/reset.sh
mvn -q compile exec:java -Dexec.args="report 2026-10-15"
LANTERN_STORE=jdbc mvn -q compile exec:java -Dexec.args=serve      # then try the routes with curl
```

The report prints `Categories: 5`, `Overdue loans: 3`, `Top tool per category: 4` and `Members without loans: 1`
with their rows.

## Steps

1. `git switch main && git pull`, then `git switch -c m6/final-delivery`.
2. Collections, then streams, then routes. Run `./scripts/verify-milestones.sh 5` once to be sure nothing earlier
   broke.
3. In your work log, answer `SQL or stream:` (one report you would compute in SQL and one in a stream, and why),
   `Big-O:` (the cost of `WaitList.join` and of `positionOf`), and `Walkthrough:` (the order you will demo).
4. Commit in small steps and push the branch. Merge into `main` and push. Remove every `TODO LEARNER` marker.

## Check

```bash
./scripts/verify-milestones.sh 6
```

It runs every check, then resets a separate database under `target/`, prints the report for 2026-10-15, and
confirms that no `TODO LEARNER` marker is left.
