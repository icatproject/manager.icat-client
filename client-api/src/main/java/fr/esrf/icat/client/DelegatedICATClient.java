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


public class DelegatedICATClient extends ICATClient {

	private ICATClient delegate;

	public DelegatedICATClient() {
		super();
	}

	public ICATClient getDelegate() {
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
	public List<Long> addInvestigationUsers(final String name, final String visit, final Collection<UserDTO> users) throws ICATClientException {
		return delegate.addInvestigationUsers(name, visit, users);
		
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

}