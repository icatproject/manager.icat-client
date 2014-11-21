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

public interface ICATClient extends SimpleICATClient {

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

	public abstract boolean investigationExists(String investigation, String visit) throws ICATClientException;

	public abstract long createInvestigation(String name, String type, String visit, String title, String instrument, GregorianCalendar startDate) throws ICATClientException;

	public abstract void updateInvestigationDescription(String name, String visit, String description) throws ICATClientException;

	/**
	 * Updates list of users for the investigation defined by name and visit.
	 * @param name the name of the investigation.
	 * @param visit the visitID of the investigation.
	 * @param users the list of users attached to that investigation.
	 * @return the list of ids of users created in the process. Already existing user ids are not returned. 
	 * @throws ICATClientException in case something goes wrong.
	 */
	public abstract List<Long> updateInvestigationUsers(String name, String visit, Collection<UserDTO> users) throws ICATClientException;

	public abstract long createDataset(String investigation, String instrument, String sampleName, String name, String location, GregorianCalendar startDate, GregorianCalendar endDate, String comment) throws ICATClientException;

	public abstract long createDataset(String investigation, String instrument, String sampleName, String name, String location, GregorianCalendar startDate, GregorianCalendar endDate, String comment, boolean complete) throws ICATClientException;

	public abstract long createDatafile(long datasetID, String filename, String location, String format, long size) throws ICATClientException;

	public abstract long createDatafile(long datasetID, DatafileDTO datafileData) throws ICATClientException;

	/**
	 * Default implementation. Override to use creatMany instead of a loop.
	 * @param datasetID
	 * @param datafileCollection
	 * @throws ICATClientException
	 */
	public abstract void createDatafiles(long datasetID, Collection<DatafileDTO> datafileCollection) throws ICATClientException;

	public abstract long createDatasetParameter(long datasetID, String parameter, String value) throws ICATClientException;

	public abstract long createDatasetParameter(long datasetID, DatasetParameterDTO datasetParamData) throws ICATClientException;

	/**
	 * Default implementation. Override to use creatMany instead of a loop.
	 * @param datasetID
	 * @param datasetParamCollection
	 * @throws ICATClientException
	 */
	public abstract void createDatasetParameters(long datasetID, Collection<DatasetParameterDTO> datasetParamCollection) throws ICATClientException;

}
