# Getting Started

Getting started with **Yawn** is **as simple as 4 steps**!

## Adding the Dependencies

There are two sets of dependencies you must add to your project to use Yawn; the `yawn-processor` dependency is what generates the definitions for your entities
annotated with `@YawnEntity`.

That needs to be added as a `compileOnly` and also `ksp` dependency:

```kotlin
    compileOnly("com.faire.yawn:yawn-processor:$version")
    ksp("com.faire.yawn:yawn-processor:$version")
```

As an alternative, you can use the Yawn Gradle plugin to automatically add the processor for you - see the
[Yawn Gradle Plugin readme][yawn-gradle-plugin-readme] for more details. This is recommended for multi-module Gradle projects.

Then, you need to add `yawn-api` as a regular dependency in order to actually make queries:

```kotlin
    implementation("com.faire.yawn:yawn-api:$version")
```

## Annotate your Entities

Annotate your Hibernate entities with `@YawnEntity` in order to have the necessary table and column definitions generated for them.

```kotlin
@Entity
@Table(name = "books")
@YawnEntity // <-- add this
class Book {
  // ...
}
```

This will cause it to generate the class called `BookTable` which is used to write the queries.

Rebuild the project, a single folder, or just run a related test to ensure all table definitions and column definitions are generated and up-to-date
automatically by the compiler.

![Building the project in IntelliJ](/docs/images/building.png)

Alternatively, if you want to re-build on the terminal, just run:

```bash
./gradlew :your:project:assemble
```

## Wire your QueryFactory

In order to hook Yawn into your Hibernate setup, you need to provide a `QueryFactory` implementation that knows how map the Yawn models into a Hibernate query.
For inspiration, you can check out the [`YawnTestQueryFactory`][yawn-test-query-factory] implementation.

Tip: wrap the Yawn class creation within your transaction management code to make it easier to use throughout your codebase!

## Write your queries

Finally, you are ready! Now you can write your type-safe queries using the power of Yawn:

```kotlin
  val yawn = Yawn(queryFactory = YourQueryFactory(...))

  val tolkienBooks = yawn.query(BookTable) { books ->
    val authors = join(books.author)
    addEq(authors.name, "J. R. R. Tolkien")
  }.list()
```

Next: read about [Basic Queries](/docs/basic_queries.md) to learn how to write queries with Yawn!


[yawn-gradle-plugin-readme]: /yawn-gradle-plugin/README.md
[yawn-test-query-factory]: /yawn-database-test/src/main/kotlin/com/faire/yawn/setup/hibernate/YawnTestQueryFactory.kt
