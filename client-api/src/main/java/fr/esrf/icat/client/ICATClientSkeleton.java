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

public abstract class ICATClientSkeleton extends SimpleICATClientSkeleton implements ICATClient {

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

	public ICATClientSkeleton() {
		super();
	}

	@Override
	public abstract void populateObjectCache() throws ICATClientException;

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#investigationExists(java.lang.String, java.lang.String)
	 */
	@Override
	public abstract boolean investigationExists(String investigation, String visit) throws ICATClientException;
	
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createInvestigation(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.GregorianCalendar)
	 */
	@Override
	public abstract long createInvestigation(String name, String type, String visit, String title, String instrument, GregorianCalendar startDate) throws ICATClientException;

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#updateInvestigationDescription(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public abstract void updateInvestigationDescription(String name, String visit, String description) throws ICATClientException;
	
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#updateInvestigationUsers(java.lang.String, java.lang.String, java.util.Collection)
	 */
	@Override
	public abstract List<Long> updateInvestigationUsers(String name, String visit, Collection<UserDTO> users) throws ICATClientException;

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDataset(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.GregorianCalendar, java.util.GregorianCalendar, java.lang.String)
	 */
	@Override
	public abstract long createDataset(String investigation, String instrument, String sampleName, String name, String location, GregorianCalendar startDate, GregorianCalendar endDate, String comment) throws ICATClientException;

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDatafile(long, java.lang.String, java.lang.String, java.lang.String, long)
	 */
	@Override
	public abstract long createDatafile(long datasetID, String filename, String location, String format, long size)  throws ICATClientException;
	
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDatafile(long, fr.esrf.icat.client.DatafileDTO)
	 */
	@Override
	public long createDatafile(final long datasetID, final DatafileDTO datafileData) throws ICATClientException {
		return createDatafile(datasetID, datafileData.getFilename(), datafileData.getLocation(), datafileData.getFormat(), datafileData.getSize());
	}
	
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDatafiles(long, java.util.Collection)
	 */
	@Override
	public void createDatafiles(final long datasetID, final Collection<DatafileDTO> datafileCollection) throws ICATClientException {
		for(DatafileDTO dtfd : datafileCollection) {
			createDatafile(datasetID, dtfd);
		}
	}

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDatasetParameter(long, java.lang.String, java.lang.String)
	 */
	@Override
	public abstract long createDatasetParameter(long datasetID, String parameter, String value) throws ICATClientException;
	
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDatasetParameter(long, fr.esrf.icat.client.DatasetParameterDTO)
	 */
	@Override
	public long createDatasetParameter(final long datasetID, final DatasetParameterDTO datasetParamData) throws ICATClientException {
		return createDatasetParameter(datasetID, datasetParamData.getParameter(), datasetParamData.getValue());
	}
	
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDatasetParameters(long, java.util.Collection)
	 */
	@Override
	public void createDatasetParameters(final long datasetID, final Collection<DatasetParameterDTO> datasetParamCollection)  throws ICATClientException {
		for(DatasetParameterDTO dtspd : datasetParamCollection) {
			createDatasetParameter(datasetID, dtspd);
		}
	}
}
