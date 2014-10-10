package fr.esrf.icat.client;

/*
 * #%L
 * ICAT client API
 * %%
 * Copyright (C) 2014 ESRF - The European Synchrotron
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


import java.util.List;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public abstract class SimpleICATClientSkeleton implements SimpleICATClient {

	public static final long ONE_MINUTE_IN_MS = 60 * 1000l;
	
	/**
	 * Time before current session expires when we will perform a refresh.
	 * Set to 5 minutes.
	 */
	private static final long REFRESH_DELAY = 5 * ONE_MINUTE_IN_MS;
	
	private String icatBaseUrl;
	private String icatAuthnPlugin;
	private String icatUsername;
	private String icatPassword;
	
	private volatile long expiringTime;
	private final Object sessionLock;

	public SimpleICATClientSkeleton() {
		super();
		expiringTime = 0l;
		sessionLock = new Object();
	}

	@Override
	public String getIcatBaseUrl() {
		return icatBaseUrl;
	}

	@Override
	public void setIcatBaseUrl(String icatBaseUrl) {
		this.icatBaseUrl = icatBaseUrl;
	}

	@Override
	public String getIcatAuthnPlugin() {
		return icatAuthnPlugin;
	}

	@Override
	public void setIcatAuthnPlugin(String icatAuthnPlugin) {
		this.icatAuthnPlugin = icatAuthnPlugin;
	}

	@Override
	public String getIcatUsername() {
		return icatUsername;
	}

	@Override
	public void setIcatUsername(String icatUsername) {
		this.icatUsername = icatUsername;
	}

	@Override
	public String getIcatPassword() {
		return icatPassword;
	}

	@Override
	public void setIcatPassword(String icatPassword) {
		this.icatPassword = icatPassword;
	}

	@Override
	public void checkConnection() throws ICATClientException {
		long now = System.currentTimeMillis();
		if(now > expiringTime) {
			synchronized (sessionLock) {
				if(now > expiringTime) {
					expiringTime = initiateConnection();
				}
			}
		} else if ((expiringTime - now) < REFRESH_DELAY) {
			synchronized (sessionLock) {
				if ((expiringTime - now) < REFRESH_DELAY) {
					expiringTime = refreshConnection();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#init()
	 */
	@Override
	public void init() throws ICATClientException {
		doInit();
		expiringTime = initiateConnection();
		populateObjectCache();
	}
	
	/**
	 * Init method for implementors. After this method is called the client has to be able to connect to ICAT.
	 */
	public void doInit() {
	}

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#stop()
	 */
	@Override
	public void stop() {
		doStop();
		if(System.currentTimeMillis() < expiringTime) {
			closeConnection();
		}
	}
	
	/**
	 * Cleanup method for implementors. Called by stop() before closing the session. 
	 */
	public void doStop() {
	}

	@Override
	public abstract long initiateConnection() throws ICATClientException;

	@Override
	public abstract long refreshConnection() throws ICATClientException;

	@Override
	public abstract void closeConnection();

	@Override
	public void populateObjectCache() throws ICATClientException {
	}

	@Override
	public abstract void deleteEntities(String entityName, Long... ids) throws ICATClientException;

	@Override
	public abstract WrappedEntityBean get(String entity, long id) throws ICATClientException;

	@Override
	public abstract List<WrappedEntityBean> search(String query) throws ICATClientException;

	@Override
	public abstract void update(WrappedEntityBean bean) throws ICATClientException;

	@Override
	public abstract WrappedEntityBean create(String entity) throws ICATClientException;

	@Override
	public abstract long create(WrappedEntityBean bean) throws ICATClientException;

	@Override
	public abstract List<String> getEntityNames() throws ICATClientException;

}
