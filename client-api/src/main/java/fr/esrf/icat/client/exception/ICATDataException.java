package fr.esrf.icat.client.exception;

/*
 * #%L
 * ICAT client API
 * %%
 * Copyright (C) 2014 - 2015 ESRF - The European Synchrotron
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
