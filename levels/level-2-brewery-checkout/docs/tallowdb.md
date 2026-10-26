# TallowDB 3.2 — Java guide

TallowDB is an embedded, in-memory table store. No server, no schema, no configuration: open a
database by name, use its tables.

```xml
<dependency>
    <groupId>com.tallowdb</groupId>
    <artifactId>tallowdb</artifactId>
    <version>3.2.1</version>
</dependency>
```

## Databases

```java
Database cellar = TallowDb.open("cellar");
```

`TallowDb.open(name)` returns the database of that name, creating it empty on first use. The
registry is shared by the whole JVM: every call with the same name returns the **same instance**,
wherever it is made. Data lives in memory and is gone when the JVM stops.

- `TallowDb.drop(name)` drops one database; `TallowDb.dropAll()` drops them all (handy between
  tests). A later `open` returns a new, empty database.
- TallowDB is not thread-safe: use a database from one thread at a time.

## Tables

```java
Table kegs = cellar.table("kegs");                       // created on first use

long id = kegs.insert(Map.of("sku", "IPA-30", "litres", 30, "filled", LocalDate.of(2026, 3, 2)));
Optional<Row> keg = kegs.get(id);
List<Row> ipas = kegs.find("sku", "IPA-30");             // ordered by id
List<Row> all = kegs.all();                              // ordered by id
boolean updated = kegs.update(id, Map.of("litres", 20)); // false if there is no such row
boolean deleted = kegs.delete(id);                       // false if there is no such row
int rows = kegs.count();
```

- Ids are generated per table, start at 1 and are never reused, even after a delete.
- Column types: `String`, `Long` (an `Integer` is stored as a `Long`), `Boolean`, `LocalDate`.
  Other types are refused with `IllegalArgumentException`. The column name `id` is reserved.
- `update` changes only the columns you pass; other columns keep their value. A `null` value
  removes the column.
- `find` compares with `equals` after the same conversion (`find("litres", 30)` finds `30L`).

## Rows

Rows are **copies**: changing what you read never changes the table, and a row you hold does not
see later writes.

```java
Row row = kegs.get(id).orElseThrow();
row.id();                     // 1
row.getString("sku");         // "IPA-30", or null if absent
row.getLong("litres");        // 30
row.getInt("litres");         // 30
row.getBoolean("empty");      // throws NoSuchElementException if absent
row.getDate("filled");        // 2026-03-02, or null if absent
row.values();                 // all columns, sorted by name (without the id)
row.toString();               // kegs#1{filled=2026-03-02, litres=30, sku=IPA-30}
```

## Sequences

```java
long next = cellar.nextValue("order_no");   // 1, then 2, 3, ...
cellar.restartSequence("order_no", 1000);   // the next value handed out is 1000
```

A value drawn is never given back, even if you do not use it.

## No transactions

TallowDB has **no transactions**. Every write is applied the moment you call it and stays
applied, whatever happens next in your code. There is nothing to commit and nothing to roll back:
if a sequence of writes must be undone, your code has to write the undo.

## Errors

A write (`insert`, `update`, `delete`) may fail with the unchecked `StoreException`. When a
write throws, nothing was written by that call; earlier writes stay applied. Reads and sequences
do not fail.

## Write guards (for tools and test kits)

`com.tallowdb.spi.WriteGuards.install(guard)` installs one JVM-wide `WriteGuard`, called before
every insert, update and delete with a `WriteRequest` (database, table, kind, row id, values
written, row before the write). A guard refuses a write by throwing `StoreException`.
`WriteGuards.clear()` removes it. The guard survives `TallowDb.dropAll()`.

Test kits use this hook to simulate write failures; for example the Ferrow Connect sandbox offers
`Sandbox.store("cellar").table("orders").failWrites()`. Application code has no reason to use it.
