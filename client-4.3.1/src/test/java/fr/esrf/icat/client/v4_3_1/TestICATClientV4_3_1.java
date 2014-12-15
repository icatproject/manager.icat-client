package fr.esrf.icat.client.v4_3_1;

/*
 * #%L
 * ICAT client 4.3.1
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


import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.icatproject_4_3_1.Datafile;
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
		
		// by default dataset should be complete
		Dataset d = (Dataset) client.get("Dataset", idd).getWrapped();
		assertTrue("Dataset should be complete by default", d.isComplete());
		
		client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}
	
	@Test
	public void testIncompleteDataset() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null, false);
		
		assertTrue("Incorrect id", idd > 0);

		Dataset d = (Dataset) client.get("Dataset", idd).getWrapped();
		assertFalse("Dataset should be incomplete", d.isComplete());
		
		client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, idi); // cascade to dataset
	}	
	
	@Test
	public void testDatasetParameter() throws ICATClientException {
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null);
		
		client.createDatasetParameter(idd, "machineMode", "dummy value");
		client.createDatasetParameter(idd, "energy", Double.toString(25.0));
		client.createDatasetParameter(idd, "motor_mi_h", "ERROR");
		
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
		
		long idi = client.createInvestigation("dummyInvestigation", "MA", "dummyVisit", "dummy title", "ID19", new GregorianCalendar());
		long idd = client.createDataset("dummyInvestigation", "dummyVisit", "dummySample", "dummy dataset", "/dummy/location", new GregorianCalendar(), new GregorianCalendar(), null);
		
		long idf = client.createDatafile(idd, "filename", "/file/location", "edf", 0L);
		
		final WrappedEntityBean bean = client.get("Datafile INCLUDE 1", idf);
		
		final String descValue = "test";
		bean.set("description", descValue);
		
		client.update(bean);
		
		try {
			Datafile resultInv = (Datafile) client.getRaw("Datafile INCLUDE 1", idf);
			assertTrue("Descriptions not changed", descValue.equals(resultInv.getDescription()));
		} catch (IcatException_Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			client.deleteEntities(ICATClientImpl.ENTITY_INVESTIGATION, idi); // cascade to dataset
		}
	}

}
