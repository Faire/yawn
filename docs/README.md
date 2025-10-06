# Yawn

> [!NOTE]
> 💡 Want to quickly get up and running with Yawn? Check our quick start guide:
>
> [Getting Started](getting_started.md)

## Introduction

**Yawn** is our type-safe wrapper over Hibernate. It allows us to write queries in a fully type-safe, code-complete-ready manner. No more nasty strings,
unchecked types and runtime bugs.

```kotlin
transactor.callPrimaryReadOnly {
  val theHobbit = yawn.query(BookTable) { books ->
    addEq(books.name, "The Hobbit")
  }.uniqueResult()!!

  assertThat(theHobbit.token.id).isEqualTo("b_2")
  assertThat(theHobbit.author).isEqualTo("J.R.R. Tolkien")
}
```

This is what it looks like:


## Index

- [Getting Started](getting_started.md)
- [Basic Queries](basic_queries.md)
- [Joins](joins.md)
- [Projections](projections.md)
- [Sub-queries](sub_queries.md)
- [Nullability & Yawn](nullability_and_yawn.md)

## Contributing

Feel free to report bugs or suggest adding missing features by following the [Contribution guide](/CONTRIBUTING.md)!

