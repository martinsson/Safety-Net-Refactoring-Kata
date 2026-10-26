# Upper Wexcombe Show Judging

Welcome to the team. Thornfield Soft looks after the judging program of the Upper Wexcombe Summer Fair, a village show where growers compete with giant vegetables (pumpkins, marrows, onions and carrots). On show day the secretary types in every entry card, with the measurements and the judges' marks; the program produces the results board: the ranking of each class, the prize money, the cups and side prizes, the certificate numbers and the list of disqualified cards. The board is pinned up in the marquee, read out by the steward, and used by the treasurer to pay the prizes. The program has served the show for many years and has been adjusted after almost every season.

The business has asked for a change: it is described in [FEATURE.md](FEATURE.md).
Your job is to deliver it.

## How we change code here

We follow **3P: Protect, Prepare, Produce**. Read
[docs/3p-protect-prepare-produce.md](docs/3p-protect-prepare-produce.md) before anything else.
More reading in [docs/references.md](docs/references.md).

Work in this order:

1. **Understand** the existing code and what it does. The rules are described from the
   business side in [docs/vegetable-show.md](docs/vegetable-show.md); the code is the reference when
   they disagree.
2. **Choose the level at which to test**, so that your tests protect the refactoring you are
   about to do (and do not break because of it).
3. **Protect.** Write the tests that will let you change the code without fear. Quick and dirty
   tests are fine at this stage.
4. **Check your tests with mutation testing** (PIT, see below) and strengthen them where it
   shows behaviour that is not really protected.
5. **Prepare.** Look at what the feature will need, and refactor towards it, tests green at
   every step, until adding the feature feels easy: a small, obvious change, without copying
   existing logic.
6. **Improve the tests** now that the design allows it: replace fragile or clumsy high-level
   tests with clean, focused ones where it makes sense.
7. **Produce.** Implement the feature, test first.

## Rules of the project

- The existing tests pass. They were written by different people over the years.
- Other systems call **`com.thornfieldsoft.vegshow.ShowJudge`, method `List<Placing> judge(List<Entry> entries, int year)`**. Keep this class, its package and its public
  signature (and the public parts of `Entry` (the entry card) and `Placing` (a line of the results board)) compatible: you may add to them, but do not
  rename, remove or change what exists. Everything behind it is yours to change.
- The current behaviour is what users rely on, including the parts that look odd. Do not change
  it unless FEATURE.md asks for it.
- Work in git, with small commits and messages that say what you did and why.
- Keep a `JOURNAL.md` at the root, updated as you go: the decisions you take and why (which
  test level and why, what mutation testing revealed (with the figures PIT prints), what you changed because of it, how you
  refactored, what you did with the tests afterwards).

## Commands

All commands run from the `java/` folder. You need JDK 21+ and Maven 3.9+.

| What | Command | Report |
|---|---|---|
| Build and run the tests (with coverage) | `mvn test` | `target/site/jacoco/index.html` |
| Mutation testing | `mvn test-compile org.pitest:pitest-maven:mutationCoverage` | `target/pit-reports/index.html` |

The mutation testing run prints a summary at the end (number of mutations, number killed, test
strength). `target/pit-reports/mutations.xml` lists every surviving mutant with its line.
