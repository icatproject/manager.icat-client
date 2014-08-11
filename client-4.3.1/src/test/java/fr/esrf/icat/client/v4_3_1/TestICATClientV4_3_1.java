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

import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.icatproject_4_3_1.Dataset;
import org.icatproject_4_3_1.IcatException_Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.esrf.icat.client.DatafileDTO;
import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.UserDTO;
import fr.esrf.icat.client.data.DatafileDTOImpl;
import fr.esrf.icat.client.data.UserDTOImpl;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class TestICATClientV4_3_1 {

	private ICATClientImpl client;
	
	@Before
	public void setUp() throws Exception {
		client = new ICATClientImpl();
		Properties prop = new Properties();
		InputStream is = ClassLoader.getSystemResourceAsStream("icat.properties");
		prop.load(is);
		try {
			is.close();
		} catch (Exception e) {
			// do nothing
		}
		client.setIcatBaseUrl(prop.getProperty("icat.service.url"));
		client.setIcatAuthnPlugin(prop.getProperty("icat.security.plugin"));
		client.setIcatUsername(prop.getProperty("icat.security.username"));
		client.setIcatPassword(prop.getProperty("icat.security.password"));
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
		
		client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, id);
	}
	
	@Test
	public void testInvestigationUsers() throws ICATClientException {
		long id = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		
		List<UserDTO> users = new LinkedList<UserDTO>();
		users.add(new UserDTOImpl("user1", "User 1", "Scientist"));
		users.add(new UserDTOImpl("user2", "User 2", "Investigator"));
		
		List<Long> idu = client.updateInvestigationUsers("dummyInvestigation", "dummyVisit", users);
		
		assertTrue("Users not created correctly", idu != null && idu.size() == 2);
		
		client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, id);
		client.deleteEntities(ICATClientImpl.ENTITY_USER, idu.toArray(new Long[idu.size()]));
	}
	
	@Test
	public void testDataset() throws ICATClientException {
		try {
			client.createDataset("dummyInvestigation", "ID19", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null); 
			fail("Should not be able to create a dataset for missing investigation");
		} catch(ICATClientException e) {
			// pass
		}

		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null);
		
		assertTrue("Incorrect id", idd > 0);

		try {
			client.createDataset("dummyInvestigation", "ID19", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null); 
			fail("Should not be able to create a dataset twice");
		} catch(ICATClientException e) {
			// pass
		}
		
		client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}
	
	@Test
	public void testDatasetParameter() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null);
		
		client.createDatasetParameter(idd, "machineMode", "dummy value");
		client.createDatasetParameter(idd, "energy", Double.toString(25.0));
		
		try {
			client.createDatasetParameter(idd, "dummy param", "value");
			fail("Should not be able to create dummy parameter");
		} catch (ICATClientException e) {
			// pass
		}
		
		client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}
	
	@Test
	public void testDatafile() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null);
		
		client.createDatafile(idd, "filename", "/file/location", "edf", 0L);
		
		try {
			Dataset resultInv = (Dataset) client.getRaw("Dataset INCLUDE Datafile, DatafileFormat", idd);
			assertTrue("Wrong datafile number", resultInv.getDatafiles().size() == 1);
		} catch (IcatException_Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, idi); // cascade to dataset
		}
	}
	
	@Test
	public void testLargeDataset() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null);
		
		final int nbdtf = 1100;
		final List<DatafileDTO> files = new LinkedList<DatafileDTO>(); 
		
		for (int i = 0; i < nbdtf; i++) {
			files.add(new DatafileDTOImpl("filename" + i, "/file/location", "edf", 0L));
		}
		client.createDatafiles(idd, files);
		
		try {
			Dataset resultInv = (Dataset) client.getRaw("Dataset INCLUDE Datafile", idd);
			assertTrue("Wrong datafile number", resultInv.getDatafiles().size() == 1100);
		} catch (IcatException_Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, idi); // cascade to dataset
		}

	}
	
	@Test
	public void testUpdateDatafile() throws Exception {
		final WrappedEntityBean bean = client.get("Datafile INCLUDE 1", 818);
		
		bean.set("description", "test");
		
		client.update(bean);
		
	}

}
