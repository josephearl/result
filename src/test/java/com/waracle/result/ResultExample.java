package com.waracle.result;

import com.waracle.result.Result.Failure;
import com.waracle.result.Result.Success;
import java.nio.file.Files;
import java.nio.file.Path;

class ResultExample {
  void example() throws Exception {
    Result<Long> result =
        Result.of(() -> Files.readString(Path.of("example.txt")))
            .map((value) -> value.lines().count())
            .flatMap((value) -> Result.of(() -> 3L / value))
            .filter((value) -> value == 1L);

    // Simple
    long value = result.orElse(0L);

    // Pattern matching for switch (Java 17 preview)
    long patternMatchValue =
        switch (result) {
          case Success<Long> success -> success.value();
          case Failure<Long> failure -> 0L;
        };

    // Pattern matching for switch with record patterns (Java 19 preview)
    long recordPatternMatchValue =
        switch (result) {
          case Success<Long>(var v) -> v;
          case Failure<Long>(var e) -> 0L;
          // Due to a bug in Java 19 this default case is required
          // https://mail.openjdk.org/pipermail/amber-dev/2022-September/007495.html
          default -> throw new AssertionError("Can never happen");
        };
  }
}
