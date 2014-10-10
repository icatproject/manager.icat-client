package fr.esrf.icat.client.dynamic;

/*
 * #%L
 * Dynamic ICAT client
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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClientSkeleton;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class DynamicSimpleICATClient extends SimpleICATClientSkeleton {

	private static final String MINIMUM_SUPPORTED_VERSION = "4.2";

	private final static Logger LOG = LoggerFactory.getLogger(DynamicSimpleICATClient.class);

	private static final String DOT_SPLIT_PATTERN = "\\.";

	private static final String QUOTE = "\"";

	private static final String SCHEMA_LOCATION_TAG = "schemaLocation=";

	private final static String SCHEMA_LOCATION_PATTERN = SCHEMA_LOCATION_TAG + QUOTE + "(.+?)" + QUOTE;
	
	private final static String ELEMENT_REF_PATTERN = "ref=\"tns:(.+?)\"";

	private final static String ELEMENT_REF_REPLACE = "name=\"$1\" type=\"tns:$1\"";
	
	private Client client;
	
	private String sessionId;

	private String packageName;

	private String version_string;
	
	private List<String> fileNameList;
	
	private List<String> entityList;
	
	public DynamicSimpleICATClient() {
		super();
		entityList = null;
	}

	@Override
	public void doInit() {
		InputStream urlStream = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		if (!ModifiedDynamicClientFactory.isCompilerAvailable()) {
			throw new IllegalStateException("This program needs a JDK to run !\nPlease see the README.txt file for how to configure it.");
		}
		
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
			
			final ModifiedDynamicClientFactory clientFactory = ModifiedDynamicClientFactory.newInstance();
			client = clientFactory.createClient(modifiedWSDL.toURI().toURL(), qName, bindings);
			fileNameList = clientFactory.getClassNameList();

			version_string = client.invoke("getApiVersion", (Object) null)[0].toString();
			LOG.debug("ICAT Version: "+ version_string);
			
			if(!isVersionAbove(MINIMUM_SUPPORTED_VERSION)) {
				throw new IllegalStateException("Minimum supported version is " + MINIMUM_SUPPORTED_VERSION);
			}
			
		} catch (Exception e) {
			LOG.error("Unable to initialise dynamic client:" + e.getMessage());
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
			LOG.error("Unable to create connection:" + e.getMessage());
			throw new ICATClientException(e);
		}
	}
	
	@Override
	public long refreshConnection() throws ICATClientException {
		if (isVersionAbove("4.3")) {
			try {
				client.invoke("refresh", sessionId);
				long remainingMinutes = (long) Math.floor((double) client.invoke("getRemainingMinutes", sessionId)[0]);
				return System.currentTimeMillis() + remainingMinutes * ONE_MINUTE_IN_MS;
			} catch (Exception e) {
				LOG.error("Unable to refresh connection:" + e.getMessage());
				throw new ICATClientException(e);
			}
		} else {
			LOG.info("ICAT version lower than 4.3, will create new connection instead of refreshing it");
			return initiateConnection();
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
	public void deleteEntities(final String entityName, final Long... ids) throws ICATClientException {
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
	public WrappedEntityBean get(final String entity, final long id) throws ICATClientException {
		checkConnection();
		try {
			return new WrappedEntityBean(client.invoke("get", sessionId, entity, id)[0]);
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public List<WrappedEntityBean> search(final String query) throws ICATClientException {
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
	public void update(final WrappedEntityBean bean) throws ICATClientException {
		checkConnection();
		try {
			client.invoke("update", sessionId, bean.getWrapped());
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public WrappedEntityBean create(final String entity) throws ICATClientException {
		try {
			return new WrappedEntityBean(Thread.currentThread().getContextClassLoader()
					.loadClass(packageName + "." + StringUtils.capitalize(entity)).newInstance());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public long create(final WrappedEntityBean bean) throws ICATClientException {
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
		if(null == entityList) {
			try {
				if (isVersionAbove("4.3")) {
					checkConnection();
					entityList = (List<String>) client.invoke("getEntityNames", sessionId)[0];
				} else {
					LOG.info("ICAT version lower than 4.3, will use compatibility getEntityNames");
					entityList = createEntityNames();
				}
			} catch (Exception e) {
				throw new ICATClientException(e);
			}
		}
		return entityList;
	}

	@Override
	public void delete(final WrappedEntityBean bean) throws ICATClientException {
		checkConnection();
		try {
			client.invoke("delete", sessionId, bean.getWrapped());
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public String getServerVersion() throws ICATClientException {
		return version_string;
	}
	
	private boolean isVersionAbove(final String version) {
		return compareVersionTo(version) >= 0;
	}

	// adapted from http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java?lq=1
	// protected for testing purpose
	protected final int compareVersionTo(final String otherVersion) {
	    final String[] vals1 = version_string.split(DOT_SPLIT_PATTERN);
	    final String[] vals2 = otherVersion.split(DOT_SPLIT_PATTERN);
	    int i = 0;
	    // set index to first non-equal ordinal or length of shortest version string
	    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
	      i++;
	    }
	    // compare first non-equal ordinal number
	    if (i < vals1.length && i < vals2.length) {
	        return Integer.signum(Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i])));
	    }
	    // the strings are equal or one string is a substring of the other
	    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
	}

	protected final List<String> createEntityNames() throws ClassNotFoundException {
		if(null == fileNameList) return null;
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		final Class<?> entityClass = contextClassLoader.loadClass(packageName + ".EntityBaseBean");
		final List<String> rep = new LinkedList<>();
		for(String name : fileNameList) {
			Class<?> clazz = contextClassLoader.loadClass(packageName + "." + name);
			if(entityClass.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
				rep.add(name);
			}
		}
		return rep;
	}

	@Override
	public void delete(final List<WrappedEntityBean> beans) throws ICATClientException {
		try {
			checkConnection();
			List<Object> raw = new LinkedList<Object>();
			for(WrappedEntityBean bean : beans) {
				raw.add(bean.getWrapped());
			}
			client.invoke("deleteMany", sessionId, raw);  
		} catch (Exception e) {
			throw new ICATClientException(e);
		}
	}

}
