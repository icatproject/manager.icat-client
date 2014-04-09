package fr.esrf.icat.client.v4_3_1;

import static org.junit.Assert.*;

import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.esrf.icat.client.ICATClient;
import fr.esrf.icat.client.ICATClientException;

public class TestICATClientV4_3_1 {

	private ICATClient client;
	
	@Before
	public void setUp() throws Exception {
		client = new ICATClientImpl();
		client.setIcatAuthnPlugin("db");
		client.setIcatBaseUrl("https://ovm-icat-sandbox.esrf.fr:8181");
		client.setIcatUsername("root");
		client.setIcatPassword("password");
		client.init();
	}

	@After
	public void tearDown() throws Exception {
		client.stop();
	}

	@Test
	public void testInvestigation() throws ICATClientException {
		long id = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		assertTrue("Incorrect id", id > 0);
		assertTrue("Failed to create investigation", client.investigationExists("dummyInvestigation", "dummyVisit"));
		
		try {
			client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
			fail("Should not be able to create an investigation twice");
		} catch(ICATClientException e) {
			// pass
		}
		
		client.deleteEntities(ICATClient.ENTITY_INVESTIGATION, id);
	}
	
	@Test
	public void testDataset() throws ICATClientException {
		try {
			client.createDataset("dummyInvestigation", "ID19", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar()); 
			fail("Should not be able to create a dataset for missing investigation");
		} catch(ICATClientException e) {
			// pass
		}

		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar());
		
		assertTrue("Incorrect id", idd > 0);

		try {
			client.createDataset("dummyInvestigation", "ID19", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar()); 
			fail("Should not be able to create a dataset twice");
		} catch(ICATClientException e) {
			// pass
		}
		
		client.deleteEntities(ICATClient.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}
	
	@Test
	public void testDatasetParameter() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar());
		
		client.createDatasetParameter(idd, "machineMode", "dummy value");
		client.createDatasetParameter(idd, "energy", Double.toString(25.0));
		
		try {
			client.createDatasetParameter(idd, "dummy param", "value");
			fail("Should not be able to create dummy parameter");
		} catch (ICATClientException e) {
			// pass
		}
		
		client.deleteEntities(ICATClient.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}
	
	@Test
	public void testDatafile() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar());
		
		client.createDatafile(idd, "filename", "/file/location", "edf");
		
		client.deleteEntities(ICATClient.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}

}
