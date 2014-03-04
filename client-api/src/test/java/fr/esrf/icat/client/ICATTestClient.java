package fr.esrf.icat.client;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICATTestClient extends DelegatedICATClient {

	private final static Logger LOG = LoggerFactory.getLogger(ICATTestClient.class);
	
	public final static String ENTITY_DATASET = "Dataset";
	public final static String ENTITY_INVESTIGATION = "Investigation";

	private List<Long> investigationList;

	private List<Long> datasetList;

	@Override
	public void init() {
		LOG.warn("Setting up ICAT client for automatic rollback at shutdown");
		investigationList = new LinkedList<Long>();
		datasetList = new LinkedList<Long>();
	}

	@Override
	public void stop() {
		try {
			LOG.debug("Removing created Entities from ICAT");
			deleteEntities(ENTITY_DATASET, datasetList.toArray(new Long[datasetList.size()]));
			datasetList = null;
			deleteEntities(ENTITY_INVESTIGATION, investigationList.toArray(new Long[investigationList.size()]));
			investigationList = null;
			LOG.debug("Created Entities removed from ICAT");
		} catch (ICATClientException e) {
			LOG.error("Unable to remove created entities", e);
		}
	}

	@Override
	public long createInvestigation(final String name, final String type, final String visit, final String title, final String instrument) throws ICATClientException {
		long id = super.createInvestigation(name, type, visit, title, instrument);
		investigationList.add(id);
		return id;
	}

	@Override
	public long createDataset(final String investigation, final String instrument, final String name, final String location, final GregorianCalendar date) throws ICATClientException {
		long id = super.createDataset(investigation, instrument, name, location, date);
		datasetList.add(id);
		return id;
	}

	@Override
	public void deleteEntities(String entityName, Long... ids) throws ICATClientException {
		// if entities are deleted for some other reason than stopping this component
		// we remove them from the list of entities to remove when stopping to avoid errors
		if(ENTITY_DATASET.equals(entityName)) {
			datasetList.removeAll(Arrays.asList(ids));
		} else if(ENTITY_INVESTIGATION.equals(entityName)) {
			investigationList.removeAll(Arrays.asList(ids));
		}
		// let the default delegate implementation do the job
		super.deleteEntities(entityName, ids);
	}

	
}
