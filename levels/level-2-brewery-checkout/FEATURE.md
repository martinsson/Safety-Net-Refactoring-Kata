# Collecting returned kegs, and no more half-placed orders

Two requests from the brothers, which come together.

**Returned kegs.** Cafés and shops give back their empty kegs on their orders (`2 RETURN KEG`).
The delivery van can no longer take them back, so from now on the courier collects them with a
separate pick-up.

**Half-placed orders.** With one more step in placing an order, the office does not want any
more of the half-placed orders it spends its mornings on: orders waiting for a payment or for a
courier that nobody remembers to book, customers charged twice, customers charged for an order
that was never saved, orders delivered although the shop's system was told that placing them
failed. The rule from now on is simple:

> An order is either placed completely, or it leaves no trace, except one mail telling the
> customer that it could not be placed.

This is already what happens today when a payment is declined. We want the same for every
problem.

## 1. Pick-up of returned kegs

When an order that is placed has returned kegs (`RETURN KEG` lines with a quantity above zero)
and is delivered by the courier (customer zone 1 to 5), the abbey books a pick-up with the
courier, after booking the delivery:

- reference: the order number followed by `-RET`, for example `SB-00007-RET`;
- zone: the customer's zone; date: the delivery date (after any move to an open day);
- parcels: the number of kegs returned on the order, all `RETURN KEG` lines together.

The pick-up's tracking id is recorded in the order's row in `orders`, in a new column `pickup`.
The confirmation mail gets one more line, just after the courier line:
`Pick-up of empty kegs: <number of kegs>, tracking <tracking id>`, for example
`Pick-up of empty kegs: 2, tracking TRK-0002`.

No pick-up for customers who collect their order at the abbey (zone 0): they bring their kegs
back themselves. No pick-up for returned crates. Prices, the credit for returned kegs and the
receipt do not change.

## 2. An order is placed completely, or not at all

Placing an order goes through these steps, in this order:

1. set the beer aside: take it from the stock and count it in the monthly allocation;
2. record the order in `orders`;
3. take the payment, for customers who pay at order, when there is something to pay;
4. book the delivery with the courier (zone 1 to 5);
5. book the pick-up of returned kegs (part 1);
6. send the confirmation mail.

The courier is never booked before the customer has paid.

**Placed.** The order is placed when every step succeeded. Everything is as today: receipt
`PLACED` with its number, lines and amounts, row `PLACED` in `orders` with the payment and tracking
ids, confirmation mail. `getMessage()` is empty (`null`) for a placed order: the statuses
`TO_CHARGE` and `TO_BOOK` are no longer used, an order never waits for a payment or a courier.

**Refused, as today.** An unknown customer, or an order where nothing can be delivered, is
refused as today: status `REFUSED`, nothing recorded, no mail.

**Declined, as today.** A declined payment gives what it gives today: status `DECLINED`, message
`payment declined`, nothing kept, one mail `Order not placed`.

**Failed.** Any other problem in one of the steps makes the order fail. A problem is: the Ferrow
platform cannot be reached, or does not answer in time, for the payment, a courier booking or the
confirmation mail; or one of the abbey's records (`stock`, `allocations`, `orders`) cannot be
written. When an order fails:

- `place(...)` returns normally, it never throws because of the platform or the records. The
  receipt is the one a declined payment gives today, with status `FAILED` and message
  `order not placed`: no number, no lines, all amounts 0.00, and `FAILED (order not placed)` on
  the last line of `getText()`;
- stock and allocations are as they were before the order;
- there is no row for it in `orders`;
- every payment taken for it is refunded and every booking made for it (delivery and pick-up)
  is cancelled;
- the customer receives one mail, the same as for a declined payment today: subject
  `Order not placed`, body `We could not place your order for delivery on <delivery date>.`, a
  line break, `Please contact the abbey office.` and a line break.

Some details the office agreed on:

- **A payment is never attempted twice for the same order.** When the payment platform does not
  answer in time, we cannot know whether it took the payment: the order fails. If the platform
  did take it, that one payment stays on the platform; the finance office refunds those from the
  platform's daily report (not part of this change). The same goes for a courier booking that did
  not answer in time: it stays with the courier and the office cancels it.
- **If a refund or a cancellation fails** while the order is being undone, that payment or
  booking stays as it is; everything else is undone, the failure mail is sent and the order is
  `FAILED`.
- **If the confirmation mail cannot be sent**, the order fails like for any other step. When the
  mail platform did not answer in time, the confirmation may have reached the customer anyway:
  the customer then receives both mails. We accept that.
- **If the failure mail cannot be sent either**, nothing more is done; the order is `FAILED`.
- **The courier's price does not change anything.** When the courier cannot give a price, the
  flat rate used today still applies, as today: an order never fails because of the price.

## Examples

Customers used below: `CAFE-0012`, a café in zone 1 paying on invoice; `CAFE-0031`, a café that
collects at the abbey (zone 0); `SH-0101`, a shop in zone 2 paying on invoice; `P-310`, a private
customer in zone 3 who pays at order. Each example is the first order of the day (number
`SB-00001`, first payment `ch_0001`, first booking `TRK-0001`) in a well-stocked cellar.

| # | Order | What happens | Today | From now on |
|---|---|---|---|---|
| 1 | `CAFE-0012`, 2026-04-14, `3 CRATE TRI`, `2 RETURN KEG`, `1 RETURN CRATE` | all goes well | placed, total 154.62, delivery booked | the same, plus a pick-up `SB-00001-RET`, zone 1, 2026-04-14, 2 parcels (tracking `TRK-0002`, after the delivery `TRK-0001`); row with `pickup` = `TRK-0002`; the mail ends with `Courier tracking TRK-0001` then `Pick-up of empty kegs: 2, tracking TRK-0002` |
| 2 | `SH-0101`, 2026-05-12, `2 CRATE DUB`, `1 RETURN KEG`, `3 RETURN KEG` | all goes well | placed, total 15.82 | one pick-up `SB-00001-RET` of 4 parcels |
| 3 | `CAFE-0031`, 2026-04-14, `3 CRATE TRI`, `1 KEG BLO`, `2 RETURN KEG` | all goes well | placed, collected at the abbey | unchanged: no pick-up |
| 4 | `P-310`, 2026-03-12, `2 CRATE TRI`, `6 BOTTLE DUB` (total 189.37) | the payment platform cannot be reached | `PLACED`, message `payment pending`, row `TO_CHARGE`, delivery booked, confirmation mail | `FAILED`, `order not placed`; stock and allocation untouched; no row, no booking; one mail `Order not placed` |
| 5 | same | the payment platform takes the payment but does not answer in time | `PLACED`, `payment pending`, the customer charged twice (2 × 189.37) | `FAILED`; one payment of 189.37 stays on the platform (only one); no booking; one mail `Order not placed` |
| 6 | same | the payment platform does not answer the first time, then works again | `PLACED`, paid at the second attempt | `FAILED`, nothing charged, one mail `Order not placed` |
| 7 | same | the courier cannot be reached to book the delivery (its tariff still answers) | `PLACED`, `delivery to be arranged`, paid, row `TO_BOOK` | `FAILED`; the payment of 189.37 is refunded; one mail `Order not placed` |
| 8 | same | the courier cannot be reached to book the delivery, and refunds cannot be made | as 7 | `FAILED`; the payment of 189.37 stays; one mail `Order not placed` |
| 9 | same | the payment is declined | `DECLINED`, `payment declined`, nothing kept, one mail `Order not placed` | unchanged |
| 10 | same | the `allocations` table cannot be written | `PLACED`, but the bottles are not counted in the allocation | `FAILED`; stock untouched; one mail `Order not placed` |
| 11 | `P-310`, 2026-03-12, `2 KEG DUB`, `1 RETURN KEG` (total 373.04) | the pick-up cannot be booked | (no pick-up today) | `FAILED`; payment refunded, delivery booking cancelled; one mail `Order not placed` |
| 12 | `CAFE-0012`, 2026-03-10, `4 CRATE TRI`, `2 CRATE DUB` | the mail platform cannot be reached | `place` throws an exception, although the order is recorded `PLACED`, the beer taken and the courier booked | `FAILED`; booking cancelled, nothing kept; no mail at all |
| 13 | same | the mail platform sends the confirmation but does not answer in time | `place` throws an exception, order placed | `FAILED`; booking cancelled, nothing kept; the customer received the confirmation `Order SB-00001` and then `Order not placed` |
