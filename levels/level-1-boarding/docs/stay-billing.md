# Stay billing at Mossgate

## Purpose

Mossgate boards pets while their owners are away. The front desk uses the billing module for
two things:

- the **invoice**, produced when a stay ends: one line per boarder with its amount, a few
  information lines, the total to pay, the loyalty points earned and an invoice reference;
- the **estimate**, a quick price given over the phone or at the desk before a booking.

## Vocabulary

| Term | Meaning |
|---|---|
| Owner | The customer. Identified by name only. |
| Boarder (pet) | An animal staying with us: a name, a species, a size class (1 small, 2 medium, 3 large) and whether it needs medication. |
| Stay | Check-in date and check-out date. Every pet of a stay arrives and leaves together. |
| Night | A night is counted from its date: a stay from Monday to Thursday has the nights of Monday, Tuesday and Wednesday. |
| Day care | Arrival and departure on the same day. |
| Weekend nights | Friday and Saturday nights. |
| Loyalty card | Card held by regular owners (numbers starting with L, gold cards with G). Points are earned on invoices and spent automatically. |
| Trade account | Kennels, farms and vets who board animals with us under a trade agreement and are billed monthly. |
| Hibernation | Long winter stays for tortoises, priced as a package. |
| Reference | Invoice number: house prefix, year, sequence number. |

## Pricing rules

**Nightly rate.** Each species has a nightly rate. For dogs and snakes the rate also depends on
the size class. Species we do not list are boarded at a default rate.

**Weekend.** Friday and Saturday nights cost a little more. Ferrets are more work at weekends
and pay a higher weekend supplement. Tortoises have no weekend supplement.

**Seasons.** Summer (July and August) and the end of December are high season: the night price
is increased by a seasonal factor.

**Day care** is billed as one night at a reduced rate.

**Tortoises** never pay more than a fixed ceiling for their lodging. In winter, a long tortoise
stay becomes a hibernation package with its own flat nightly price.

**Several pets.** When an owner brings several pets of the same species, the second and the
following ones get a reduction.

**Long stays** of one week or more get a reduction, larger from two weeks.

**Medication** is charged per night, up to a ceiling per pet.

Each pet's amount is rounded to the nearest five cents and printed on its own line.

**Loyalty.** An owner presenting a card with enough points gets a reduction on the whole
invoice, and the points are spent. Every invoice earns points according to the amount paid;
gold cards earn double.

**Minimum charge.** No invoice is below a minimum amount.

**Trade accounts** get a reduction on lodging, are billed at the end of the month (no minimum
charge) and do not use loyalty cards. Their invoice references have their own prefix.

**Mr Brindlecombe**, one of our oldest customers, has a personal agreement: his parrots have
their own rate, and he pays neither weekend supplements nor seasonal prices.

## The estimate

The estimate follows the same price list, but it is a quote, not a bill:

- it is rounded up to the whole euro;
- the season is judged on the arrival date;
- day care is quoted at a flat price per pet;
- it looks at the loyalty card but never spends or earns points;
- an impossible request (departure before arrival, no pet) is answered with -1.
