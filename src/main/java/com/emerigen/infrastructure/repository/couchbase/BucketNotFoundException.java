package com.emerigen.infrastructure.repository.couchbase;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.environment.Environment;
import com.emerigen.infrastructure.repository.RepositoryException;

public class BucketNotFoundException extends RepositoryException {

	static Logger logger = Logger.getLogger(BucketNotFoundException.class);
	
	private static final long serialVersionUID = 1L;

	public BucketNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public BucketNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public BucketNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public BucketNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
