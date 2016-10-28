package org.nebula.framework.retry;

public class RetryException extends RuntimeException {

  public RetryException() {
  }

  public RetryException(String exceptionMessage) {
    super(exceptionMessage);
  }

  public RetryException(String exceptionMessage, Throwable reason) {
    super(exceptionMessage, reason);
  }

  public RetryException(Throwable reason) {
    super(reason);
  }
}
