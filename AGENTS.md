# Project agent memory

- This is a Java 25/Maven learner starter using plain JDBC and embedded H2; the authoritative workflow and learner/supplied boundary are in `README.md`.
- `mvn -q verify` is the starter health command and intentionally excludes `*CompletionTest`; `mvn -q -Pcompletion test` is the learner contract.
- Keep learner TODOs only in the files listed in `README.md`. Public tests, support code, seed data, and scripts are supplied contract files.
- `docs/milestones/manifest.tsv` is the authoritative milestone list (guide, learner files, TODO ids, test classes); `MilestoneManifestTest` keeps it consistent with README links and completion tests. `./scripts/verify-milestones.sh` reads it; solved milestone overlays for its `states` mode must never be committed.
- `docs/objective-map.md` is learner-facing: selected outcomes, prerequisites, and exclusions only. Keep dependency versions in `pom.xml` and keep source/review provenance, repository links, and private paths out of learner docs.

## Maintaining this file

Keep only knowledge useful to almost every future session. Point to authoritative files instead of duplicating them,
and preserve this concise self-governance section when editing.
