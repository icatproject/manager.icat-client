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


import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

public abstract class ICATClient {

	/**
	 * Name of dataset entity. Should not change.
	 */
	public static final String ENTITY_DATASET = "Dataset";

	/**
	 * Name of investigation entity. Should not change.
	 */
	public static final String ENTITY_INVESTIGATION = "Investigation";

	/**
	 * Name of user entity. Should not change.
	 */
	public static final String ENTITY_USER = "User";

	protected static final String ICAT_SERVICE_NAME = "ICATService";

	protected static final String ICATPROJECT_NAMESPACE = "http://icatproject.org";

	protected static final String ICAT_SERVICE_URL = "/ICATService/ICAT?wsdl";

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
	
	private Object sessionLock;
 
	public ICATClient() {
		super();
		expiringTime = 0l;
		sessionLock = new Object();
	}

	public String getIcatBaseUrl() {
		return icatBaseUrl;
	}

	public void setIcatBaseUrl(String icatBaseUrl) {
		this.icatBaseUrl = icatBaseUrl;
	}

	public String getIcatAuthnPlugin() {
		return icatAuthnPlugin;
	}

	public void setIcatAuthnPlugin(String icatAuthnPlugin) {
		this.icatAuthnPlugin = icatAuthnPlugin;
	}

	public String getIcatUsername() {
		return icatUsername;
	}

	public void setIcatUsername(String icatUsername) {
		this.icatUsername = icatUsername;
	}

	public String getIcatPassword() {
		return icatPassword;
	}

	public void setIcatPassword(String icatPassword) {
		this.icatPassword = icatPassword;
	}

	/**
	 * Method that check whether the current session is still valid. 
	 * Has to be called before any call to the web service.
	 * Delegates actual connection/refresh operation to the implementor.
	 * @throws ICATClientException 
	 * 
	 */
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

	/**
	 * Initiate an ICAT session.
	 * @return the expiring time (in currentTimeInMillis units).
	 * @throws ICATClientException
	 */
	public abstract long initiateConnection() throws ICATClientException;
	
	/**
	 * Refresh the ICAT session.
	 * @return the new expiring time (in currentTimeInMillis units).
	 * @throws ICATClientException
	 */
	public abstract long refreshConnection() throws ICATClientException;
	
	public abstract void closeConnection();
	
	public abstract void populateObjectCache() throws ICATClientException;

	public abstract void deleteEntities(String entityName, Long... ids) throws ICATClientException;
	
	public abstract boolean investigationExists(String investigation, String visit) throws ICATClientException;
	
	public abstract long createInvestigation(String name, String type, String visit, String title, String instrument, GregorianCalendar startDate) throws ICATClientException;

	public abstract void updateInvestigationDescription(String name, String visit, String description) throws ICATClientException;
	
	/**
	 * Add the given users to the investigation defined by name and visit.
	 * @param name the name of the investigation.
	 * @param visit the visitID of the investigation.
	 * @param users the list of users to add to that investigation.
	 * @return the list of ids of users created in the process. Already existing user ids are not returned. 
	 * @throws ICATClientException in case something goes wrong.
	 */
	public abstract List<Long> addInvestigationUsers(String name, String visit, Collection<UserDTO> users) throws ICATClientException;

	public abstract long createDataset(String investigation, String instrument, String sampleName, String name, String location, GregorianCalendar startDate, GregorianCalendar endDate, String comment) throws ICATClientException;

	public abstract long createDatafile(long datasetID, String filename, String location, String format)  throws ICATClientException;
	
	public long createDatafile(final long datasetID, final DatafileDTO datafileData) throws ICATClientException {
		return createDatafile(datasetID, datafileData.getFilename(), datafileData.getLocation(), datafileData.getFormat());
	}
	
	/**
	 * Default implementation. Override to use creatMany instead of a loop.
	 * @param datasetID
	 * @param datafileCollection
	 * @throws ICATClientException
	 */
	public void createDatafiles(final long datasetID, final Collection<DatafileDTO> datafileCollection) throws ICATClientException {
		for(DatafileDTO dtfd : datafileCollection) {
			createDatafile(datasetID, dtfd);
		}
	}

	public abstract long createDatasetParameter(long datasetID, String parameter, String value) throws ICATClientException;
	
	public long createDatasetParameter(final long datasetID, final DatasetParameterDTO datasetParamData) throws ICATClientException {
		return createDatasetParameter(datasetID, datasetParamData.getParameter(), datasetParamData.getValue());
	}
	
	/**
	 * Default implementation. Override to use creatMany instead of a loop.
	 * @param datasetID
	 * @param datasetParamCollection
	 * @throws ICATClientException
	 */
	public void createDatasetParameters(final long datasetID, final Collection<DatasetParameterDTO> datasetParamCollection)  throws ICATClientException {
		for(DatasetParameterDTO dtspd : datasetParamCollection) {
			createDatasetParameter(datasetID, dtspd);
		}
	}
}
