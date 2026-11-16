# Milestone 3: Domain model (Week 3)

Due Friday of Week 3. It checks what was taught from Thursday of Week 2 through Wednesday of Week 3: classes and
objects, enums, null handling, equality, inheritance, interfaces, composition, records and functional interfaces.

Now the library's ideas become objects that protect their own rules: a member always has a valid email, a tool can
only move between allowed states, and each membership tier has its own fee policy.

## What you hand in

All files are in `src/main/java/com/pep/toolshare/domain/`.

| TODO | File | What to do |
| --- | --- | --- |
| 3A | `Member.java` | Constructor validation, encapsulated fields, `changeEmail`, a `toString` without the email. |
| 3B | `ToolCategory.java` | Enum constants with fields, a constructor and accessors; `fromCode` and `dueDate`. |
| 3C | `Lendable.java`, `Tool.java` | Abstract base class and the `Tool` subclass with its status changes. |
| 3D | `ToolKit.java` | A kit that *has* tools (composition) and delegates to them. |
| 3E | `Loan.java` | A record with a compact constructor and "copy with changes" methods. |
| 3F | `StandardFeePolicy.java`, `GraceFeePolicy.java`, `FeePolicies.java` | Interface implementations, a wrapper, a factory. |
| 3G | `ToolFilters.java` | `Predicate<Tool>` rules written as anonymous classes, and a loop that applies one. |
| 3H | `Receipts.java` | A plain-text receipt built with `StringBuilder`. |
| 3W | `WORKLOG.md` | Your `### M3` entry. |

Checks: `MemberCompletionTest`, `ToolCatalogCompletionTest`, `LoanAndFeesCompletionTest` and
`M3DeliveryCompletionTest`.

Each class comment lists its rules. `Tool.java` draws the allowed status changes; `ToolCategory.java` lists every
constant's values; `Receipts.java` shows a complete example receipt.

## Steps

1. `git switch main && git pull`, then `git switch -c m3/domain-model`.
2. Start with `Member`: reject `null` with `NullPointerException` (`Objects.requireNonNull`) and bad values with
   `IllegalArgumentException`. The same rule applies to the constructors of `Tool`, `ToolKit`, `Loan`,
   `StandardFeePolicy` and `GraceFeePolicy`. Fields are `private final` unless they really change.
3. `Lendable` is abstract: `Tool` and `ToolKit` extend it and call `super(code, name)` first. A kit holds a list of
   tools; it is not a tool itself.
4. `FeePolicy` is an interface. `GraceFeePolicy` wraps another policy instead of extending it. `FeePolicies.forTier`
   chooses the policy so callers never name the classes.
5. Use `==` for enum constants and `equals` for strings; know why.
6. Run `mvn -q -Pcompletion test -Dtest='MemberCompletionTest,ToolCatalogCompletionTest,LoanAndFeesCompletionTest'`
   while you work. `mvn dependency:tree` shows which libraries Maven resolved for the project.
7. Commit in small steps and push the branch. Fill in `### M3` in `WORKLOG.md`, commit, merge into `main`, then
   push both: `git push` on `main` and `git push origin m3/domain-model`.

## Check

```bash
./scripts/verify-milestones.sh 3
```
