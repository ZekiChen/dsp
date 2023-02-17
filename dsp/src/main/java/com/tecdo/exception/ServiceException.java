package com.tecdo.exception;

/**
 * 业务异常
 * <p>
 * Created by Zeki on 2023/2/17
 */
public class ServiceException extends RuntimeException{

    private static final long serialVersionUID = 2359767895161832954L;

    public ServiceException(String message) {
        super(message);
    }

    /**
     * 提高性能
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
