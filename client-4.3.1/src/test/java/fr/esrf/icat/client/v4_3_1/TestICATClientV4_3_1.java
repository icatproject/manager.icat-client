package fr.esrf.icat.client.v4_3_1;

/*
 * #%L
 * ICAT client 4.3.1
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
			client.createDataset("dummyInvestigation", "ID19", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar()); 
			fail("Should not be able to create a dataset for missing investigation");
		} catch(ICATClientException e) {
			// pass
		}

		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar());
		
		assertTrue("Incorrect id", idd > 0);

		try {
			client.createDataset("dummyInvestigation", "ID19", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar()); 
			fail("Should not be able to create a dataset twice");
		} catch(ICATClientException e) {
			// pass
		}
		
		client.deleteEntities(ICATClient.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}
	
	@Test
	public void testDatasetParameter() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar());
		
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
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar());
		
		client.createDatafile(idd, "filename", "/file/location", "edf");
		
		client.deleteEntities(ICATClient.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}

}
