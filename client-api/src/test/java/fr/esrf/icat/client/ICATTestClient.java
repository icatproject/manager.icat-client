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
	public List<Long> updateInvestigationUsers(String name, String visit, Collection<UserDTO> users) throws ICATClientException {
		List<Long> ids = super.updateInvestigationUsers(name, visit, users);
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
