package com.github.wuchao.activiti.exception;

/**
 * 参数异常
 */
public class ParamsException extends RuntimeException {

    private final static String DEFAULT_MESSAGE = "invalid parameter value";

    private final String message;

    public ParamsException(String message) {
        super(message);
        this.message = message;
    }

    public ParamsException() {
        super(DEFAULT_MESSAGE);
        this.message = DEFAULT_MESSAGE;
    }
}
