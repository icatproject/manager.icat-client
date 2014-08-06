package fr.esrf.icat.client.dynamic;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.jaxb.JAXBUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClientSkeleton;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class DynamicSimpleICATClient extends SimpleICATClientSkeleton {

	private final static Logger LOG = LoggerFactory.getLogger(DynamicSimpleICATClient.class);
	
	private Client client;
	
	private String sessionId;

	private String packageName;
	
	public DynamicSimpleICATClient() {
		super();
	}

	@Override
	public void doInit() {
		try {
			final String icatBaseUrl = getIcatBaseUrl();
			URL icatUrl = new URL(new URL(icatBaseUrl), ICAT_SERVICE_URL);
			LOG.debug("Using ICAT service at " + icatUrl.toString());
			QName qName = new QName(ICATPROJECT_NAMESPACE, ICAT_SERVICE_NAME);
			
			packageName = JAXBUtils.namespaceURIToPackage(icatBaseUrl);
			File file = org.apache.cxf.tools.util.JAXBUtils.getPackageMappingSchemaBindingFile(ICATPROJECT_NAMESPACE, packageName);
			List<String> bindings = new LinkedList<>();
			bindings.add(file.toURI().toString());
			
			client = JaxWsDynamicClientFactory.newInstance().createClient(icatUrl, qName, bindings);
//			serviceInfo = client.getEndpoint().getService().getServiceInfos().get(0);
			final Object[] response = client.invoke("getApiVersion", (Object) null);
			LOG.debug("ICAT Version: "+ response[0].toString());
		} catch (Exception e) {
			LOG.error("Unable to initialise dynamic client", e);
			throw new IllegalStateException("Unable to initialise DynamicSimpleICATClient", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public long initiateConnection() throws ICATClientException {
		LOG.debug("Connecting to ICAT as '" + getIcatUsername() + "'");

		try {
			final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			final Class<?> loginClass = contextClassLoader.loadClass(packageName + ".Login");
			final Class<?> credentialsClass = contextClassLoader.loadClass(packageName + ".Login$Credentials");
			final Object credentials = credentialsClass.newInstance();
			
			final Object entries = credentialsClass.getMethod("getEntry", (Class<?>[]) null).invoke(credentials, (Object[])null);
			
			final Class<?> entryClass = contextClassLoader.loadClass(packageName + ".Login$Credentials$Entry");
			
			final Class<?>[] stringParam = new Class<?>[]{String.class};

			Object entry = entryClass.newInstance();
			entryClass.getMethod("setKey", stringParam).invoke(entry, "username");
			entryClass.getMethod("setValue", stringParam).invoke(entry, getIcatUsername());
			
			((List<Object>)entries).add(entry);

			entry = entryClass.newInstance();
			entryClass.getMethod("setKey", stringParam).invoke(entry, "password");
			entryClass.getMethod("setValue", stringParam).invoke(entry, getIcatPassword());
			
			((List<Object>)entries).add(entry);
			
			final Object login = loginClass.newInstance();
			loginClass.getMethod("setPlugin", stringParam).invoke(login, getIcatAuthnPlugin());
			loginClass.getMethod("setCredentials", new Class<?>[]{credentialsClass}).invoke(login, credentials);
			
			sessionId = client.invokeWrapped("login", login)[0].toString();
			LOG.debug("Connected to ICAT [" + sessionId + "]");
			
			long remainingMinutes = (long) Math.floor((double) client.invoke("getRemainingMinutes", sessionId)[0]);
			return System.currentTimeMillis() + remainingMinutes * ONE_MINUTE_IN_MS;

		} catch (Exception e) {
			LOG.error("Unable to create connection", e);
			throw new ICATClientException(e);
		}
	}
	
	@Override
	public long refreshConnection() throws ICATClientException {
		try {
			client.invoke("refresh", sessionId);
			long remainingMinutes = (long) Math.floor((double) client.invoke("getRemainingMinutes", sessionId)[0]);
			return System.currentTimeMillis() + remainingMinutes * ONE_MINUTE_IN_MS;
		} catch (Exception e) {
			LOG.error("Unable to refresh connection", e);
			throw new ICATClientException(e);
		}
	}

	@Override
	public void closeConnection() {
		if(null != client && null != sessionId) {
			try {
				LOG.debug("Closing session " + sessionId);
				client.invoke("logout", sessionId);
				sessionId = null;
				LOG.debug("Session closed");
			} catch (Exception e) {
				LOG.warn("Unable to close ICAT session", e);
			}
		}
	}

	@Override
	public void deleteEntities(String entityName, Long... ids) throws ICATClientException {
		if(null == ids || ids.length == 0) {
			return;
		}
		StringBuilder query = new StringBuilder();
		query.append(entityName);
		query.append(" [id IN (");
		for(long id : ids) {
			query.append(Long.toString(id));
			query.append(",");
		}
		query.setCharAt(query.length() - 1, ')');
		query.append("]");
		try {
			checkConnection();
			
			List<WrappedEntityBean> result = search(query.toString());
			List<Object> raw = new LinkedList<Object>(result);
			client.invoke("deleteMany", sessionId, raw);  
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public WrappedEntityBean get(String entity, long id) throws ICATClientException {
		checkConnection();
		try {
			return new WrappedEntityBean(client.invoke("get", sessionId, entity, id)[0]);
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public List<WrappedEntityBean> search(String query) throws ICATClientException {
		checkConnection();
		try {
			List<?> l = (List<?>) client.invoke("search", sessionId, query)[0];
			List<WrappedEntityBean> retL = new LinkedList<>();
			for(Object o : l) {
				retL.add(new WrappedEntityBean(o));
			}
			return retL;
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public void update(WrappedEntityBean bean) throws ICATClientException {
		checkConnection();
		try {
			client.invoke("update", sessionId, bean.getWrapped());
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public WrappedEntityBean create(String entity) throws ICATClientException {
		try {
			return new WrappedEntityBean(Thread.currentThread().getContextClassLoader()
					.loadClass(packageName + "." + StringUtils.capitalize(entity)).newInstance());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public long create(WrappedEntityBean bean) throws ICATClientException {
		checkConnection();
		try {
			return (long) client.invoke("create", sessionId, bean.getWrapped())[0];
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getEntityNames() throws ICATClientException {
		checkConnection();
		try {
			return (List<String>) client.invoke("getEntityNames", sessionId)[0];
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

}
