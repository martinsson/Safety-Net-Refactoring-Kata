# Junior section

At its October meeting the show committee decided to do more for young growers. The single
"junior award" of 5 per class has not encouraged many children to enter, so from the **2027
show** on, every class gets a **junior section** with its own prize money.

## What the committee decided

From the 2027 show (show year 2027 and later):

- In every class, the cards of growers **under 16** that are judged (not disqualified) are
  ranked **a second time, among juniors only**. They stay in the open class exactly as today:
  same score, same place, same prize money there.
- The junior ranking follows the same rules as the open class: same score, same order, same
  tie rule (same score and same judges' average share a rank, and the next rank is skipped).
- **Junior prize fund**: 10 for every junior card of the class, disqualified cards included
  (adult fees do not count). The fund is at least 20 and at most 60.
- **Two junior places**: the first receives 60% of the fund and the second 40%. Juniors who
  share a rank share the money of the places they occupy. Amounts are paid in multiples of
  50p (rounded down), and no junior prize may exceed **25**. A junior who scores under 10
  points wins nothing.
- Every junior prize winner receives a **junior certificate**. Junior certificates use the
  same numbering as the other certificates, with the prefix `UWJ` instead of `UW`
  (`UWJ-<year>-<number>`), and are numbered in the order of the board.
- There is no cup, no reserve and no side prize in the junior section.
- The **junior award** (5 for the best junior out of the money) is **discontinued** from the
  2027 show. The reserve of the open class is named as before, among all cards.

For the 2026 show and earlier, nothing changes.

## How it shows through `ShowJudge.judge`

- `Placing` gets one new method: **`String getSection()`**, which returns **`"OPEN"`** for
  every line that exists today (ranked lines, side-prize lines, disqualified cards, for every
  show year) and **`"JUNIOR"`** for the lines of the junior section.
- In each class, the junior lines come **right after the side-prize line** (or right after the
  ranked lines when there is no side prize) and **before the disqualified cards**. When no
  junior is judged in a class, the class has no junior line.
- A junior line has: `getCategory()` the class code (for example `"PUMPKIN"`), `getRank()` the
  rank among juniors (1, 2, ...), the card number and grower, `getScore()` the same score as in
  the open class, `getPrize()` the junior prize (0 if none), `getNote()` empty (`""`), and
  `getCertificate()` the junior certificate, or `null` when there is no junior prize.

## Examples

Pumpkins at the 2027 show, all cards registered on 15 January 2026 (well before the closing date). For simplicity each pumpkin scores
its weight (juniors get their usual +5 points).

1. Cards: A1 adult 200 kg, J1 age 12 150 kg, J2 age 14 120 kg, B1 adult 100 kg.
   Open pot: 20 + 10 + 10 + 20 = 60. Junior fund: 10 + 10 = 20. Board of the class:

   | Section | Rank | Card | Score | Prize | Note | Certificate |
   |---|---|---|---|---|---|---|
   | OPEN | 1 | A1 | 200.0 | 30.00 | Hedley Cup | UW-... |
   | OPEN | 2 | J1 | 155.0 | 18.00 | | UW-... |
   | OPEN | 3 | J2 | 125.0 | 12.00 | | UW-... |
   | OPEN | 4 | B1 | 100.0 | 0 | reserve | |
   | OPEN | 0 | A1 | 200.0 | 10.00 | heaviest exhibit | |
   | JUNIOR | 1 | J1 | 155.0 | 12.00 | | UWJ-... |
   | JUNIOR | 2 | J2 | 125.0 | 8.00 | | UWJ-... |

2. One junior alone: fund 10, raised to the minimum of 20; first place 12.00.
3. Nine juniors: fund 90, limited to 60; the first place would be 36 but is limited to
   **25.00**, the second gets 24.00, the others nothing.
4. Five juniors, three of them tied at the top: fund 50; the three tied juniors share
   60% + 40% = 50, that is 16.67 each, paid **16.50**; all three have junior rank 1, the next
   two have ranks 4 and 5 and no prize.
5. Four adults (200, 190, 180, 170 kg) and a junior (150 kg): at the **2026** show the junior
   gets the junior award (5.00, note `junior award`, section `OPEN`) and there is no junior
   line; at the **2027** show the junior gets nothing in the open class (no note), and a
   junior line with rank 1 and 12.00.
