# Saint Brevin checkout

Welcome to the team. You are joining the small team that maintains the checkout of the
Saint Brevin abbey brewery. The abbey brews four beers in small batches and sells them by the
bottle, the crate and the keg to cafés, shops and private customers. The checkout places an order:
it sets the beer aside in the cellar (stock and monthly allocations, kept in the abbey's TallowDB
database), records the order, takes the payment, books the courier and mails the confirmation
through the Ferrow platform, and returns a receipt. It has been in service for many years and the
brothers rely on it every day.

The business has asked for a change: it is described in [FEATURE.md](FEATURE.md).
Your job is to deliver it.

## How we change code here

We follow **3P: Protect, Prepare, Produce**. Read
[docs/3p-protect-prepare-produce.md](docs/3p-protect-prepare-produce.md) before anything else.
More reading in [docs/references.md](docs/references.md).

Work in this order:

1. **Understand** the existing code and what it does. The rules are described from the
   business side in [docs/checkout.md](docs/checkout.md); the code is the reference when
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
- Other systems call **`com.stbrevin.ordering.Checkout`**: they create it with `new Checkout()`
  and call `place(String customerCode, LocalDate deliveryDate, String... lines)`. Keep this class,
  its package and its public signature (and the public parts of `Receipt`, including the exact
  text returned by `getText()`) compatible: you may add to them, but do not rename, remove or
  change what exists. The office and the cellar also read and write the `cellar` database (tables
  `customers`, `stock`, `allocations`, `orders`, their columns and values) and the customers read
  the mails: keep them as they are too. Everything behind it is yours to change.
- The store and the Ferrow platform are reached through the suppliers' libraries in `java/lib`
  (documented in `docs/tallowdb.md` and `docs/ferrow-connect.md`). You cannot change these
  libraries; you may wrap them.
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
