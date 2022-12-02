package com.waracle.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.*;

import com.waracle.result.Result.Failure;
import com.waracle.result.Result.Success;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultTest {
  @Test
  void
      ofShouldReturnSuccessThatEncapsulatesOutcomeOfCallingCallableWhenCallableReturnsSuccessfully()
          throws Exception {
    var value = "test";
    var callable = (Callable<String>) mock(Callable.class);
    when(callable.call()).thenReturn(value);

    Result<String> result = Result.of(callable);

    assertThat(result).asInstanceOf(type(Success.class)).extracting(Success::value).isSameAs(value);
  }

  @Test
  void ofShouldReturnFailureThatEncapsulatesOutcomeOfCallingCallableWhenCallableThrowsException()
      throws Exception {
    var exception = new Exception("test");
    var callable = (Callable<String>) mock(Callable.class);
    when(callable.call()).thenThrow(exception);

    Result<String> result = Result.of(callable);

    assertThat(result)
        .asInstanceOf(type(Failure.class))
        .extracting(Failure::exception)
        .isSameAs(exception);
  }

  @Test
  void ofShouldThrowExceptionWhenCallableThrowsError() throws Exception {
    var exception = new Error("test");
    var callable = (Callable<String>) mock(Callable.class);
    when(callable.call()).thenThrow(exception);

    assertThatThrownBy(() -> Result.of(callable)).isSameAs(exception);
  }

  @Nested
  class SuccessTest {
    @Test
    void constructorShouldCreateNewSuccess() {
      var value = "test";

      var success = new Success<>(value);

      assertThat(success.value()).isSameAs(value);
    }

    @Test
    void constructorShouldThrowExceptionWhenExceptionIsNull() {
      assertThatThrownBy(() -> new Success<String>(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("value");
    }

    @Test
    void isSuccessShouldReturnTrue() {
      var success = new Success<>("test");

      boolean result = success.isSuccess();

      assertThat(result).isTrue();
    }

    @Test
    void isFailureShouldReturnFalse() {
      var success = new Success<>("test");

      boolean result = success.isFailure();

      assertThat(result).isFalse();
    }

    @Test
    void ifSuccessShouldApplyActionToValue() {
      var value = "test";
      var success = new Success<>(value);
      var action = (Consumer<String>) mock(Consumer.class);

      success.ifSuccess(action);

      verify(action).accept(value);
    }

    @Test
    void ifFailureShouldNotApplyExceptionAction() {
      var success = new Success<>("test");
      var exceptionAction = (Consumer<Throwable>) mock(Consumer.class);

      success.ifFailure(exceptionAction);

      verifyNoInteractions(exceptionAction);
    }

    @Test
    void ifSuccessOrElseShouldApplyActionToValue() {
      var value = "test";
      var success = new Success<>(value);
      var action = (Consumer<String>) mock(Consumer.class);
      var exceptionAction = (Consumer<Throwable>) mock(Consumer.class);

      success.ifSuccessOrElse(action, exceptionAction);

      verify(action).accept(value);
      verifyNoInteractions(exceptionAction);
    }

    @Test
    void filterShouldReturnThisWhenPredicateMatchesValue() {
      var value = "test";
      var success = new Success<>(value);
      var predicate = (Predicate<String>) mock(Predicate.class);
      when(predicate.test(value)).thenReturn(true);

      var result = success.filter(predicate);

      assertThat(result).isSameAs(success);
    }

    @Test
    void
        filterShouldReturnFailureThatEncapsulatesNoSuchElementExceptionWhenPredicateDoesNotMatchValue() {
      var value = "test";
      var success = new Success<>(value);
      var predicate = (Predicate<String>) mock(Predicate.class);
      when(predicate.test(value)).thenReturn(false);

      var result = success.filter(predicate);

      assertThat(result)
          .asInstanceOf(type(Failure.class))
          .extracting(Failure::exception)
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void foldShouldReturnOutcomeOfApplyingSuccessMapperToValue() {
      var value = "test";
      var success = new Success<>(value);
      var mappedValue = 1;
      var successMapper = (Function<String, Integer>) mock(Function.class);
      when(successMapper.apply(value)).thenReturn(mappedValue);
      var exceptionMapper = (Function<Throwable, Integer>) mock(Function.class);

      Integer result = success.fold(successMapper, exceptionMapper);

      assertThat(result).isSameAs(mappedValue);
      verifyNoInteractions(exceptionMapper);
    }

    @Test
    void mapShouldReturnSuccessThatEncapsulatesOutcomeOfApplyingMapperToValue() {
      var value = "test";
      var success = new Success<>(value);
      var mappedValue = 1;
      var mapper = (Function<String, Integer>) mock(Function.class);
      when(mapper.apply(value)).thenReturn(mappedValue);

      Success<Integer> result = success.map(mapper);

      assertThat(result.value()).isEqualTo(mappedValue);
    }

    @Test
    void flatMapShouldReturnOutcomeOfApplyingMapperToValue() {
      var value = "test";
      var success = new Success<>(value);
      var mappedValue = new Success<>(1);
      var mapper = (Function<String, Success<Integer>>) mock(Function.class);
      when(mapper.apply(value)).thenReturn(mappedValue);

      Result<Number> result = success.flatMap(mapper);

      assertThat(result).isSameAs(mappedValue);
    }

    @Test
    void mapFailureShouldReturnThis() {
      var success = new Success<>("test");
      var exceptionMapper = (Function<Throwable, Throwable>) mock(Function.class);

      Success<String> result = success.mapFailure(exceptionMapper);

      assertThat(result).isSameAs(success);
      verifyNoInteractions(exceptionMapper);
    }

    @Test
    void orShouldReturnThis() {
      var success = new Success<>("test");
      var supplier = (Supplier<Result<String>>) mock(Supplier.class);

      Success<String> result = success.or(supplier);

      assertThat(result).isSameAs(success);
      verifyNoInteractions(supplier);
    }

    @Test
    void orElseShouldReturnValue() {
      var value = "test";
      var success = new Success<>(value);

      String result = success.orElse("alternative");

      assertThat(result).isSameAs(value);
    }

    @Test
    void orElseGetShouldReturnValue() {
      var value = "test";
      var success = new Success<>(value);
      var exceptionMapper = (Function<Throwable, String>) mock(Function.class);

      String result = success.orElseGet(exceptionMapper);

      assertThat(result).isSameAs(value);
      verifyNoInteractions(exceptionMapper);
    }

    @Test
    void orElseThrowShouldReturnValue() {
      var value = "test";
      var success = new Success<>(value);

      String result = success.orElseThrow();

      assertThat(result).isSameAs(value);
    }

    @Test
    void orElseThrowWithMapperShouldReturnValue() {
      var value = "test";
      var success = new Success<>(value);
      var exceptionMapper = (Function<Throwable, RuntimeException>) mock(Function.class);

      String result = success.orElseThrow(exceptionMapper);

      assertThat(result).isSameAs(value);
      verifyNoInteractions(exceptionMapper);
    }

    @Test
    void exceptionOrNullShouldReturnNull() {
      var success = new Success<>("test");

      Throwable result = success.exceptionOrNull();

      assertThat(result).isNull();
    }

    @Test
    void recoverShouldReturnThis() {
      var success = new Success<>("test");
      var exceptionMapper = (Function<Throwable, String>) mock(Function.class);

      Success<String> result = success.recover(exceptionMapper);

      assertThat(result).isSameAs(success);
      verifyNoInteractions(exceptionMapper);
    }

    @Test
    void recoverWithShouldReturnThis() {
      var success = new Success<>("test");
      var exceptionMapper = (Function<Throwable, Result<String>>) mock(Function.class);

      Result<String> result = success.recoverWith(exceptionMapper);

      assertThat(result).isSameAs(success);
      verifyNoInteractions(exceptionMapper);
    }

    @Test
    void toOptionShouldReturnOptionalWithValuePresent() {
      var value = "test";
      var success = new Success<>(value);

      Optional<String> result = success.toOptional();

      assertThat(result).isPresent().hasValue(value);
    }

    @Test
    void equalsAndHashCodeShouldSatisfyContract() {
      EqualsVerifier.forClass(Success.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringShouldReturnSuccessWithToStringOfValue() {
      var valueString = "test";
      var value = mock(Object.class);
      when(value.toString()).thenReturn(valueString);
      var success = new Success<>(value);

      String result = success.toString();

      assertThat(result).isEqualTo("Success(" + valueString + ")");
    }
  }

  @Nested
  class FailureTest {
    @Test
    void constructorShouldCreateNewFailure() {
      var exception = new Exception("test");

      var failure = new Failure<>(exception);

      assertThat(failure.exception()).isSameAs(exception);
    }

    @Test
    void constructorShouldThrowExceptionWhenExceptionIsNull() {
      assertThatThrownBy(() -> new Failure<String>(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("exception");
    }

    @Test
    void isSuccessShouldReturnFalse() {
      var failure = new Failure<>(new Exception("test"));

      boolean result = failure.isSuccess();

      assertThat(result).isFalse();
    }

    @Test
    void isFailureShouldReturnTrue() {
      var failure = new Failure<>(new Exception("test"));

      boolean result = failure.isFailure();

      assertThat(result).isTrue();
    }

    @Test
    void ifSuccessShouldNotApplyAction() {
      var failure = new Failure<>(new Exception("test"));
      var action = (Consumer<Object>) mock(Consumer.class);

      failure.ifSuccess(action);

      verifyNoInteractions(action);
    }

    @Test
    void ifFailureShouldApplyExceptionActionToException() {
      var exception = new Exception("test");
      var failure = new Failure<>(exception);
      var exceptionAction = (Consumer<Throwable>) mock(Consumer.class);

      failure.ifFailure(exceptionAction);

      verify(exceptionAction).accept(exception);
    }

    @Test
    void ifSuccessOrElseShouldApplyExceptionActionToException() {
      var exception = new Exception("test");
      var failure = new Failure<>(exception);
      var action = (Consumer<Object>) mock(Consumer.class);
      var exceptionAction = (Consumer<Throwable>) mock(Consumer.class);

      failure.ifSuccessOrElse(action, exceptionAction);

      verify(exceptionAction).accept(exception);
      verifyNoInteractions(action);
    }

    @Test
    void filterShouldReturnThis() {
      var failure = new Failure<>(new Exception("test"));
      var predicate = (Predicate<Object>) mock(Predicate.class);

      var result = failure.filter(predicate);

      assertThat(result).isSameAs(failure);
      verifyNoInteractions(predicate);
    }

    @Test
    void foldShouldReturnTheOutcomeOfApplyingExceptionMapperToException() {
      var exception = new Exception("test");
      var failure = new Failure<>(exception);
      var mappedException = "mapped";
      var successMapper = (Function<Object, String>) mock(Function.class);
      var exceptionMapper = (Function<Throwable, String>) mock(Function.class);
      when(exceptionMapper.apply(exception)).thenReturn(mappedException);

      String result = failure.fold(successMapper, exceptionMapper);

      assertThat(result).isSameAs(mappedException);
      verifyNoInteractions(successMapper);
    }

    @Test
    void mapShouldReturnThis() {
      var failure = new Failure<>(new Exception("test"));
      var mapper = (Function<Object, Object>) mock(Function.class);

      Failure<Object> result = failure.map(mapper);

      assertThat(result).isSameAs(failure);
      verifyNoInteractions(mapper);
    }

    @Test
    void flatMapShouldReturnThis() {
      var failure = new Failure<>(new Exception("test"));
      var mapper = (Function<Object, Result<Object>>) mock(Function.class);

      Failure<Object> result = failure.flatMap(mapper);

      assertThat(result).isSameAs(failure);
      verifyNoInteractions(mapper);
    }

    @Test
    void mapFailureReturnFailureThatEncapsulatesOutcomeOfApplyingExceptionMapperToException() {
      var exception = new Exception("test");
      var failure = new Failure<>(exception);
      var mappedException = new Exception("mapped");
      var exceptionMapper = (Function<Throwable, Throwable>) mock(Function.class);
      when(exceptionMapper.apply(exception)).thenReturn(mappedException);

      Failure<Object> result = failure.mapFailure(exceptionMapper);

      assertThat(result.exception()).isSameAs(mappedException);
    }

    @Test
    void orShouldReturnOutcomeOfCallingSupplier() {
      var failure = new Failure<>(new Exception("test"));
      var defaultValue = new Failure<>(new Exception("alternative"));
      var supplier = (Supplier<Result<Object>>) mock(Supplier.class);
      when(supplier.get()).thenReturn(defaultValue);

      Result<Object> result = failure.or(supplier);

      assertThat(result).isSameAs(defaultValue);
    }

    @Test
    void orElseShouldReturnDefaultValue() {
      var failure = new Failure<String>(new Exception("test"));
      var defaultValue = "alternative";

      String result = failure.orElse(defaultValue);

      assertThat(result).isSameAs(defaultValue);
    }

    @Test
    void orElseGetShouldReturnOutcomeOfApplyingExceptionMapperToException() {
      var exception = new Exception("test");
      var failure = new Failure<String>(exception);
      var defaultValue = "alternative";
      var exceptionMapper = (Function<Throwable, String>) mock(Function.class);
      when(exceptionMapper.apply(exception)).thenReturn(defaultValue);

      String result = failure.orElseGet(exceptionMapper);

      assertThat(result).isSameAs(defaultValue);
    }

    @Test
    void orElseThrowShouldThrowException() {
      var exception = new Exception("test");
      var failure = new Failure<>(exception);

      assertThatThrownBy(failure::orElseThrow).isSameAs(exception);
    }

    @Test
    void orElseThrowShouldThrowError() {
      var exception = new Error("test");
      var failure = new Failure<>(exception);

      assertThatThrownBy(failure::orElseThrow).isSameAs(exception);
    }

    @Test
    void orElseThrowShouldThrowUnsupportedThrowableException() {
      var exception = new Throwable("test");
      var failure = new Failure<>(exception);

      assertThatThrownBy(failure::orElseThrow)
          .isInstanceOf(UnsupportedThrowableException.class)
          .extracting(Throwable::getCause)
          .isSameAs(exception);
    }

    @Test
    void orElseThrowWithMapperShouldThrowTheOutcomeOfApplyingExceptionMapperToException() {
      var exception = new Exception("test");
      var failure = new Failure<>(exception);
      var mappedException = new RuntimeException("mapped");
      var exceptionMapper = (Function<Throwable, RuntimeException>) mock(Function.class);
      when(exceptionMapper.apply(exception)).thenReturn(mappedException);

      assertThatThrownBy(() -> failure.orElseThrow(exceptionMapper)).isSameAs(mappedException);
    }

    @Test
    void exceptionOrNullShouldReturnException() {
      var exception = new Exception("test");
      var failure = new Failure<>(exception);

      Throwable result = failure.exceptionOrNull();

      assertThat(result).isSameAs(exception);
    }

    @Test
    void recoverShouldReturnSuccessThatEncapsulatesOutcomeOfApplyingExceptionMapperToException() {
      var exception = new Exception("test");
      var failure = new Failure<String>(exception);
      var mappedException = "mapped";
      var exceptionMapper = (Function<Throwable, String>) mock(Function.class);
      when(exceptionMapper.apply(exception)).thenReturn(mappedException);

      Success<String> result = failure.recover(exceptionMapper);

      assertThat(result.value()).isSameAs(mappedException);
    }

    @Test
    void recoverWithShouldReturnOutcomeOfApplyingExceptionMapperToException() {
      var exception = new Exception("test");
      var failure = new Failure<String>(exception);
      var mappedException = new Failure<String>(new Exception("mapped"));
      var exceptionMapper = (Function<Throwable, Result<String>>) mock(Function.class);
      when(exceptionMapper.apply(exception)).thenReturn(mappedException);

      Result<String> result = failure.recoverWith(exceptionMapper);

      assertThat(result).isSameAs(mappedException);
    }

    @Test
    void toOptionShouldReturnEmptyOptional() {
      var failure = new Failure<>(new Exception("test"));

      Optional<Object> result = failure.toOptional();

      assertThat(result).isEmpty();
    }

    @Test
    void equalsAndHashCodeShouldSatisfyContract() {
      EqualsVerifier.forClass(Success.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringShouldReturnFailureWithToStringOfException() {
      var exceptionString = "test";
      var exception = mock(Throwable.class);
      when(exception.toString()).thenReturn(exceptionString);
      var failure = new Failure<>(exception);

      String result = failure.toString();

      assertThat(result).isEqualTo("Failure(" + exceptionString + ")");
    }
  }
}
