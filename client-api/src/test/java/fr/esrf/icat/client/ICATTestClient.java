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


import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICATTestClient extends DelegatedICATClient {

	private final static Logger LOG = LoggerFactory.getLogger(ICATTestClient.class);
	
	private List<Long> investigationList;

	private List<Long> datasetList;
	
	private List<Long> userList;

	public ICATTestClient() {
		super();
		LOG.warn("Setting up ICAT client for automatic rollback at shutdown");
		investigationList = new LinkedList<Long>();
		datasetList = new LinkedList<Long>();
		userList = new LinkedList<Long>();
	}

	@Override
	public void stop() {
		try {
			LOG.warn("Removing created Entities from ICAT");
			deleteEntities(ENTITY_DATASET, datasetList.toArray(new Long[datasetList.size()]));
			datasetList = null;
			deleteEntities(ENTITY_INVESTIGATION, investigationList.toArray(new Long[investigationList.size()]));
			investigationList = null;
			deleteEntities(ENTITY_USER, userList.toArray(new Long[userList.size()]));
			userList = null;
			LOG.warn("Created Entities removed from ICAT");
		} catch (ICATClientException e) {
			LOG.error("Unable to remove created entities", e);
		}
	}

	@Override
	public long createInvestigation(final String name, final String type, final String visit, final String title, final String instrument, final GregorianCalendar startDate) throws ICATClientException {
		long id = super.createInvestigation(name, type, visit, title, instrument, startDate);
		investigationList.add(id);
		return id;
	}

	@Override
	public long createDataset(final String investigation, final String instrument, final String sampleName, final String name, final String location, final GregorianCalendar startDate, final GregorianCalendar endDate, final String comment) throws ICATClientException {
		long id = super.createDataset(investigation, instrument, sampleName, name, location, startDate, endDate, comment);
		datasetList.add(id);
		return id;
	}
	
	@Override
	public List<Long> addInvestigationUsers(String name, String visit, Collection<UserDTO> users) throws ICATClientException {
		List<Long> ids = super.addInvestigationUsers(name, visit, users);
		userList.addAll(ids);
		return ids;
	}

	@Override
	public void deleteEntities(final String entityName, final Long... ids) throws ICATClientException {
		// if entities are deleted for some other reason than stopping this component
		// we remove them from the list of entities to remove when stopping to avoid errors
		if(ENTITY_DATASET.equals(entityName)) {
			datasetList.removeAll(Arrays.asList(ids));
		} else if(ENTITY_INVESTIGATION.equals(entityName)) {
			investigationList.removeAll(Arrays.asList(ids));
		} else if (ENTITY_USER.equals(entityName)) {
			userList.removeAll(Arrays.asList(ids));
		}
		// let the default delegate implementation do the job
		super.deleteEntities(entityName, ids);
	}

	
}
