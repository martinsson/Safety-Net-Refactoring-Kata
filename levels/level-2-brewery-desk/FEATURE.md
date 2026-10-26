# Quotes

Cafés and shops often call before ordering: "if I order this for next Tuesday, what will I get
and what will it cost me?". Today the brothers can only answer by placing the order, which takes
the beer from the stock, uses the allocation, uses an order number, records the order, charges
the account, books the courier and mails the customer. Undoing all that by hand is a pain and
some of it cannot be undone at all.

From now on the order desk must also give **quotes**.

## What a quote is

A quote answers the question "what would this order give, now, without placing it?".

- It gives **exactly the answer that placing the same order at the same moment would give**:
  the same delivered lines and amounts, the same back-orders, the same delivery date (and its
  "moved from" note), the same goods, discount, deposit, shipping, tax and total, the same status
  and the same payment terms. Everything that decides the answer counts as for an order: the
  stock in the cellar, what is left of the monthly allocation (including what the earlier lines
  of the same order take), the customer and their arrangements, the season, the courier tariff.
- It **places nothing and leaves no trace**: no stock is taken, no allocation is used, nothing is
  written in the cellar database (no order, no order line, no back-order), no order number is
  used, no account is charged, no courier is booked, no mail is sent. If the customer then
  places the order, they get exactly what they would have got without asking for a quote first,
  order number included.
- For deliveries by courier, the shipping comes from the courier's tariff, as for an order. A
  quote may ask the courier for its tariff (`quote` in Ferrow Connect): asking is not booking.
  So, like an order, a quote needs the Ferrow platform for zones 1 to 5, and fails the same way
  when the courier cannot be reached.
- A quote can be asked as many times as wanted.

## How it is asked

A new public method on `OrderDesk`, with the same parameters as `process`:

```java
public Confirmation quote(String customerCode, int zone, LocalDate deliveryDate, String... lines)
```

It returns a `Confirmation`, read with the usual getters:

- `getNumber()` is `null` (a quote has no number);
- `getStatus()`, `getDeliveryDate()`, `getLines()` and `getTotal()` give what the order would give;
- `getText()` is the text of the order's confirmation with two differences:
  - the first line is `Quote  issued <today>` (two spaces before `issued`, as in the first line of
    an order) instead of `Order <number>  issued <today>`;
  - there is no `Tracking` line.

  The `Payment` line is there when the order would be accepted (status 0 or 1), as for an order.

`process(...)` does not change.

## Examples

The examples assume the cellar as the desk sets it up on a new database, no order yet, and these
registered customers: `CAFE-0012`, a café; `SH-0101`, a shop with the mail address
`orders@shop101.example` and the payment account `SH0101-ACC`. `P-77` is not registered (a private
customer). `<today>` is the date of the day.

| Quote | Expected |
|---|---|
| `CAFE-0012`, zone 2, 2026-03-10, `4 CRATE TRI`, `2 CRATE DUB` | status 0; lines `4 x TRI crate 211.97`, `2 x DUB crate 92.74`; total 355.39; text below |
| `P-77`, zone 0, 2026-03-14, `30 BOTTLE TRI`, `30 BOTTLE DUB` | status 1 (a private customer gets 48 bottles a month: the second line is cut to 18); lines `30 x TRI bottle 72.00`, `18 x DUB bottle 37.80`, `BACKORDER 12 x DUB bottle`; total 137.65; text below |
| `SH-0101`, zone 4, 2026-05-23, `4 CRATE BLO`, `1 KEG DUB` | status 0; delivery 2026-05-26; lines `4 x BLO crate 167.81`, `1 x DUB keg 120.00`; total 426.00; nothing charged to `SH0101-ACC`, no courier booked, no mail sent; text below |
| `SH-0101`, zone 2, 2026-05-26, `3 BOTTLE ALE` | status 2, no lines, total 0.00, as an order would be refused |

```
Quote  issued <today>
Delivery 2026-03-10
4 x TRI crate 211.97
2 x DUB crate 92.74
Goods 304.71 Discount 0.00 Deposit 32.40
Shipping 0.00 Tax 18.28 TOTAL 355.39
Payment 30 days
```

```
Quote  issued <today>
Delivery 2026-03-14
30 x TRI bottle 72.00
18 x DUB bottle 37.80
BACKORDER 12 x DUB bottle
Goods 109.80 Discount 0.00 Deposit 4.80
Shipping 0.00 Tax 23.06 TOTAL 137.65
Payment on delivery
```

```
Quote  issued <today>
Delivery 2026-05-26 (moved from 2026-05-23)
4 x BLO crate 167.81
1 x DUB keg 120.00
Goods 287.81 Discount 0.00 Deposit 51.60
Shipping 65.40 Tax 21.19 TOTAL 426.00
Payment charged to account
```

After these four quotes, placing `SH-0101`, zone 4, 2026-05-23, `4 CRATE BLO`, `1 KEG DUB` gives
the order `SB<year>-0001` with the same lines and total, charges 426.00 to `SH0101-ACC`, books the
courier and mails the confirmation, which ends with the lines `Payment charged to account` and
`Tracking TRK-0001` (with the Ferrow test kit, which numbers bookings from `TRK-0001`).
