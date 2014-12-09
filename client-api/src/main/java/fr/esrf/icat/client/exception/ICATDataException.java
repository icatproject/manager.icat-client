package fr.esrf.icat.client.exception;

import fr.esrf.icat.client.ICATClientException;

public class ICATDataException extends ICATClientException {

	private static final long serialVersionUID = -5502709455865188872L;

	public ICATDataException() {
		super();
	}

	public ICATDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ICATDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public ICATDataException(String message) {
		super(message);
	}

	public ICATDataException(Throwable cause) {
		super(cause);
	}

}
