# Itemised allowance (circular 2027/03)

## Why

Agents keep phoning the posting office to ask how their allowance was arrived at, and the
ombudsman asked us to make the calculation transparent. From now on, every granted allowance comes
with an **itemised statement**: one line per element of the calculation, showing how much each
element contributed, down to the granted amount. Payroll will print it as an annex to the notice.

## What we need

The notice returned by `AllowanceDesk.process(TransferFile)` gets a new method:

```java
public String getBreakdown()
```

- For a **rejected** file (or a transfer within the same locality), it returns an empty string `""`.
- For a **granted** file, it returns the statement: lines `<label>: <amount>`, separated by `\n`,
  with no `\n` after the last line.

The text of the notice (`getText()`) and all the other values of the notice stay exactly as they
are today.

### Lines, in this order

| Label | What it shows |
|---|---|
| `Base allowance` | the base amount of the agent's category. Always present. |
| `Household supplement` | what the household (partner, children) adds to the base. |
| `Distance supplement` | the distance supplement, at the full rate. |
| `Reduction for transfer at own request` | negative: the part of the base, household and distance supplements that is not paid when the agent asked for the transfer (they are paid at 60%). |
| `Winter installation supplement` | the winter supplement for Harrow Gap. |
| `Housing shortage supplement` | the housing shortage supplement actually added. |
| `Remote outpost supplement` | what the remote outpost coefficient adds. |
| `Seniority supplement` | what seniority adds. |
| `Return bonus` | the return bonus. |
| `Second transfer within twelve months` | negative: the half that is not paid on a second transfer. |
| `Rounding` | what is needed for the lines above to add up to the amount after rounding to a multiple of five. |
| `Ceiling` | negative: the part above the ceiling. |
| `Minimum top-up` | what is added to reach the minimum allowance. |
| `Total` | the granted amount, equal to `getAmount()`. Always present. |

A line whose amount is zero is not printed (except `Base allowance` and `Total`). Each line is
rounded to the nearest cent, and the lines always add up exactly to the total: the `Rounding` line
takes up the cents.

Amounts are written like in the notice: two decimals, a space between groups of three digits, and
a minus sign in front of negative amounts (`1 635.00`, `-592.20`, `90.00`).

## Examples

A category B agent sent by the service from Brennick Ford to Castel Varro, request of 1 May 2019,
with a partner and two children, 22 years of service:

```
Base allowance: 940.00
Household supplement: 376.00
Distance supplement: 243.10
Seniority supplement: 124.73
Rounding: -3.83
Total: 1 680.00
```

A category C agent moving at their own request from Port Aldry to the Isle of Quell, request of
1 June 2020, one child, 25 years of service:

```
Base allowance: 760.00
Household supplement: 76.00
Distance supplement: 281.35
Reduction for transfer at own request: -446.94
Remote outpost supplement: 201.12
Seniority supplement: 50.00
Rounding: -1.53
Total: 920.00
```

A category A agent sent to Harrow Gap from Brennick Ford, request of 1 December 2021, partner and
four children, 30 years of service:

```
Base allowance: 1 150.00
Household supplement: 805.00
Distance supplement: 466.25
Winter installation supplement: 150.00
Remote outpost supplement: 514.25
Seniority supplement: 246.84
Rounding: -2.34
Ceiling: -330.00
Total: 3 000.00
```

A category B agent (partner, 15 years of service) sent from Brennick Ford to Saltwick Halt
(request of 1 March 2021), then sent back to Brennick Ford (request of 1 September 2021). The second
notice:

```
Base allowance: 940.00
Household supplement: 188.00
Seniority supplement: 56.40
Second transfer within twelve months: -592.20
Rounding: -2.20
Total: 590.00
```

A category C agent sent from Oldmere to Port Aldry (10 January 2020) and back four months later
(10 May 2020). The second notice:

```
Base allowance: 760.00
Second transfer within twelve months: -380.00
Minimum top-up: 20.00
Total: 400.00
```
