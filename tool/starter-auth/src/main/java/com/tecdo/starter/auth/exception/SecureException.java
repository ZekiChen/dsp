package com.tecdo.starter.auth.exception;

import com.tecdo.core.launch.response.RCode;
import lombok.Getter;

/**
 * Created by Zeki on 2023/3/14
 */
public class SecureException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Getter
	private final RCode rCode;

	public SecureException(String message) {
		super(message);
		this.rCode = RCode.UN_AUTHORIZED;
	}

	public SecureException(RCode rCode) {
		super(rCode.getMessage());
		this.rCode = rCode;
	}

	public SecureException(RCode rCode, Throwable cause) {
		super(cause);
		this.rCode = rCode;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
