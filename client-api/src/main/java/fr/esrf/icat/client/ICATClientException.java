package fr.esrf.icat.client;

public class ICATClientException extends Exception {

	private static final long serialVersionUID = -8568934144045347810L;

	public ICATClientException() {
		super();
	}

	public ICATClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ICATClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ICATClientException(String message) {
		super(message);
	}

	public ICATClientException(Throwable cause) {
		super(cause);
	}
	

}
