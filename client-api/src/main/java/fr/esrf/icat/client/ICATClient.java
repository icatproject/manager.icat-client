package fr.esrf.icat.client;

import java.util.Collection;
import java.util.GregorianCalendar;

public abstract class ICATClient {

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
	public final void checkConnection() throws ICATClientException {
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
	
	public final void init() throws ICATClientException {
		doInit();
		expiringTime = initiateConnection();
		populateObjectCache();
	}
	
	/**
	 * Init method for implementors. After this method is called the client has to be able to connect to ICAT.
	 */
	public void doInit() {
	}

	public final void stop() {
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
	
	public abstract boolean investigationExists(String investigation) throws ICATClientException;
	
	public abstract long createInvestigation(String name, String type, String visit, String title, String instrument) throws ICATClientException;

	public abstract long createDataset(String investigation, String instrument, String name, String location, GregorianCalendar date) throws ICATClientException;

	public abstract long createDatafile(long datasetID, String filename, String location, String format)  throws ICATClientException;
	
	public long createDatafile(long datasetID, DatafileDTO datafileData) throws ICATClientException {
		return createDatafile(datasetID, datafileData.getFilename(), datafileData.getLocation(), datafileData.getFormat());
	}
	
	/**
	 * Default implementation. Override to use creatMany instead of a loop.
	 * @param datasetID
	 * @param datafileCollection
	 * @throws ICATClientException
	 */
	public void createDatafiles(long datasetID, Collection<DatafileDTO> datafileCollection) throws ICATClientException {
		for(DatafileDTO dtfd : datafileCollection) {
			createDatafile(datasetID, dtfd);
		}
	}

	public abstract long createDatasetParameter(long datasetID, String parameter, String value) throws ICATClientException;
	
	public long createDatasetParameter(long datasetID, DatasetParameterDTO datasetParamData) throws ICATClientException {
		return createDatasetParameter(datasetID, datasetParamData.getParameter(), datasetParamData.getValue());
	}
	
	/**
	 * Default implementation. Override to use creatMany instead of a loop.
	 * @param datasetID
	 * @param datasetParamCollection
	 * @throws ICATClientException
	 */
	public void createDatasetParameters(long datasetID, Collection<DatasetParameterDTO> datasetParamCollection)  throws ICATClientException {
		for(DatasetParameterDTO dtspd : datasetParamCollection) {
			createDatasetParameter(datasetID, dtspd);
		}
	}
}
