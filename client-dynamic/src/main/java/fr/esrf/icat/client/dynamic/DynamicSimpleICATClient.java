package fr.esrf.icat.client.dynamic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final String QUOTE = "\"";

	private static final String SCHEMA_LOCATION_TAG = "schemaLocation=";

	private final static String SCHEMA_LOCATION_PATTERN = SCHEMA_LOCATION_TAG + QUOTE + "(.+?)" + QUOTE;
	
	private final static String ELEMENT_REF_PATTERN = "ref=\"tns:(.+?)\"";

	private final static String ELEMENT_REF_REPLACE = "name=\"$1\" type=\"tns:$1\"";
	
	private Client client;
	
	private String sessionId;

	private String packageName;
	
	public DynamicSimpleICATClient() {
		super();
	}

	@Override
	public void doInit() {
		InputStream urlStream = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			final String icatBaseUrl = getIcatBaseUrl();
			URL icatUrl = new URL(new URL(icatBaseUrl), ICAT_SERVICE_URL);
			LOG.debug("Using ICAT service at " + icatUrl.toString());
			QName qName = new QName(ICATPROJECT_NAMESPACE, ICAT_SERVICE_NAME);
			
			// ADDED: modify the wsdl on the fly to avoid the namespace issue
			// this will have to do until a better solution is found
			
			final File initialWSDL = File.createTempFile("initial", "wsdl");
			initialWSDL.deleteOnExit();
			
			urlStream = icatUrl.openStream();
			Files.copy(urlStream, initialWSDL.toPath(),  StandardCopyOption.REPLACE_EXISTING);
			urlStream.close();
			
			final File modifiedWSDL = File.createTempFile("modified", "wsdl");
			modifiedWSDL.deleteOnExit();
			
			final File modifiedXSD = File.createTempFile("modified", "xsd");
			modifiedXSD.deleteOnExit();

			reader = new BufferedReader(new FileReader(initialWSDL));
			writer = new BufferedWriter(new FileWriter(modifiedWSDL));
			String line;
			String schemaLocationUrl = null;
			boolean found = false;
			Pattern pattern = Pattern.compile(SCHEMA_LOCATION_PATTERN);
			while((line = reader.readLine()) != null) {
				if(!found) {
					final Matcher matcher = pattern.matcher(line);
					if(matcher.find()) {
						StringBuffer sb = new StringBuffer();
						schemaLocationUrl = matcher.group(1);
						matcher.appendReplacement(sb, SCHEMA_LOCATION_TAG + QUOTE + modifiedXSD.toURI().toURL().toString() + QUOTE);
						matcher.appendTail(sb);
						line = sb.toString();
					}
				}
				writer.write(line);
				writer.newLine();
			}
			reader.close();
			writer.close();
			
			if(null != schemaLocationUrl) {
				
				final File initialXSD = File.createTempFile("initial", "xsd");
				initialXSD.deleteOnExit();
				
				urlStream = new URL(schemaLocationUrl).openStream();
				Files.copy(urlStream, initialXSD.toPath(),  StandardCopyOption.REPLACE_EXISTING);
				urlStream.close();
				
				reader = new BufferedReader(new FileReader(initialXSD));
				writer = new BufferedWriter(new FileWriter(modifiedXSD));
				pattern = Pattern.compile(ELEMENT_REF_PATTERN);
				while((line = reader.readLine()) != null) {
					line = pattern.matcher(line).replaceAll(ELEMENT_REF_REPLACE);
					writer.write(line);
					writer.newLine();
				}
				reader.close();
				writer.close();
				
			}
			
			// END ADDED
			
			packageName = JAXBUtils.namespaceURIToPackage(icatBaseUrl);
			File file = org.apache.cxf.tools.util.JAXBUtils.getPackageMappingSchemaBindingFile(ICATPROJECT_NAMESPACE, packageName);
			List<String> bindings = new LinkedList<>();
			bindings.add(file.toURI().toString());
			
			client = JaxWsDynamicClientFactory.newInstance().createClient(modifiedWSDL.toURI().toURL(), qName, bindings);

			final Object[] response = client.invoke("getApiVersion", (Object) null);
			LOG.debug("ICAT Version: "+ response[0].toString());
		} catch (Exception e) {
			LOG.error("Unable to initialise dynamic client", e);
			throw new IllegalStateException("Unable to initialise DynamicSimpleICATClient", e);
		} finally {
			close(urlStream);
			close(reader);
			close(writer);
		}
	}

	private void close(final Closeable closeable) {
		if(null != closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOG.warn("Error closing Closeable " + closeable.toString(), e);
			}
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
			List<Object> raw = new LinkedList<Object>();
			for(WrappedEntityBean bean : result) {
				raw.add(bean.getWrapped());
			}
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

	@Override
	public void delete(WrappedEntityBean bean) throws ICATClientException {
		checkConnection();
		try {
			client.invoke("delete", sessionId, bean.getWrapped());
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public String getServerVersion() throws ICATClientException {
		checkConnection();
		try {
			return client.invoke("getApiVersion", sessionId)[0].toString();
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

}
