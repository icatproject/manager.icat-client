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


import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;


public class DelegatedICATClient extends ICATClientSkeleton {

	private ICATClient delegate;

	public DelegatedICATClient() {
		super();
	}

	public SimpleICATClient getDelegate() {
		return delegate;
	}

	public void setDelegate(final ICATClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public void init() {
	}

	@Override
	public void checkConnection() throws ICATClientException {
		delegate.checkConnection();
	}

	@Override
	public long initiateConnection() throws ICATClientException {
		return delegate.initiateConnection();
	}

	@Override
	public long refreshConnection() throws ICATClientException {
		return delegate.refreshConnection();
	}

	@Override
	public void closeConnection() {
		delegate.closeConnection();
	}

	@Override
	public void stop() {
	}

	@Override
	public void populateObjectCache() throws ICATClientException {
		delegate.populateObjectCache();
	}

	@Override
	public boolean investigationExists(final String investigation, final String visit) throws ICATClientException {
		return delegate.investigationExists(investigation, visit);
	}

	@Override
	public long createInvestigation(final String name, final String type, final String visit, final String title, final String instrument, final GregorianCalendar startDate) throws ICATClientException {
		return delegate.createInvestigation(name, type, visit, title, instrument, startDate);
	}

	@Override
	public void updateInvestigationDescription(final String name, final String visit, final String description) throws ICATClientException {
		delegate.updateInvestigationDescription(name, visit, description);
	}

	@Override
	public long createDataset(final String investigation, final String instrument, final String sampleName, final String name, final String location, final GregorianCalendar startDate, final GregorianCalendar endDate, final String comment) throws ICATClientException {
		return delegate.createDataset(investigation, instrument, sampleName, name, location, startDate, endDate, comment);
	}

	@Override
	public long createDatafile(final long datasetID, final String filename, final String location, final String format, final long size) throws ICATClientException {
		return delegate.createDatafile(datasetID, filename, location, format, size);
	}

	@Override
	public long createDatasetParameter(final long datasetID, final String parameter, final String value) throws ICATClientException {
		return delegate.createDatasetParameter(datasetID, parameter, value);
	}

	@Override
	public void deleteEntities(final String entityName, final Long... ids) throws ICATClientException {
		delegate.deleteEntities(entityName, ids);
	}

	@Override
	public List<Long> updateInvestigationUsers(final String name, final String visit, final Collection<UserDTO> users) throws ICATClientException {
		return delegate.updateInvestigationUsers(name, visit, users);
		
	}

	@Override
	public long createDatafile(long datasetID, DatafileDTO datafileData) throws ICATClientException {
		return delegate.createDatafile(datasetID, datafileData);
	}

	@Override
	public void createDatafiles(long datasetID, Collection<DatafileDTO> datafileCollection) throws ICATClientException {
		delegate.createDatafiles(datasetID, datafileCollection);
	}

	@Override
	public long createDatasetParameter(long datasetID, DatasetParameterDTO datasetParamData) throws ICATClientException {
		return delegate.createDatasetParameter(datasetID, datasetParamData);
	}

	@Override
	public void createDatasetParameters(long datasetID, Collection<DatasetParameterDTO> datasetParamCollection) throws ICATClientException {
		delegate.createDatasetParameters(datasetID, datasetParamCollection);
	}

	@Override
	public WrappedEntityBean get(String entity, long id) throws ICATClientException {
		return delegate.get(entity, id);
	}

	@Override
	public List<WrappedEntityBean> search(String query) throws ICATClientException {
		return delegate.search(query);
	}

	@Override
	public void update(WrappedEntityBean bean) throws ICATClientException {
		delegate.update(bean);
	}

	@Override
	public WrappedEntityBean create(String entity) throws ICATClientException {
		return delegate.create(entity);
	}

	@Override
	public long create(WrappedEntityBean bean) throws ICATClientException {
		return delegate.create(bean);
	}

	@Override
	public List<String> getEntityNames() throws ICATClientException {
		return delegate.getEntityNames();
	}

	@Override
	public void delete(WrappedEntityBean bean) throws ICATClientException {
		delegate.delete(bean);
	}

	@Override
	public String getServerVersion() throws ICATClientException {
		return delegate.getServerVersion();
	}

	@Override
	public void delete(final List<WrappedEntityBean> beans) throws ICATClientException {
		delegate.delete(beans);
		
	}

}
