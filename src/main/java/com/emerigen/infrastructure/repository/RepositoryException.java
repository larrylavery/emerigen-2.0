package com.emerigen.infrastructure.repository;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.environment.Environment;

public class RepositoryException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(RepositoryException.class);

	public RepositoryException(String message) {
		super(message);
	}

	public RepositoryException(Throwable cause) {
		super(cause);
	}

	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public RepositoryException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
