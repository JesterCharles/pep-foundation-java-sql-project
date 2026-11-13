# Milestone 2: Lending basics (Week 2)

Due Friday of Week 2. It checks what was taught from Thursday of Week 1 through Wednesday of Week 2: Java basics,
methods, recursion, arrays, lists and maps.

The desk needs reliable small calculations before any bigger feature can use them: late fees, money formatting, tool
codes, searching and counting. Every method is in one class, and later milestones call them.

## What you hand in

| TODO | File | What to do |
| --- | --- | --- |
| 2A | `src/main/java/com/pep/toolshare/basics/LendingMath.java` | `lateFeeCents`, `membershipTier`: arithmetic, `if`/`switch`. |
| 2B | same | `formatCents`, `parseCents`: division, remainder, loops over characters. |
| 2C | same | `checkDigit` (recursive), `normalizeToolCode`, `isValidToolCode`: strings and recursion. |
| 2D | same | `indexOfToolNumber` (binary search), `reverseInPlace` (two indexes). |
| 2E | same | `overdueCodes` (`ArrayList`), `countByCategory` (`HashMap`). |
| 2W | `WORKLOG.md` | Your `### M2` entry. |

Checks: `LendingMathCompletionTest` and `M2DeliveryCompletionTest`.

Each method's comment states its exact rule and examples. Read the matching tests in
`src/test/java/com/pep/toolshare/LendingMathCompletionTest.java` too: they are the specification.

## Steps

1. `git switch main && git pull`, then `git switch -c m2/lending-basics`.
2. Work one TODO at a time. Run only this milestone's tests while you work:
   `mvn -q -Pcompletion test -Dtest=LendingMathCompletionTest`.
3. Money is a whole number of cents in a `long`. Watch for overflow when you multiply: an `int` times an `int` is
   still an `int`.
4. `checkDigit` must call itself. Write the base case first, then trace `checkDigit(1009)` on paper.
5. Step through `indexOfToolNumber` in your IDE's debugger once: set a breakpoint, watch `low`, `high` and `mid`.
6. Commit after each TODO with a message that says what changed, then push the branch:
   `git push -u origin m2/lending-basics`.
7. Fill in `### M2` in `WORKLOG.md` (stand-up and pull-request notes), commit, merge into `main`, and push both:

```bash
git switch main
git merge m2/lending-basics
git push
```

## Check

```bash
./scripts/verify-milestones.sh 2
```
