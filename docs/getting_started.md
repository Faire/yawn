# Getting Started

Getting started with **Yawn** is **as simple as 3 steps**!

## Step 1: Add the plugin

The **Yawn** APIs require the code it generates to provide all the type-safety benefits.

For each gradle project that needs to use **Yawn**, make sure you have the `"com.faire.yawn"` gradle plugin installed in the build file. Basically, the head of
the file should look like the following:

```kotlin
plugins {
  // ...
  id("com.faire.yawn")
}
```

## Step 2: Annotate your Entity

Then, annotate each `DbEntity` class with `@YawnEntity`.

```kotlin
@Entity
@YawnEntity <-- add this
class Book {
  // ...
}
```

This will cause it to generate the class called `BookTable` which is used to write the queries.

Rebuild the project, a single folder, or just run a related test to ensure all table definitions and column definitions are generated and up-to-date
automatically by the compiler.

![Building the project in IntelliJ](/docs/images/building.png)

Alternatively, if you want to re-build on the terminal, just run:

```kotlin
./gradlew :your:project:assemble
```

## Step 3: Write your queries

Enjoy the bliss of type-safety and write your queries using `query`:

```kotlin
yawn.query(BookTable) { books ->
  addEq(books.name, "The Hobbit")
}.uniqueResult()
```

Simple as that!

