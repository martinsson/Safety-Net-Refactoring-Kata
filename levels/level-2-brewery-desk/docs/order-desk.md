# The Saint Brevin order desk

## Purpose

The abbey of Saint Brevin brews four beers in small batches and sells them to cafés, shops and
private customers of the region. The order desk takes one order at a time and answers with a
confirmation: what will be delivered, when, what is back-ordered, and what the customer pays.
When an order is accepted, the desk also does what follows from it: it takes the beer from the
cellar stock, records the order, has the customer's account charged when the customer pays that
way, books the courier and mails the confirmation.

Production is limited, so the desk also shares the beer out: every customer type has a monthly
allocation, and the desk keeps track of the stock in the cellar as orders come in. Two identical
orders placed one after the other may therefore get different answers.

## Vocabulary

- **Beers**: `TRI` (tripel), `DUB` (dubbel), `BLO` (blonde) and `NOX`, a dark winter beer sold
  only from November to January. Beer codes are written in capitals. The beers the desk knows,
  with their prices and stock, are those of the cellar's `beers` table.
- **Units**: a beer is sold by the `BOTTLE`, by the `CRATE` of 24 bottles (old order forms say
  `CASE`), or by the `KEG`. NOX is not kegged.
- **Order line**: a quantity, a unit and a beer, for example `3 CRATE TRI`. Lines with an unknown
  beer, an unknown unit or a quantity of zero or less are left out of the confirmation. The order
  form always sends a whole-number quantity, a unit and a beer on every line.
- **Returned empties**: a line such as `4 RETURN CRATE` gives back empty crates, kegs or bottles;
  their deposit is credited on the order.
- **Customer**: identified by a code. Registered customers are in the cellar's `customers`
  table, with their type (`CAFE`, `SHOP`, or private), and possibly a mail address and a payment
  account. Anyone else is a private customer. Some codes have their own arrangements: the
  brothers of the abbey (`BRO-...`), guests of the abbey guesthouse (`GUEST-...`), and a few
  long-standing customers.
- **Zone**: the delivery zone, 0 meaning the customer collects the order at the abbey; zones 1 to
  5 are served by the courier, the higher the zone, the further away.
- **Allocation**: the number of bottles a customer type may receive in a calendar month. Cafés
  share one allocation, shops share another, each private customer has their own. A crate counts
  as its 24 bottles, a keg as 60 bottles.
- **Back-order**: what cannot be delivered now (no stock, allocation used up, beer out of season)
  is listed as back-ordered, is not charged, and is noted so that the brothers can plan the next
  brews.
- **Order number**: `SB<year>-<four digits>`, from the cellar's order counter.

## Main rules

**Delivery date.** The abbey does not deliver on Sundays or feast days (fixed feasts, among them
Saint Brevin's day on 3 October, and the movable feasts Easter Monday and Ascension Day). A delivery planned on
such a day moves to the next open day. The far zones are delivered on fewer days of the week.
The confirmation says when the delivery date was moved. The allocation month is the month of the
actual delivery date.

**What is delivered.** For each line the desk delivers as much as the stock and the remaining
allocation allow, in whole units, and back-orders the rest. Lines are served in the order of the
order form. A part of the bottle stock of every beer is kept for the cafés. An order is accepted
when at least something is delivered. If nothing can be delivered, the order is refused.

**Prices.** Each beer has a bottle price and a keg price. A crate costs less than its 24 bottles
bought one by one. Larger lines get a volume discount, with steps that depend on the unit. Cafés
and shops that order crates of several beers, or collect their order at the abbey, get a little
more off. The brothers have their own discount, which replaces all the others.

**Deposit.** Bottles, crates and kegs carry a deposit, charged on top of the goods, never
discounted and never taxed. Returned empties are credited against it, within limits for private
customers.

**Shipping.** Orders delivered by courier pay the courier's tariff for the weight of the order
(the Ferrow tariff, see [ferrow-connect.md](ferrow-connect.md)). Small orders to distant zones pay
a fee; large orders to nearby zones travel for free. Some customers have special shipping terms.

**Tax.** Cafés and shops pay the reduced rate, private customers the standard rate (the brothers
are taxed like cafés). Tax applies to the goods after discount plus shipping.

**Payment.** Customers with a payment account have the total charged to it when the order is
accepted. Otherwise cafés and shops pay by invoice, private customers pay cash on delivery, so
their total is rounded to five cents, and the brothers settle at the abbey.

**Confirmation.** It lists the delivered lines with their amounts, the back-ordered lines, the
returned empties, then the goods, discount, deposit, shipping, tax and total, the payment terms
of the customer and the courier's tracking number. Registered customers with a mail address
receive it by mail.

## The cellar database

The desk keeps its data in the `cellar` database (TallowDB, see [tallowdb.md](tallowdb.md)). Other
applications of the abbey read and write the same tables (the shop till, the brewing plan, the
accounts), so the table and column names below are fixed. Amounts are in cents.

| Table | Columns | Written by the desk |
|---|---|---|
| `beers` | `beer`, `bottles`, `kegs`, `bottle_price`, `keg_price` | stock taken by accepted orders; filled with the abbey's beers when the desk starts on an empty cellar |
| `customers` | `code`, `type`, `email`, `account` (the last three may be absent) | never |
| `allocations` | `key` (customer type or private customer code, then year and month, e.g. `SHOP/2026-3`), `used` (bottles) | bottles used |
| `orders` | `number`, `customer`, `delivery`, `status`, `total`, `terms`, `charge`, `tracking` | one row per accepted order |
| `order_lines` | `number`, `qty`, `unit`, `beer`, `amount` | one row per delivered line |
| `backorders` | `customer`, `qty`, `unit`, `beer`, `wanted` | one row per back-ordered line |

The order counter is the sequence `order_no`.
