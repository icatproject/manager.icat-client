package fr.esrf.icat.client;

import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public abstract class ICATClient {

	protected static final String ICAT_SERVICE_NAME = "ICATService";

	protected static final String ICATPROJECT_NAMESPACE = "http://icatproject.org";

	protected static final String ICAT_SERVICE_URL = "/ICATService/ICAT?wsdl";

	private String icatBaseUrl;
	
	private String icatAuthnPlugin;

	private String icatUsername;

	private String icatPassword;

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

	@PostConstruct
	public abstract void init();
	
	@PreDestroy
	public abstract void stop();
	
	public abstract void deleteEntities(String entityName, Long... ids) throws ICATClientException;
	
	public abstract boolean investigationExists(String investigation) throws ICATClientException;
	
	public abstract long createInvestigation(String name, String type, String visit, String title, String instrument) throws ICATClientException;

	public abstract long createDataset(String investigation, String instrument, String name, String location, GregorianCalendar date) throws ICATClientException;

	public abstract long createDatafile(long datasetID, String filename, String location, String format)  throws ICATClientException;

	public abstract long createDatasetParameter(long datasetID, String parameter, String value) throws ICATClientException;

}
