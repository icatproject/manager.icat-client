package fr.esrf.icat.client.wrapper;

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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.icatproject_4_3_1.Instrument;
import org.icatproject_4_3_1.Investigation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.esrf.icat.client.v4_3_1.ICATClientImpl;

public class WrapperTest {

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
	public void basicReflectionTest() throws Exception {
		Object o = client.getRaw("Instrument", 393);
		
		System.out.println("--- Methods ---");		
		for(Method m : o.getClass().getMethods()) {
			System.out.println("Method " + m.getName() + " returns " + m.getReturnType().getName());
		}
		
		System.out.println("--- XmlTypes ---");		
		javax.xml.bind.annotation.XmlType t = o.getClass().getAnnotation(javax.xml.bind.annotation.XmlType.class);
		assertNotNull("No XmlTypes defined", t);
		for (String s : t.propOrder()) {
			System.out.println(s);
		}
		
		System.out.println("--- Parent XmlTypes ---");		
		javax.xml.bind.annotation.XmlType pt = o.getClass().getSuperclass().getAnnotation(javax.xml.bind.annotation.XmlType.class);
		assertNotNull("No parent XmlTypes defined", pt);
		for (String s : pt.propOrder()) {
			System.out.println(s);
		}
	}
	
	@Test
	public void testWrapper() throws Exception {
		Instrument inst = (Instrument) client.getRaw("Instrument INCLUDE 1", 393);
		WrappedEntityBean w = client.get("Instrument INCLUDE 1", 393);
		
		assertEquals("Wrong create id", inst.getCreateId(), w.get("createId"));
		assertEquals("Wrong create time", inst.getCreateTime(), w.get("createTime"));
		assertEquals("Wrong description", inst.getDescription(), w.get("description"));
//		assertEquals("Wrong facility", inst.getFacility(), w.get("facility"));
		assertEquals("Wrong full name", inst.getFullName(), w.get("fullName"));
		assertEquals("Wrong id", inst.getId(), w.get("id"));
//		assertEquals("Wrong instrument scientists", inst.getInstrumentScientists(), w.get("instrumentScientists"));
//		assertEquals("Wrong investigation instrument", inst.getInvestigationInstruments(), w.get("investigationInstruments"));
		assertEquals("Wrong mod id", inst.getModId(), w.get("modId"));
		assertEquals("Wrong mod time", inst.getModTime(), w.get("modTime"));
		assertEquals("Wrong name", inst.getName(), w.get("name"));
		assertEquals("Wrong type", inst.getType(), w.get("type"));
		assertEquals("Wrong url", inst.getUrl(), w.get("url"));
		
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
		
		long idi = client.createInvestigation("TEST1234", "TEST", "id19", "dis is a test", "id19", (GregorianCalendar) GregorianCalendar.getInstance());
		
		WrappedEntityBean w = client.get("Investigation INCLUDE 1", idi);
		Object desc = w.get("title");
		String newDesc = desc.toString() + "_updated";
		w.set("title", newDesc);
		
		assertEquals("Description changed !", desc, ((Investigation)client.getRaw("Investigation", idi)).getTitle());
		client.update(w);
		assertEquals("Description not updated !", newDesc, ((Investigation)client.getRaw("Investigation", idi)).getTitle());
		
		client.deleteEntities("Investigation", idi);
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
}
