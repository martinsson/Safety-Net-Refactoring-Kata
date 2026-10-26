# Mixed crates

For years the abbey shop has sold a "mixed crate" over the counter: one crate, three beers.
Cafés and shops keep asking for it on their orders. From now on the order desk must accept it.

## What a mixed crate is

A mixed crate is a crate of 24 bottles holding three beers, 8 bottles of each:

- from November to January: 8 TRI, 8 DUB and 8 NOX;
- the rest of the year: 8 TRI, 8 DUB and 8 BLO.

As for everything else, the month is the month of the delivery date, after any move of the
delivery to the next open day.

## How it is ordered

On an order line, like any other crate, with the beer code `MIX`:

- `"3 CRATE MIX"` (or `"3 CASE MIX"`; the unit may be written in any case, as today).
- Only crates can be mixed: `MIX` with any other unit (`"6 BOTTLE MIX"`, `"1 KEG MIX"`) is left
  out of the confirmation, as an unknown beer is today.

## Rules

A mixed crate is handled exactly like a crate holding the same 24 bottles would be:

- **Price**: the crate price of its 24 bottles (the bottle prices of its content, with the
  usual crate price for 24 bottles). The volume discount of crates applies to the number of
  mixed crates on the line.
- **Discount for crates of several beers**: a mixed crate counts as its three beers.
- **Deposit, shipping, returned empties**: as for any crate.
- **Monthly allocation**: 24 bottles per mixed crate, as for any crate.
- **Stock**: every mixed crate takes 8 bottles of each of its three beers. A mixed crate can only
  be delivered when all three beers are available for that customer (the bottles kept for the
  cafés are kept from mixed crates too). Mixed crates are delivered in whole crates; the rest is
  back-ordered.
- Everything else (customer arrangements, brothers' discount, tax, payment terms, order number,
  status) works as for any other order.

## What the confirmation shows

Nothing new in the public API: `OrderDesk.process(...)` is called as today and the
`Confirmation` has the same getters. The mixed crates appear in `getLines()` and in `getText()`,
in the usual format:

- delivered: `"<n> x MIX crate <amount>"`, for example `"2 x MIX crate 94.21"` (amount with two
  decimals and a dot, as other lines);
- back-ordered: `"BACKORDER <k> x MIX crate"`.

Goods, discount, deposit, shipping, tax and total include the mixed crates, in `getTotal()` and
in the text as today.

## Examples

| Customer | Zone | Delivery | Lines | Expected |
|---|---|---|---|---|
| `CAFE-0012`, `CAFE` | 1 | 2024-03-12 | `2 CRATE MIX` | status 0; lines `2 x MIX crate 94.21`; text `Goods 94.21 Discount 1.88 Deposit 10.80` and `Shipping 4.50 Tax 5.81 TOTAL 113.44`; total 113.44 |
| `CAFE-0013`, `CAFE` | 1 | 2024-12-10 | `2 CRATE MIX` | lines `2 x MIX crate 113.34`; text `Goods 113.34 Discount 2.27 Deposit 10.80` and `Shipping 4.50 Tax 6.93 TOTAL 133.30` |
| `SH-0101`, `SHOP` | 2 | 2024-04-09 | `12 CRATE MIX`, `1 CRATE TRI` | lines `12 x MIX crate 565.25`, `1 x TRI crate 52.99`; text `Goods 618.24 Discount 34.97 Deposit 70.20` and `Shipping 0.00 Tax 35.00 TOTAL 688.47` |
| `P-77`, private | 0 | 2024-05-14 | `3 CRATE MIX` | status 1; lines `2 x MIX crate 94.21`, `BACKORDER 1 x MIX crate`; total 122.50 |
| `SH-0105`, `SHOP` | 1 | 2024-07-09 | `2 BOTTLE MIX`, `1 KEG MIX` | status 2, no lines, total 0.00 (as today) |

The examples assume a cellar with enough stock and allocations not yet used that month.
