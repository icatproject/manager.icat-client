package fr.esrf.icat.client.dynamic;

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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class WrapperTest {

	private SimpleICATClient client;
	
	@Before
	public void setUp() throws Exception {
		client = new DynamicSimpleICATClient();
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
	public void testSearch() throws Exception {
		List<WrappedEntityBean> l = client.search("Instrument INCLUDE 1");
		
		assertEquals("Wrong number of instruments", 5, l.size());
		
		for(WrappedEntityBean w : l) {
			System.out.print(w.get("name"));
			System.out.print(" [");
			System.out.print(((WrappedEntityBean)w.get("facility")).get("name"));
			System.out.println("] ");
		}
	}
	
	@Test
	public void testUpate() throws Exception {
		long idi = -1;
		try {
			WrappedEntityBean inv = client.create("Investigation");
			inv.set("name", "TEST1234");
			inv.set("title", "dis is an investigation");
			inv.set("visitId", "id19");
			inv.set("facility", client.get("Facility", 390));
			inv.set("type", client.get("InvestigationType", 1));
			idi = client.create(inv);
			WrappedEntityBean w = client.get("Investigation INCLUDE 1", idi);
			Object desc = w.get("title");
			String newDesc = desc.toString() + "_updated";
			w.set("title", newDesc);
			
			client.update(w);
			WrappedEntityBean w2 = client.get("Investigation INCLUDE 1", idi);
			assertEquals("Description not updated", newDesc, w2.get("title"));
			
		} finally {
			if(idi >= 0) {
				try {
					client.deleteEntities("Investigation", idi);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void testMethods() throws Exception {
		WrappedEntityBean w = client.get("Instrument INCLUDE 1", 393);
		
		assertEquals("Wrong mutable fields", Arrays.asList(
				"description",
			    "facility",
			    "fullName",
			    "name",
			    "type",
			    "url"),
			    w.getMutableFields());
		
		assertEquals("Wrong immutable fields", Arrays.asList(
				"createId",
			    "createTime",
			    "modTime",
			    "id",
			    "modId"),
			    w.getImmutableFields());

		assertEquals("Wrong association fields", Arrays.asList(
			    "instrumentScientists",
			    "investigationInstruments"),
			    w.getAssociationFields());

		assertEquals("Wrong entity fields", Arrays.asList(
			    "facility"),
			    w.getEntityFields());
	}
	
	@Test
	public void testFields() throws Exception {
		WrappedEntityBean w = client.get("Instrument INCLUDE 1", 393);
		
		assertTrue("Field should exist", w.exists("name"));
		assertTrue("Field should be mutable", w.isMutable("name"));
		assertTrue("Field should be immutable", w.isImmutable("id"));
		assertTrue("Field should exist", w.exists("facility"));
		assertTrue("Field should be mutable", w.isMutable("facility"));
		assertTrue("Field should be entity", w.isEntity("facility"));
		assertTrue("Field should be association", w.isAssociation("instrumentScientists"));
		
	}
	
	@Test
	public void testSetter() throws Exception {
		WrappedEntityBean w = client.get("Facility INCLUDE InvestigationType", 390);
		
		w.set("name", "test facility");
		assertEquals("Name not changed", "test facility", w.get("name"));
		
		try {
			w.set("id", -1l);
			fail("Should not be able to set id");
		} catch (Exception e) {
			// pass
		}
		
		@SuppressWarnings("unchecked")
		List<WrappedEntityBean> l = (List<WrappedEntityBean>) w.get("investigationTypes");

		assertEquals("Wrong list size", 21, l.size());

		assertTrue("List content not wrapped", l.get(0) instanceof WrappedEntityBean);
		
		WrappedEntityBean u = client.create("InvestigationType");
		l.add(u);
		try {
			w.set("investigationTypes", l);
			fail("Should not be able to change many to one relationship");
		} catch (Exception e) {
			// pass
		}
	}
	
	@Test
	public void testCreate() throws Exception {
		long idi = -1;
		try {
			WrappedEntityBean inv = client.create("Investigation");
			inv.set("name", "TEST1234");
			inv.set("title", "dis is an investigation");
			inv.set("visitId", "id19");
			inv.set("facility", client.get("Facility", 390));
			inv.set("type", client.get("InvestigationType", 1));
			idi = client.create(inv);
			
			assertEquals("Bean id not set by create", idi, (long) inv.get("id"));
			
		} finally {
			if(idi >= 0) {
				try {
					client.deleteEntities("Investigation", idi);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	@Test
	public void testEquals() throws Exception {
		long idi = -1;
		try {
			WrappedEntityBean inv = client.create("Investigation");
			inv.set("name", "TEST1234");
			inv.set("title", "dis is an investigation");
			inv.set("visitId", "id19");
			inv.set("facility", client.get("Facility", 390));
			inv.set("type", client.get("InvestigationType", 1));
			
			assertNotEquals("Wrapped object should not equal null", inv, null);
			assertEquals("Wrapped object should be equal to itself", inv, inv);
			
			WrappedEntityBean inv2 = client.create("Investigation");
			inv2.set("name", "TEST1234");
			inv2.set("title", "dis is an investigation");
			inv2.set("visitId", "id19");
			inv2.set("facility", client.get("Facility", 390));
			inv2.set("type", client.get("InvestigationType", 1));
	
			assertNotEquals("Entities with no id should be different", inv, inv2);
			
			idi = client.create(inv);
	
			WrappedEntityBean returned = client.get("Investigation", idi);
			
			assertEquals("Wrapped entities should be equals if the entities have the same id", inv, returned);
	
			assertNotEquals("Entity with id should be different from another with no id", inv, inv2);
		} finally {
			if(idi >= 0) {
				try {
					client.deleteEntities("Investigation", idi);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
