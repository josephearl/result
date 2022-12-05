package com.waracle.result;

/**
 * Thrown to indicate an attempt to throw a {@link Throwable} that is not an instance of {@link
 * Exception} or {@link Error}. The original {@link Throwable} is recorded as the {@code cause}.
 */
public class UnsupportedThrowableException extends RuntimeException {
  public UnsupportedThrowableException(String message, Throwable cause) {
    super(message, cause);
  }
}
