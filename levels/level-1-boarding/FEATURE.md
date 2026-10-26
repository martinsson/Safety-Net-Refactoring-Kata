# Holiday nights

**Requested by:** front desk and management. **Applies to:** invoices and estimates.

## Why

On public holidays we have to pay the staff more, and the house is always full. Until now a
holiday night cost the same as any other night. From now on, holiday nights carry a holiday
supplement, and the accountant needs to see on each invoice how much of it was billed, because
it is paid back to the staff on duty.

## Holiday nights

A night is a holiday night when its date (the date the night starts, as everywhere else in
billing) is one of:

- 1 January
- 1 May
- 25 December
- 26 December
- Fair Monday: the first Monday of September

## The rule

- Each pet pays a **holiday supplement of 6.00** for each holiday night of its stay.
- On a holiday night the holiday supplement **replaces** the weekend supplement: when a holiday
  falls on a Friday or a Saturday night, only the holiday supplement is charged (ferrets
  included).
- **Tortoises** do not pay it (hibernation packages included).
- **Mr Brindlecombe pays it**: his agreement covers weekends and seasons, not holidays.
- The supplement is part of the night's price: everything that applies to the night's price
  (seasonal prices, day care, reductions for several pets, long stays and trade accounts)
  applies to it as well, exactly as for the rest of the night.
- A day-care visit on a holiday counts as one holiday night on the invoice.
- The **estimate** includes the supplement too, priced the way the estimate already prices
  nights. Day care in the estimate keeps its flat price.

## On the invoice

When the stay has at least one holiday night, the invoice gets one extra line:

```
Holiday nights: N, supplement X
```

- N is the number of holiday nights of the stay (whatever the pets).
- X is the part of the pets' amounts that comes from the holiday supplement: for each pet, its
  supplements after seasonal prices and every reduction that applied to that pet's lodging
  (day care, several pets, long stay, trade account), added up for all pets and rounded to the
  cent. The loyalty reduction, which applies to the whole invoice, is not taken into account.
  X is 0 when only tortoises stayed.
- X is written the way amounts are written on the pets' lines: `15.6`, `6.0`, `19.44`.

This line comes after all the pets' lines and before the loyalty line, if there is one. Stays
without a holiday night do not get this line, and nothing else changes on them.

Nothing changes in how `StayBilling` is called: the supplement shows in the totals, in the pets'
amounts, and in the new invoice line above (an element of `Invoice.getLines()`).

## Examples

| Owner | Pets | Stay | Invoice lines | Invoice total | Estimate |
|---|---|---|---|---|---|
| Durand | Mimi, CAT, size 1 | 2024-12-24 to 2024-12-27 | `Mimi CAT 3n 85.8`, `Holiday nights: 2, supplement 15.6` | 85.8 | 66.0 |
| Durand | Fifi, FERRET, size 1 | 2025-08-30 to 2025-09-02 | `Fifi FERRET 3n 72.0`, `Holiday nights: 1, supplement 6.0` | 72.0 | 78.0 |
| Durand | Fifi, FERRET, size 1 | 2026-12-26 to 2026-12-27 (a Saturday) | `Fifi FERRET 1n 28.6`, `Holiday nights: 1, supplement 7.8` | 28.6 | 25.0 |
| Brindlecombe | Coco, PARROT, size 1 | 2025-05-01 to 2025-05-03 | `Coco PARROT 2n 28.0`, `Holiday nights: 1, supplement 6.0` | 28.0 | 28.0 |
| Durand | Leon, TORTOISE, size 1 | 2025-01-01 to 2025-01-03 | `Leon TORTOISE 2n 18.0`, `Holiday nights: 1, supplement 0.0` | 25.0 | 25.0 |
| Durand | Rex, DOG, size 3 | 2025-12-25 to 2025-12-25 (day care) | `Rex DOG 1n 31.2`, `Holiday nights: 1, supplement 4.68` | 31.2 | 15.0 |
| Durand | Mimi, CAT, size 1 and Tom, CAT, size 1 | 2025-01-01 to 2025-01-02 | `Mimi CAT 1n 24.0`, `Tom CAT 1n 19.2`, `Holiday nights: 1, supplement 10.8` | 43.2 | 44.0 |
| Durand | Rex, DOG, size 2, medication | 2024-12-20 to 2025-01-03 | `Rex DOG 14n 505.85`, `Holiday nights: 3, supplement 19.44` | 505.85 | 411.0 |
| Hollins Kennels | Rex, DOG, size 2 | 2025-05-01 to 2025-05-03 | `Rex DOG 2n 57.6`, `Holiday nights: 1, supplement 5.4` | 57.6 | 58.0 |
| Durand | Rex, DOG, size 2 | 2025-09-08 to 2025-09-09 (second Monday) | `Rex DOG 1n 27.0` | 27.0 | 27.0 |

With loyalty card L-1187 (100 points), the first example becomes: lines `Mimi CAT 3n 85.8`,
`Holiday nights: 2, supplement 15.6`, `Loyalty -5%`; total 81.51; 8 points earned.
