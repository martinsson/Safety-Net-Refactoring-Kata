# The Saint Brevin order desk

## Purpose

The abbey of Saint Brevin brews four beers in small batches and sells them to cafés, shops and
private customers of the region. The order desk takes one order at a time and answers with a
confirmation: what will be delivered, when, what is back-ordered, and what the customer pays.

Production is limited, so the desk also shares the beer out: every customer type has a monthly
allocation, and the desk keeps track of the stock in the cellar as orders come in. Two identical
orders placed one after the other may therefore get different answers.

## Vocabulary

- **Beers**: `TRI` (tripel), `DUB` (dubbel), `BLO` (blonde) and `NOX`, a dark winter beer sold
  only from November to January. Beer codes are written in capitals.
- **Units**: a beer is sold by the `BOTTLE`, by the `CRATE` of 24 bottles (old order forms say
  `CASE`), or by the `KEG`. NOX is not kegged.
- **Order line**: a quantity, a unit and a beer, for example `3 CRATE TRI`. Lines with an unknown beer,
  an unknown unit or a quantity of zero or less are left out of the confirmation. The order form
  always sends a whole-number quantity, a unit and a beer on every line.
- **Returned empties**: a line such as `4 RETURN CRATE` gives back empty crates, kegs or bottles;
  their deposit is credited on the order.
- **Customer type**: `CAFE`, `SHOP`, or a private customer (any other value).
- **Customer code**: identifies the customer. Some codes have their own arrangements: the
  brothers of the abbey (`BRO-...`), guests of the abbey guesthouse (`GUEST-...`), and a few
  long-standing customers.
- **Zone**: the delivery zone, 0 meaning the customer collects the order at the abbey; the
  higher the zone, the further away.
- **Allocation**: the number of bottles a customer type may receive in a calendar month. Cafés
  share one allocation, shops share another, each private customer has their own. A crate counts
  as its 24 bottles, a keg as 60 bottles.
- **Back-order**: what cannot be delivered now (no stock, allocation used up, beer out of season)
  is listed as back-ordered and is not charged.

## Main rules

**Delivery date.** The abbey does not deliver on Sundays or feast days (fixed feasts, among them
Saint Brevin's day on 3 October, and movable feasts that follow Easter). A delivery planned on
such a day moves to the next open day. The far zones are delivered on fewer days of the week.
The confirmation says when the delivery date was moved. The allocation month is the month of the
actual delivery date.

**What is delivered.** For each line the desk delivers as much as the stock and the remaining
allocation allow, in whole units, and back-orders the rest. A part of the bottle stock of every
beer is kept for the cafés. An order is accepted when at least something is delivered; it then
gets an order number. If nothing can be delivered, the order is refused.

**Prices.** Each beer has a bottle price and a keg price. A crate costs less than its 24 bottles
bought one by one. Larger lines get a volume discount, with steps that depend on the unit. Cafés
and shops that order crates of several beers, or collect their order at the abbey, get a little
more off. The brothers have their own discount, which replaces all the others.

**Deposit.** Bottles, crates and kegs carry a deposit, charged on top of the goods, never
discounted and never taxed. Returned empties are credited against it, within limits for private
customers.

**Shipping.** Shipping depends on the zone and grows with the number of kegs (and, in the far
zones, crates). Small orders to distant zones pay a fee; large orders to nearby zones travel for
free. Some customers have special shipping terms.

**Tax.** Cafés and shops pay the reduced rate, private customers the standard rate (the brothers
are taxed like cafés). Tax applies to the goods after discount plus shipping.

**Confirmation.** It lists the delivered lines with their amounts, the back-ordered lines, the
returned empties, then the goods, discount, deposit, shipping, tax and total, and the payment
terms of the customer. Private customers pay cash on delivery, so their total is rounded to
five cents (the brothers settle at the abbey).
