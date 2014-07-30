package fr.esrf.icat.client.dynamic;

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
		WrappedEntityBean w = client.get("Investigation INCLUDE 1", 729);
		Object desc = w.get("title");
		String newDesc = desc.toString() + "_updated";
		w.set("title", newDesc);
		
		client.update(w);
		WrappedEntityBean w2 = client.get("Investigation INCLUDE 1", 729);
		assertEquals("Description not updated", newDesc, w2.get("title"));
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
