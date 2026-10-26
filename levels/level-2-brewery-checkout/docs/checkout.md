# The Saint Brevin checkout

## Purpose

The abbey of Saint Brevin brews four beers in small batches and sells them to cafés, shops and
private customers of the region. The checkout takes one order at a time and does everything that
placing it involves: it sets the beer aside in the cellar, records the order, takes the payment
when the customer pays at order, books the courier and sends the confirmation mail. It answers
with a receipt.

The cellar's records live in the abbey's TallowDB database `cellar` (see [tallowdb.md](tallowdb.md));
payments, courier and mail go through the Ferrow platform (see
[ferrow-connect.md](ferrow-connect.md)).

## Vocabulary

- **Beers**: `TRI` (tripel), `DUB` (dubbel), `BLO` (blonde) and `NOX`, a dark winter beer sold
  only from November to January. Beer codes are written in capitals.
- **Units**: a beer is sold by the `BOTTLE`, by the `CRATE` of 24 bottles (old order forms say
  `CASE`), or by the `KEG`. NOX is not kegged.
- **Order line**: a quantity, a unit and a beer, for example `3 CRATE TRI`. Lines with an unknown
  beer, an unknown unit or a quantity of zero or less are left out.
- **Returned empties**: a line such as `2 RETURN KEG` or `4 RETURN CRATE` gives back empty kegs or
  crates; their deposit is credited on the order, within a limit for private customers.
- **Customer**: identified by a code (`CAFE-0012`, `SH-0101`, `P-77`...). The office keeps each
  customer's type (`CAFE`, `SHOP`, anything else is a private customer), delivery zone, mail
  address, Ferrow account and payment terms (`ORDER`: pays when ordering, `INVOICE`: receives an
  invoice).
- **Zone**: 0 means the customer collects the order at the abbey; 1 to 5 are delivered by the
  courier, the higher the further.
- **Allocation**: the number of bottles a customer may receive in a calendar month. Cafés share
  one allocation, shops share another, each private customer has their own. A crate counts as 24
  bottles, a keg as 60.
- **Back-order**: what cannot be delivered now (no stock, allocation used up, beer out of season)
  is listed as back-ordered and is not charged.

## The cellar database

| Table | One row per | Columns |
|---|---|---|
| `customers` | customer (maintained by the office) | `code`, `type`, `zone`, `email`, `account`, `terms` |
| `stock` | beer (maintained by the cellar) | `beer`, `bottles`, `kegs` |
| `allocations` | allocation and month | `key` (`CAFE/2026-03`, `SHOP/2026-03`, `P-77/2026-03`), `used` (bottles) |
| `orders` | order | `number`, `customer`, `delivery`, `total` (cents), `status`, `charge`, `tracking` |

Order numbers (`SB-00001`, `SB-00002`...) come from the sequence `order_no`.

## Placing an order

**Customer and date.** Orders from unknown customers are refused. The abbey does not deliver on
Sundays or feast days; a delivery planned on such a day moves to the next open day and the
receipt says so. The allocation month is the month of the actual delivery date.

**Setting the beer aside.** For each line the checkout delivers as much as the stock and the
remaining allocation allow, in whole units, takes it from the stock and counts it in the
allocation; the rest is back-ordered. If nothing at all can be delivered, the order is refused
and nothing is recorded.

**Prices.** Each beer has a bottle price and a keg price; a crate costs less than its 24 bottles.
Larger lines get a volume discount. Bottles, crates and kegs carry a deposit, charged on top of
the goods and never taxed; returned empties are credited against it. Cafés and shops pay the
reduced tax rate, private customers the standard rate.

**Shipping.** Orders collected at the abbey travel for free, and so do large orders of cafés and
shops. Otherwise the courier gives the price for the zone and the weight; when the courier cannot
be asked, the abbey's flat rate per zone applies.

**Recording, payment, courier, mail.** The order gets the next number and is recorded in
`orders`. Customers who pay at order are charged the total on their Ferrow account, with the order
number as reference. Orders in zones 1 to 5 are booked with the courier for the delivery date. The
customer receives a confirmation mail with the lines, the total, the payment and the delivery.

**When the platform is unavailable.** A declined payment ends the order: the beer goes back to the
cellar and the customer is told by mail. When the payment platform cannot be reached, the order
is recorded with the status `TO_CHARGE` and the evening run takes the payment; when the courier
cannot be booked, it is recorded as `TO_BOOK` and the office books it by phone. Placed orders
have the status `PLACED`.

**Receipt.** It gives the order number, the status (`PLACED`, `REFUSED`, `DECLINED`...) with an
optional message, the delivery date, the delivered, back-ordered and returned lines, and the
amounts: goods, discount, deposit, shipping, tax and total.
