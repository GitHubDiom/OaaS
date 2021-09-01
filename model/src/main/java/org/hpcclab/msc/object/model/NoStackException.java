package org.hpcclab.msc.object.model;

public class NoStackException extends RuntimeException{

  int code = 500;

  public NoStackException(String message) {
    super(message, null, true, true);
  }

  public NoStackException(String message, Throwable cause) {
    super(message, cause, false, false);
  }

  public int getCode() {
    return code;
  }

  public NoStackException setCode(int code) {
    this.code = code;
    return this;
  }
}
