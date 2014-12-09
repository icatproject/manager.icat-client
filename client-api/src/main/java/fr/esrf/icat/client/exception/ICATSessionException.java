package fr.esrf.icat.client.exception;

import fr.esrf.icat.client.ICATClientException;

public class ICATSessionException extends ICATClientException {

	private static final long serialVersionUID = -7417166986630437620L;

	public ICATSessionException() {
		super();
	}

	public ICATSessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ICATSessionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ICATSessionException(String message) {
		super(message);
	}

	public ICATSessionException(Throwable cause) {
		super(cause);
	}

}
