package io.github.josephearl.result;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.*;

/**
 * A discriminated union that encapsulates a successful outcome with a value of type {@code T} or a
 * failure with an arbitrary {@link Throwable} exception. Instances of {@code Result<T>} are either
 * an instance of {@link Success<T>} or {@link Failure<T>}.
 *
 * <p>This is a <a
 * href="https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; programmers should treat instances that are equal as interchangeable and should not use
 * instances for synchronization, or unpredictable behavior may occur. or example, in a future
 * release, synchronization may fail.
 */
public sealed interface Result<T> {
  /**
   * Returns a {@code Result} that encapsulates the outcome of calling the given {@code callable}
   * function as a {@link Success} containing the return value or a {@link Failure} containing the
   * exception thrown.
   *
   * <p>Note: only non-fatal exceptions (instances of {@link Exception}) are caught. Other instances
   * of {@link Throwable}) (i.e. {@link Error}s) will be thrown.
   */
  static <T> Result<T> of(@NonNull Callable<? extends T> callable) {
    try {
      return new Success<>(callable.call());
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

  /** Returns a {@link Success} that encapsulates the given {@code value}. */
  static <T> Success<T> success(@NonNull T value) {
    return new Success<>(value);
  }

  /** Returns a {@link Failure} that encapsulates the given {@code exception}. */
  static <T> Failure<T> failure(@NonNull Throwable exception) {
    return new Failure<>(exception);
  }

  /** Returns {@code true} if this is a {@link Success}, {@code false} otherwise. */
  boolean isSuccess();

  /** Returns {@code true} if this a {@link Failure}, {@code false} otherwise. */
  default boolean isFailure() {
    return !isSuccess();
  }

  /**
   * Applies the given {@code action} function to the encapsulated value if this is a {@link
   * Success}.
   */
  void ifSuccess(@NonNull Consumer<? super T> action);

  /**
   * Applies the given {@code exceptionAction} function to the encapsulated {@link Throwable} if
   * this is a {@link Failure}.
   */
  void ifFailure(@NonNull Consumer<? super Throwable> exceptionAction);

  /**
   * Applies the given {@code action} function to the value if this is a {@link Success} or
   * conversely the given {@code exceptionAction} function to the {@link Throwable} if this is a
   * {@link Failure}.
   */
  default void ifSuccessOrElse(
      @NonNull Consumer<? super T> action, @NonNull Consumer<? super Throwable> exceptionAction) {
    ifSuccess(action);
    ifFailure(exceptionAction);
  }

  /**
   * Returns this if this is a {@link Success} and the value matches the given {@code predicate} or
   * a {@link Failure} otherwise. Returns a new {@link Failure} encapsulating a {@link
   * java.util.NoSuchElementException} if the value does not match the predicate, or this if this is
   * a {@link Failure}.
   */
  Result<T> filter(Predicate<? super T> predicate);

  /**
   * Returns the outcome of applying the given {@code mapper} function to the value if this is a
   * {@link Success} or the {@code exceptionMapper} function to the {@link Throwable} if this is a
   * {@link Failure}.
   */
  <R> R fold(
      @NonNull Function<? super T, ? extends R> mapper,
      @NonNull Function<? super Throwable, ? extends R> exceptionMapper);

  /**
   * Returns a {@link Success} that encapsulates the outcome of applying the given {@code mapper}
   * function to the value if this is a {@link Success} or this if this is a {@link Failure}.
   */
  <R> Result<R> map(@NonNull Function<? super T, ? extends R> mapper);

  /**
   * Returns the outcome of applying the given {@code mapper} function to the value if this is a
   * {@link Success} or this if this is a {@link Failure}.
   */
  <R> Result<R> flatMap(@NonNull Function<? super T, ? extends Result<? extends R>> mapper);

  /**
   * Returns a {@link Failure} that encapsulates the outcome of applying the given {@code
   * exceptionMapper} function to the {@link Throwable} if this is a {@link Failure} or this if this
   * is a {@link Success}. This is like the {@link #map(Function)} but for the exception.
   */
  Result<T> mapFailure(@NonNull Function<? super Throwable, ? extends Throwable> exceptionMapper);

  /**
   * Returns the outcome of calling the given {@code supplier} function if this is a {@link Failure}
   * or this if this is a {@link Success}.
   */
  Result<T> or(@NonNull Supplier<? extends Result<? extends T>> supplier);

  /**
   * Returns the value if this is a {@link Success} or the given {@code defaultValue} if this is a
   * {@link Failure}.
   */
  T orElse(T defaultValue);

  /**
   * Returns the value if this is a {@link Success} or the outcome of applying the given {@code
   * exceptionMapper} function to the {@link Throwable} if this is a {@link Failure}.
   */
  T orElseGet(@NonNull Function<? super Throwable, ? extends T> exceptionMapper);

  /**
   * Returns the value if this is a {@link Success} or throws the {@link Throwable} if this is a
   * {@link Failure}.
   */
  T orElseThrow() throws Throwable;

  /**
   * Returns the value if this is a {@link Success} or throws the outcome of applying the given
   * {@code exceptionMapper} function to the {@link Throwable} if this is a {@link Failure}.
   */
  <X extends Throwable> T orElseThrow(
      @NonNull Function<? super Throwable, ? extends X> exceptionMapper) throws X;

  /**
   * Returns the {@link Throwable} if this is a {@link Failure} or {@code null} if this is a {@link
   * Success}.
   */
  Throwable exceptionOrNull();

  /**
   * Returns a {@link Success} that encapsulates the outcome of applying the given {@code
   * exceptionMapper} function to the {@link Throwable} if this is a {@link Failure} or this if this
   * is a {@link Success}. This is like the {@link #map(Function)} but for the exception.
   */
  Success<T> recover(@NonNull Function<? super Throwable, ? extends T> exceptionMapper);

  /**
   * Returns the outcome of applying the given {@code exceptionMapper} function to the {@link
   * Throwable} if this is a {@link Failure} or this if this is a {@link Success} This is like
   * {@link #flatMap(Function)} but for the exception.
   */
  Result<T> recoverWith(
      @NonNull Function<? super Throwable, ? extends Result<? extends T>> exceptionMapper);

  /**
   * Returns an {@link Optional} containing the value if this is a {@link Success} or an empty
   * {@link Optional} if this is a {@link Failure}.
   */
  Optional<T> toOptional();

  /** A {@link Result} representing a successful outcome. */
  record Success<T>(@NonNull T value) implements Result<T> {
    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public void ifSuccess(@NonNull Consumer<? super T> action) {
      action.accept(value);
    }

    @Override
    public void ifFailure(@NonNull Consumer<? super Throwable> exceptionAction) {}

    @Override
    public Result<T> filter(Predicate<? super T> predicate) {
      return predicate.test(value)
          ? this
          : new Failure<>(new NoSuchElementException("Predicate does not match " + value));
    }

    @Override
    public <R> R fold(
        @NonNull Function<? super T, ? extends R> mapper,
        @NonNull Function<? super Throwable, ? extends R> exceptionMapper) {
      return mapper.apply(value);
    }

    @Override
    public <R> Success<R> map(@NonNull Function<? super T, ? extends R> mapper) {
      return new Success<>(mapper.apply(value));
    }

    @Override
    public <R> Result<R> flatMap(
        @NonNull Function<? super T, ? extends Result<? extends R>> mapper) {
      @SuppressWarnings("unchecked")
      var result = (Result<R>) mapper.apply(value);
      return result;
    }

    @Override
    public Success<T> mapFailure(
        @NonNull Function<? super Throwable, ? extends Throwable> exceptionMapper) {
      return this;
    }

    @Override
    public Success<T> or(@NonNull Supplier<? extends Result<? extends T>> supplier) {
      return this;
    }

    @Override
    public T orElse(T defaultValue) {
      return value;
    }

    @Override
    public T orElseGet(@NonNull Function<? super Throwable, ? extends T> exceptionMapper) {
      return value;
    }

    @Override
    public T orElseThrow() {
      return value;
    }

    @Override
    public <X extends Throwable> T orElseThrow(
        @NonNull Function<? super Throwable, ? extends X> exceptionMapper) {
      return value;
    }

    @Override
    public Throwable exceptionOrNull() {
      return null;
    }

    @Override
    public Success<T> recover(@NonNull Function<? super Throwable, ? extends T> exceptionMapper) {
      return this;
    }

    @Override
    public Success<T> recoverWith(
        @NonNull Function<? super Throwable, ? extends Result<? extends T>> exceptionMapper) {
      return this;
    }

    @Override
    public Optional<T> toOptional() {
      return Optional.of(value);
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "(" + value + ")";
    }
  }

  /** A {@link Result} representing a failure. */
  record Failure<T>(@NonNull Throwable exception) implements Result<T> {
    @Override
    public boolean isSuccess() {
      return false;
    }

    @Override
    public void ifSuccess(@NonNull Consumer<? super T> action) {}

    @Override
    public void ifFailure(@NonNull Consumer<? super Throwable> exceptionAction) {
      exceptionAction.accept(exception);
    }

    @Override
    public Failure<T> filter(Predicate<? super T> predicate) {
      return this;
    }

    @Override
    public <R> R fold(
        @NonNull Function<? super T, ? extends R> mapper,
        @NonNull Function<? super Throwable, ? extends R> exceptionMapper) {
      return exceptionMapper.apply(exception);
    }

    @Override
    public <R> Failure<R> map(@NonNull Function<? super T, ? extends R> mapper) {
      @SuppressWarnings("unchecked")
      var result = (Failure<R>) this;
      return result;
    }

    @Override
    public <R> Failure<R> flatMap(
        @NonNull Function<? super T, ? extends Result<? extends R>> mapper) {
      @SuppressWarnings("unchecked")
      var result = (Failure<R>) this;
      return result;
    }

    @Override
    public Failure<T> mapFailure(
        @NonNull Function<? super Throwable, ? extends Throwable> exceptionMapper) {
      return new Failure<>(exceptionMapper.apply(exception));
    }

    @Override
    public Result<T> or(@NonNull Supplier<? extends Result<? extends T>> supplier) {
      @SuppressWarnings("unchecked")
      var result = (Result<T>) supplier.get();
      return result;
    }

    @Override
    public T orElse(T defaultValue) {
      return defaultValue;
    }

    @Override
    public T orElseGet(@NonNull Function<? super Throwable, ? extends T> exceptionMapper) {
      return exceptionMapper.apply(exception);
    }

    @Override
    public T orElseThrow() throws Throwable {
      throw exception;
    }

    @Override
    public <X extends Throwable> T orElseThrow(
        @NonNull Function<? super Throwable, ? extends X> exceptionMapper) throws X {
      throw exceptionMapper.apply(exception);
    }

    @Override
    public Throwable exceptionOrNull() {
      return exception;
    }

    @Override
    public Success<T> recover(@NonNull Function<? super Throwable, ? extends T> exceptionMapper) {
      return new Success<>(exceptionMapper.apply(exception));
    }

    @Override
    public Result<T> recoverWith(
        @NonNull Function<? super Throwable, ? extends Result<? extends T>> exceptionMapper) {
      @SuppressWarnings("unchecked")
      var result = (Result<T>) exceptionMapper.apply(exception);
      return result;
    }

    @Override
    public Optional<T> toOptional() {
      return Optional.empty();
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "(" + exception + ")";
    }
  }
}
