# Nullability & Yawn

While **Yawn** provide full and sound type-safety to all queries, it has a loose acceptance in some cases for null types and values (i.e. it is designed to
accept non-nullable columns in places where nullable-columns are expected).

For example, consider a generated `TableDef` with two fields as follows (example from `Image`):

```kotlin
val url: YawnTableDef<_>.ColumnDef<String> = ColumnDef("url")
val originalUrl: YawnTableDef<_>.ColumnDef<String?> = ColumnDef("originalUrl")
```

## `addEq(*, null)` is banned

First and foremost, we do not allow `addEq(*, null)` as it leads to foot-guns because the behavior on the SQL side is not clear when dealing with such null
comparisons.

To accomplish that, we have two overloads of the method on the Kotlin side (they end up with different bytecode names due to type and nullability erasures):

```kotlin
  @JvmName("addEqNullable")
  fun <F : Any> addEq(
      column: YawnDef<T, *>.YawnColumnDef<F?>,
      value: F,
  )

  @JvmName("addEqNonNullable")
  fun <F : Any> addEq(
      column: YawnDef<T, *>.YawnColumnDef<F>,
      value: F,
  )
```

## `addIs[Not]Null` accepts any columns

And to actually test nullability, we provide dedicated methods `addIsNull(column)` and `addIsNotNull(column)`, that can end up a bit more verbose but force the
user to specify what happens on each branch.

At first thought we could do some clever typing and say that the `addIs[Not]Null` methods can only take nullable `ColumnDef`s. After all, it would make no sense
to call it with the `url` column above right?

```kotlin
// in a Yawn lambda
addIsNull(url) // <- could be a compile error
```

However the problem with SQL is that a table might not be the base table; it can come from a join. And when you consider joins, everything can be nullable. A V2
of **Yawn** could track that by creating two versions of each `TableDef`, but in the interest of time and simplicity, we decided not to do that for now. It gets
specially tricky with right joins, that would requiring redefining the main table variable of the lambda itself. Therefore, on the current version of **Yawn**,
methods such as `addIs[Not]Null` accept any `ColumnDef`.

## `nullable(column)`

That also means we need some convenience helpers for more complex queries. For example, imagine you want to do a `add[Not]Eq` between two columns, one nullable,
one not nullable. As defined, **Yawn** will not let that because of mismatched types. At a first glance, we could modify the definitions to something along the
lines of:

```kotlin
// Yawn version
  fun <F> addEq(
      column: YawnDef<T, *>.YawnColumnDef<F>,
      otherColumn: YawnDef<T, *>.YawnColumnDef<F>,
  )

// hypothetical version
 fun <F1, F2, F> addEq(
      column: YawnDef<T, *>.YawnColumnDef<F1>,
      otherColumn: YawnDef<T, *>.YawnColumnDef<F2>,
  ) where F1: F, F2: F
```

However the problem because obvious once you think about it: that would resolve for any two types `F1, F2`, as they will always share a supertype (at least `F =
Any`).

In order to support that, we would need different classes to represent nullable and non-nullable `ColumnDef`s - again, something we could explore for V2.

For now, to solve this problem, we provide the `nullable` function that “converts” a non-null column to be used on a nullable context.

So two compare two columns you could do:

```kotlin
addEq(images.url, images.originalUrl) // <- will not compile!
addEq(nullable(image.url), images.originalUrl)
```

That could also be useful for custom projections, though we have special code baked in the projection generated code to automatically support both nullable and
non-nullable columns.

## Custom Projections

There are also some considerations regarding custom projections (i.e., with a data class and `@YawnProjection`).

Consider our test example:

```kotlin
  @YawnProjection
  data class NullabilityAllowance(
   // ...
      val aLong: Long,
      val aNullableLong: Long?,
      val aString: String,
      val aNullableString: String?,
  )
```

The generated code will accept `TypedProjection`s (i.e. `ColumnDef`s or other projections) of both `T` and `T?` for each field; therefore, the `nullable`
function is not necessary in this context (though it could be called without harm):

```kotlin
project(
    NullabilityAllowanceProjection.create(
 aLong = table.nonNullLong,
 aNullableLong = table.nonNullLong, // ok!
 aString = table.nonNullString,
 aNullableString = nullable(table.nonNullString), // not necessary, but ok
    ),
)
```

This can only be done because, unlike the `addEq` of two arbitrary columns case, here we *know* the maximum supertype we support, so we can allow any sub-types.
Naturally that would also allow covariance on any sub-classes of columns, but today we only generate the flexible definitions for nullable columns. We are open
to change that if there area other sub-type use cases.

There are still cases where you might need some helper functions though. So far we provide two:

## `TypedProjections.coalesce`

This will do a Kotlin side (not SQL side) coalesce for you, allowing you to provide a nullable string into a non-null column with default value:

```kotlin
// [in a projection]
aString = TypedProjections.coalesce(books.notes, "fallback"),
```

Note that we don’t currently support a type-safe SQL-side coalesce, but we plan on implementing that soon.

## `TypedProjections.null`

If you want to always set `null` to the projection field, we also provide a `null()` function. This actually works on the SQL-side, but we plan to change this
(and its sibling `selectConstant`) to a more robust, generic, “constant” selector projection that works on the Kotlin side and pairs with the enhanced
`coalesce` implementation.

But for now you can do:

```kotlin
// [in a projection]
aNullableString = TypedProjections.`null`(),
```
