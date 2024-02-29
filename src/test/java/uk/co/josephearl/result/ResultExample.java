package uk.co.josephearl.result;

import java.nio.file.Files;
import java.nio.file.Path;

class ResultExample {
  void example() {
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
          case Result.Success<Long> success -> success.value();
          case Result.Failure<Long> ignored -> 0L;
        };

    // Pattern matching for switch with record patterns
    long recordPatternMatchValue =
        switch (result) {
          case Result.Success<Long>(var v) -> v;
          case Result.Failure<Long>(var ignored) -> 0L;
        };
  }
}
