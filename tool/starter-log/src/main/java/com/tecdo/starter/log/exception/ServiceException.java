package com.tecdo.starter.log.exception;

import com.tecdo.core.launch.response.RCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>
 * Created by Zeki on 2023/2/17
 */
public class ServiceException extends RuntimeException{

    private static final long serialVersionUID = 2359767895161832954L;

    @Getter
    private final RCode rCode;

    public ServiceException(String message) {
        super(message);
        this.rCode = RCode.FAILURE;
    }

    public ServiceException(RCode rCode) {
        super(rCode.getMessage());
        this.rCode = rCode;
    }

    public ServiceException(RCode rCode, Throwable cause) {
        super(cause);
        this.rCode = rCode;
    }

    /**
     * 提高性能
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
