# Safety Net Kata

A training kata on **protecting a change with AI**. Participants (developers working with an AI
coding agent) receive a small, old-style Java codebase with weak tests and a feature to add.
They apply **3P, Protect → Prepare → Produce** ([methodology](docs/3p-protect-prepare-produce.md)):

1. **Understand** the existing code (functional analysis).
2. **Choose the level at which to test**, to best protect the upcoming refactoring.
3. **Write the tests (Protect).** Quick-and-dirty characterization tests are fine here.
4. **Self-critique with mutation testing** (PIT) and strengthen the tests accordingly.
5. **Refactor (Prepare)** until the feature feels easy.
6. **Improve the tests after refactoring.** Pull fragile high-level tests down to clean,
   focused ones.
7. **Produce** the feature (TDD on the prepared design).

## Repository layout

```
docs/          shared, language-independent material given to every participant
levels/        one folder per level and domain: README, FEATURE, domain docs, code per language
trainer/       facilitator material. NEVER give it to participants (or to trial agents)
tools/         measurement and packaging scripts
```

Each level folder holds one sub-folder per language (`java/` today; `python/`,
`typescript/`... can be added next to it later).

## Running a session

1. Pick a level and build the participant handout. **Never hand out the repository itself**:
   it contains `trainer/`.

   ```
   tools/make-participant-copy.sh level-1-<domain> /path/to/handout
   ```

   The copy contains the level README (the participants' instructions), `FEATURE.md`, `docs/`
   (domain description + the 3P method + references) and the code. The script runs
   `tools/check-neutrality.sh` on the result.

2. Participants need JDK 21+ and Maven 3.9+. Everything runs offline once Maven has downloaded
   the dependencies (`mvn test` once before the session if the network is unreliable).

3. Debrief with `trainer/level-1-<domain>/CORRIGE.md` (what is wrong with the code, which test
   level works best, what the naive feature looks like).

4. To check a group's result against the hidden acceptance tests and the golden master:

   ```
   tools/run-acceptance.sh level-1-<domain> /path/to/their/java
   tools/measure.sh /path/to/their/java
   ```

## Tools

| Script | Purpose |
|---|---|
| `tools/measure.sh <java-project>` | LOC, line/branch coverage (JaCoCo), mutation score (PIT), duplication (PMD CPD), cyclomatic complexity and method size (PMD) |
| `tools/make-participant-copy.sh <level> <dest>` | participant handout, without trainer content |
| `tools/check-neutrality.sh <dir>` | scans a handout for words that would reveal the exercise or hint at the defects |
| `tools/run-acceptance.sh <level> <java-project>` | hidden feature tests + golden-master regression suite |
| `tools/run-trial.sh <level> <n>` | isolated handout as a fresh git repo, for a trial with an AI agent |
| `tools/collect-trial.sh <level> <n> <dir>` | copies a trial's history, journal and metrics into `trainer/` |

Requirements for the tools: bash, Maven, JDK 21, `cloc`, `python3`, `rsync`, `curl` (PMD is
downloaded on first use into `tools/.cache/`).

## Status

Level 1 only, Java only. See `trainer/REPORT.md` for progress and `trainer/COMPARISON.md` for the
choice between candidate domains.

The method is by Johan Martinsson; see [docs/references.md](docs/references.md).
