package com.tecdo.exception;

import com.tecdo.core.launch.response.RCode;
import lombok.Getter;

/**
 * dsp服务业务异常
 * <p>
 * Created by Zeki on 2023/2/17
 */
public class DspException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final RCode rCode;

    public DspException(String message) {
        super(message);
        this.rCode = RCode.FAILURE;
    }

    public DspException(RCode rCode) {
        super(rCode.getMessage());
        this.rCode = rCode;
    }

    public DspException(RCode rCode, Throwable cause) {
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
