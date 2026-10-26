# Ferrow Connect 1.8 — Java guide

Ferrow Connect is the Java client of the Ferrow B2B platform: **payments** from customer
accounts, **courier** pick-ups and **mail**.

```xml
<dependency>
    <groupId>io.ferrow</groupId>
    <artifactId>ferrow-connect</artifactId>
    <version>1.8.0</version>
</dependency>
<!-- official test kit -->
<dependency>
    <groupId>io.ferrow</groupId>
    <artifactId>ferrow-connect-sandbox</artifactId>
    <version>1.8.0</version>
    <scope>test</scope>
</dependency>
```

## Clients

```java
PaymentClient payments = Connect.payments();
CourierClient courier = Connect.courier();
MailClient mail = Connect.mail();
```

Clients come from static factories and use the global configuration. Obtaining a client never
contacts the platform; each call sends one request to `api.ferrow.example`.

### Payments

```java
Charge charge = payments.charge("CAFE-0012", 4_250, "SO-1001");  // cents
charge.id();                                                     // used to refund
payments.refund(charge.id());                                    // full refund
```

- `charge` throws `DeclinedException` when the platform refuses the charge. Nothing is charged.
- `refund` is idempotent: refunding a charge that is already refunded succeeds and changes
  nothing. Refunding an unknown charge throws `NotFoundException`.

### Courier

```java
long cents = courier.quote(2, 25);                                // zone, kilos
Booking booking = courier.book("SO-1001", 2, LocalDate.of(2026, 3, 2), 3); // reference, zone, pick-up date, parcels
courier.cancel(booking.trackingId());
```

- Zones are numbered 1 to 5.
- `cancel` is idempotent: cancelling a cancelled booking succeeds and changes nothing.
  Cancelling an unknown booking throws `NotFoundException`.

**Tariff.** Quotes follow the published tariff, also available as `Tariff.price(zone, kilos)`:

    price in cents = 300 + 150 × zone + 60 × kilos

For example zone 2, 25 kg: 300 + 300 + 1500 = 2100 cents. Kilos range from 1 to 1000.

### Mail

```java
mail.send("orders@cafe.example", "Order SO-1001", "Thank you for your order.");
```

## Errors

All exceptions are unchecked and extend `FerrowException`.

| Exception | Meaning | Applied? |
|---|---|---|
| `DeclinedException` | the platform refused the charge (business answer) | no |
| `NotFoundException` | the charge or booking does not exist | no |
| `RemoteException` | no business answer; one of the two below | see below |
| `UnreachableException` | the platform could not be reached | no |
| `TimeoutException` | the platform did not answer in time | **unknown**: it may or may not have been applied |

Invalid arguments (blank account, zone 0, ...) throw `IllegalArgumentException` before any request
is sent.

## Platform access

The platform is reachable only from machines with platform access. Everywhere else, **every
client call fails** with `UnreachableException("api.ferrow.example unreachable")`. Develop and
test with the sandbox.

## The sandbox (official test kit)

`ferrow-connect-sandbox` installs a deterministic, in-memory platform behind `Connect`, for the
whole JVM. It accepts every request, applies it and records it, unless a fault rule says
otherwise.

```java
@BeforeEach
void startSandbox() {
    Sandbox.start();            // or Sandbox.reset(): a fresh sandbox every time
}

@AfterEach
void stopSandbox() {
    Sandbox.stop();             // Connect calls are unreachable again
}
```

- `Sandbox.start()` / `Sandbox.reset()`: a fresh sandbox, with no records, no fault rules, and ids
  counted from 1 again. Clients obtained earlier use it too.
- `Sandbox.stop()`: removes it.
- Nothing depends on the clock, on threads or on randomness: the same calls give the same ids and
  the same records.

### Ids

Charges are `ch_0001`, `ch_0002`, ...; bookings are `TRK-0001`, `TRK-0002`, ..., in the order the
sandbox applies them, counted from the last reset. Declined, unreachable and timed-out-before
requests do not use an id.

### Inspection

```java
Sandbox.charges();   // List<RecordedCharge>(id, account, cents, reference, refunded)
Sandbox.bookings();  // List<RecordedBooking>(trackingId, reference, zone, date, parcels, cancelled)
Sandbox.mails();     // List<RecordedMail>(to, subject, body)
Sandbox.calls();     // List<RecordedCall>(operation, arguments, outcome), every request received
```

Charges and bookings are listed in id order, with their current refunded / cancelled flag; mails
in the order they were sent. `charges()` lists accepted charges only. `calls()` lists every
request with its `Outcome` (`OK`, `DECLINED`, `NOT_FOUND`, `UNREACHABLE`,
`TIMEOUT_BEFORE_APPLYING`, `TIMEOUT_AFTER_APPLYING`), for diagnostics; for example
`payments.charge CAFE-0012 4250 SO-1001 -> OK`.

### Fault rules

Fault rules make requests fail according to **what they are about**: an account, a reference, a
zone, a recipient. They do not depend on how many calls were made before. A rule is active from
the moment it is created until the next reset.

```java
// payments
Sandbox.payments().decline().forAccount("CAFE-0012");
Sandbox.payments().decline().forReference("SO-1001");
Sandbox.payments().unreachable();                               // charges and refunds
Sandbox.payments().refund().timeoutBeforeApplying();
Sandbox.payments().charge().timeoutAfterApplying().forAccount("CAFE-0012");

// courier
Sandbox.courier().unreachable().forZone(4);                     // quote, book and cancel
Sandbox.courier().quote().timeoutBeforeApplying();
Sandbox.courier().book().timeoutAfterApplying().forReference("SO-1001");
Sandbox.courier().cancel().unreachable();

// mail
Sandbox.mail().unreachable();
Sandbox.mail().timeoutAfterApplying().forRecipient("orders@cafe.example");
Sandbox.mail().unreachable().forSubjectContaining("Invoice");

// only the next matching request fails, later ones succeed
Sandbox.payments().decline().forAccount("CAFE-0012").times(1);
```

Fault kinds:

- `decline()`: the charge is refused with `DeclinedException`. Charges only.
- `unreachable()`: `UnreachableException`; the request is not applied.
- `timeoutBeforeApplying()`: `TimeoutException`; the request is not applied.
- `timeoutAfterApplying()`: the request **is applied and recorded** (the charge exists, the
  booking exists, the mail is sent), then the caller gets `TimeoutException`.

Matching:

- Without an operation (`charge()`, `refund()`, `quote()`, `book()`, `cancel()`), a rule applies to
  every operation of the service.
- A refund matches through the charge it names (its account and reference); a cancel through the
  booking it names (its reference and zone). A quote has a zone but no reference.
- Several narrowings on one rule must all match. When several rules match a request, the oldest
  one applies.
- `times(n)`: the rule applies to the next `n` matching requests, then expires.

### TallowDB write faults

The sandbox can also make [TallowDB](tallowdb.md) writes fail, through TallowDB's write guard:

```java
Sandbox.store("cellar").table("orders").failWrites();           // insert, update and delete
Sandbox.store("cellar").table("orders").failInserts();
Sandbox.store("cellar").table("stock").failUpdates().where("sku", "IPA-30");
Sandbox.store("cellar").table("stock").failDeletes().times(1);
```

A failing write throws `StoreException` and writes nothing. `where(column, value)` matches the
value written or the value the row holds before the write. Sequences never fail. The sandbox does
not create, empty or drop databases: use `TallowDb.dropAll()` for that.
