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

public interface SimpleICATClient {

	public static final String ICAT_SERVICE_NAME = "ICATService";
	public static final String ICATPROJECT_NAMESPACE = "http://icatproject.org";
	public static final String ICAT_SERVICE_URL = "/ICATService/ICAT?wsdl";

	public abstract String getIcatBaseUrl();

	public abstract void setIcatBaseUrl(String icatBaseUrl);

	public abstract String getIcatAuthnPlugin();

	public abstract void setIcatAuthnPlugin(String icatAuthnPlugin);

	public abstract String getIcatUsername();

	public abstract void setIcatUsername(String icatUsername);

	public abstract String getIcatPassword();

	public abstract void setIcatPassword(String icatPassword);

	/**
	 * Method that check whether the current session is still valid. 
	 * Has to be called before any call to the web service.
	 * Delegates actual connection/refresh operation to the implementor.
	 * @throws ICATClientException 
	 * 
	 */
	public abstract void checkConnection() throws ICATClientException;

	public abstract void init() throws ICATClientException;

	public abstract void stop();

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

	public abstract WrappedEntityBean get(String entity, long id) throws ICATClientException;

	public abstract List<WrappedEntityBean> search(String query) throws ICATClientException;

	public abstract void update(WrappedEntityBean bean) throws ICATClientException;

	public abstract WrappedEntityBean create(String entity) throws ICATClientException;

	public abstract long create(WrappedEntityBean bean) throws ICATClientException;

	public abstract List<String> getEntityNames() throws ICATClientException;
	
	public abstract void delete(WrappedEntityBean bean) throws ICATClientException;
	
	public abstract void delete(List<WrappedEntityBean> beans) throws ICATClientException;

	public abstract String getServerVersion() throws ICATClientException;

}