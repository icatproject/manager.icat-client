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

public abstract class ICATClientSkeleton extends SimpleICATClientSkeleton implements ICATClient {

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
	public long createDataset(String investigation, String instrument, String sampleName, String name, String location, GregorianCalendar startDate, GregorianCalendar endDate, String comment) throws ICATClientException {
		return createDataset(investigation, instrument, sampleName, name, location, startDate, endDate, comment, true);
	}

	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.ICATClient#createDataset(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.GregorianCalendar, java.util.GregorianCalendar, java.lang.String)
	 */
	@Override
	public abstract long createDataset(String investigation, String instrument, String sampleName, String name, String location, GregorianCalendar startDate, GregorianCalendar endDate, String comment, boolean complete) throws ICATClientException;

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
