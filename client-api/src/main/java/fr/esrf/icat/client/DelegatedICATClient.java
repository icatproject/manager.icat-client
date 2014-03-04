package fr.esrf.icat.client;

import java.util.GregorianCalendar;


public abstract class DelegatedICATClient extends ICATClient {

	private ICATClient delegate;

	public DelegatedICATClient() {
		super();
	}

	public ICATClient getDelegate() {
		return delegate;
	}

	public void setDelegate(ICATClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean investigationExists(final String investigation) throws ICATClientException {
		return delegate.investigationExists(investigation);
	}

	@Override
	public long createInvestigation(final String name, final String type, final String visit, final String title, final String instrument) throws ICATClientException {
		return delegate.createInvestigation(name, type, visit, title, instrument);
	}

	@Override
	public long createDataset(final String investigation, final String instrument, final String name, final String location, final GregorianCalendar date) throws ICATClientException {
		return delegate.createDataset(investigation, instrument, name, location, date);
	}

	@Override
	public long createDatafile(final long datasetID, final String filename, final String location, final String format) throws ICATClientException {
		return delegate.createDatafile(datasetID, filename, location, format);
	}

	@Override
	public long createDatasetParameter(final long datasetID, final String parameter, final String value) throws ICATClientException {
		return delegate.createDatasetParameter(datasetID, parameter, value);
	}

	@Override
	public void deleteEntities(final String entityName, final Long... ids) throws ICATClientException {
		delegate.deleteEntities(entityName, ids);
	}

}