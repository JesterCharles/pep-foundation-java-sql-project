# Milestone 1: Team workflow (Week 1)

Due Friday of Week 1. It checks what was taught through Wednesday of Week 1: the shell, Git and AI-assistant
practices.

You join the Lantern Tool Library team. Before anyone writes Java, the team needs a safe repository, a small shell
tool, user stories, and a habit of reviewing assistant output with evidence.

## What you hand in

| TODO | File | What to do |
| --- | --- | --- |
| 1A | `.gitignore` | Ignore `.env` and every `.env.*` file, but keep `.env.example` tracked. |
| 1B | `.env.example` | List `LANTERN_PORT=7070` and `LANTERN_ADMIN_TOKEN=` (empty: never a real token). |
| 1C | `bin/loan-summary.sh` | Write the loan-log summary below and make the script executable. |
| 1D | `WORKLOG.md` | Your name, three user stories, and your review of the assistant's proposal. |
| 1W | `WORKLOG.md` | Your `### M1` entry. |

Checks: `LoanSummaryScriptCompletionTest`, `SecretHygieneCompletionTest`, `WorklogStoriesCompletionTest`,
`GitHistoryCompletionTest` and `M1DeliveryCompletionTest`.

**Timing.** Do all of the work below with the shell and Git on Days 1-3. The check itself is a Java program, so it
runs once you have installed Java and Maven on Thursday of Week 1: then run `./scripts/prefetch.sh` once while online,
and run the check before Friday's deadline.

## 1. Look around with the shell

From the repository root, explore before you change anything:

```bash
pwd
ls -la
cat README.md | head -20
grep -n "CHECKOUT" fixtures/loan-log.txt | head
grep -c " RETURN " fixtures/loan-log.txt
```

## 2. Keep secrets out of Git (1A, 1B)

1. Finish TODO 1A in `.gitignore`, then create your own local settings file: `cp .env.example .env`.
2. Finish TODO 1B in `.env.example`. Put a made-up token only in your local `.env`.
3. Confirm: `git status` does not list `.env`, and `git check-ignore -v .env` names the rule that ignores it.

`.env` must never appear in any commit, not even one you later delete. If you commit it by accident, ask your trainer
before you push.

## 3. The loan summary script (1C)

`./bin/loan-summary.sh [LOG]` reads a loan log (default `fixtures/loan-log.txt`) and prints exactly:

```text
Loan log: fixtures/loan-log.txt
Checkouts: 14
Returns: 10
Late returns: 3
Other events: 4
Location: (not set)
```

Rules:

- A checkout is a line containing ` CHECKOUT ` (upper case, spaces on both sides); a return contains ` RETURN `.
  The word "checkout" inside a NOTE is not a checkout.
- A late return is a return line that contains `late` in any letter case (use `grep -i`).
- Other events are lines that are neither checkouts nor returns (use `grep -v`).
- Count with pipes and `wc -l`. A count of zero is normal, not an error: `grep` exits with status 1 when nothing
  matches, which stops a `set -e` script unless you handle it (for example `(grep ... || true) | wc -l`).
- `Location:` shows the exported `LANTERN_LOCATION` variable, or `(not set)` when it is missing.
  Try `export LANTERN_LOCATION="Riverside branch"` and run the script again.
- A missing log file prints `No such log file: <path>` to standard error, prints nothing else, and exits with
  status 2. This check is already written for you at the top of the script (an `if` block); keep it above your
  summary.

Make the script executable with `chmod 750 bin/loan-summary.sh` and commit that change too; Git stores the
executable bit.

## 4. Stories and the assistant review (1D)

1. In `WORKLOG.md`, write your name and at least three stories as `As a <role>, I want <goal>, so that <benefit>`.
   Each story needs at least one testable `Acceptance:` line.
2. Read `review/assistant-proposal.md`. Compare every claim with the numbered diff. Decide `approve` or
   `request changes`, and write at least two `Evidence:` lines that each cite a diff line as `line N`. Never paste a
   real secret into an assistant; note in your entry what context you shared and what you removed.

## 5. Git: branches, a conflict and a revert

Do all of this in your own repository. Your remote is usually called `origin`.

```bash
git switch main
git switch -c m1/team-workflow
# ...edit, then commit small steps; three or more focused commits, for example:
git add .gitignore .env.example && git commit -m "Ignore local settings and document them"
git add bin/loan-summary.sh && git commit -m "Summarize the loan log"
git add WORKLOG.md && git commit -m "Add user stories and proposal review"
git push -u origin m1/team-workflow
git switch main
git merge m1/team-workflow
git push -u origin main
```

Do the conflict and revert exercises below only after you have merged `m1/team-workflow` into `main`: the check
counts your work from your first Milestone 1 change onwards, so exercises done earlier are not seen.

Conflict exercise: change the same line on two branches, then merge both.

```bash
git switch -c m1/notes-a main      # edit the M1 "Blockers:" line in WORKLOG.md one way, commit
git switch -c m1/notes-b main      # edit the same line another way, commit
git switch main
git merge m1/notes-a
git merge m1/notes-b               # Git reports a conflict
# open WORKLOG.md, keep the text you want, delete the <<<<<<< ======= >>>>>>> marker lines
git add WORKLOG.md
git commit                         # finishes the merge
```

Revert exercise: make a small deliberate mistake on `main` (for example add a wrong line to `README.md`), commit it,
then undo it with `git revert HEAD`. Revert keeps history honest: it adds a new commit instead of rewriting old ones.

Finish with `git push`. Useful views: `git log --oneline --graph --all`, `git diff`, `git status`.

## 6. Work log (1W)

Fill in `### M1` in `WORKLOG.md`: the stand-up (`Yesterday`, `Today`, `Blockers`) and your pull-request notes
(`Story`, `Security check`, `AI used`, `I verified`), plus `Context shared` (what you gave an assistant) and
`Redacted` (what you removed first). Each value needs at least three words of your own. Commit and push it.

## Check

```bash
./scripts/verify-milestones.sh 1
```
