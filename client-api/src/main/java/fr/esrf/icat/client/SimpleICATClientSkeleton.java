package fr.esrf.icat.client;

/*
 * #%L
 * ICAT client API
 * %%
 * Copyright (C) 2014 ESRF - The European Synchrotron
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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