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