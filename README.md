# Result

Handle errors gracefully in a type-safe, functional manner similar to Kotlin's
[Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) type or Scala's
[Try](https://www.scala-lang.org/api/2.13.6/scala/util/Try.html) type using Java sealed classes.

A result is discriminated union that encapsulates a successful outcome with a value or a failure with an exception.
Instances of `Result<T>` are either an instance of `Success<T>` or `Failure<T>`.

## Using the library

Requires Java 21. View the [API documentation](https://josephearl.github.io/result/).

To use the library add the `https://maven.pkg.github.com/josephearl/result` repository (see
[Using a published package](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package)
for more information) and the dependency:

```groovy
implementation 'uk.co.josephearl.result:result:1.0.0-SNAPSHOT'
```

Example:

```java
Result<Long> result =
    Result.of(() -> Files.readString(Path.of("example.txt")))
        .map((value) -> value.lines().count())
        .flatMap((value) -> Result.of(() -> 3L / value))
        .filter((value) -> value == 1L);

// Simple
long value = result.orElse(0L);

// Pattern matching for switch
long patternMatchValue =
    switch (result) {
      case Success<Long> success -> success.value();
      case Failure<Long> ignored -> 0L;
    };

// Pattern matching for switch with record patterns
long recordPatternMatchValue =
    switch (result) {
      case Success<Long>(var v) -> v;
      case Failure<Long>(var ignored) -> 0L;
    };
```

## Developing the library

Requires Java 21. All commands are run from the root of the project, from a terminal:

| Command                         | Action                                             |
|:--------------------------------|:---------------------------------------------------|
| `./gradlew build`               | Build and run tests                                |
| `./gradlew spotlessApply`       | Format code                                        |
| `./gradlew javadoc`             | Generate documentation                             |
| `./gradlew publishToMavenLocal` | Publish the library to your local Maven repository |
