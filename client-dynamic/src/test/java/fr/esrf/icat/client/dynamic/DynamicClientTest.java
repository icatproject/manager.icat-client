package fr.esrf.icat.client.dynamic;

/*
 * #%L
 * Dynamic ICAT client
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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class DynamicClientTest {

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
	public void testEnum() throws Exception {
		List<WrappedEntityBean> l = client.search("ParameterType INCLUDE 1");
		for(WrappedEntityBean b : l) {
			final Object object = b.get("valueType");
			assertTrue("This field should be an enum", object instanceof Enum);
			System.out.println(object.toString());
		}
		
	}
	
	@Test
	public void testUpdateDatafile() throws Exception {
		final WrappedEntityBean bean = client.get("Datafile INCLUDE 1", 818);
		bean.set("description", "test");
		client.update(bean);
	}
	
	@Test
	public void testVersion() throws Exception {
		DynamicSimpleICATClient dclient = (DynamicSimpleICATClient) client;
		
		assertEquals("Wrong version", "4.3.1", client.getServerVersion());
		
		assertTrue("Error comparing to 4.3.1", dclient.compareVersionTo("4.3.1") == 0);
	
		assertTrue("Error comparing to 4.3", dclient.compareVersionTo("4.3") > 0);
		
		assertTrue("Error comparing to 4.2", dclient.compareVersionTo("4.2") > 0);
		
		assertTrue("Error comparing to 4.4", dclient.compareVersionTo("4.4") < 0);
	}
	
	@Test
	public void testEntityList() throws Exception {
		DynamicSimpleICATClient dclient = (DynamicSimpleICATClient) client;
		final List<String> createdEntityNames = dclient.createEntityNames();
		System.out.println(createdEntityNames);
		assertEquals("Generated entity list does not match retrieved entity list",
				new HashSet<String>(dclient.getEntityNames()), new HashSet<String>(createdEntityNames));
	}

	public static void main(String[] args) throws Exception {
		DynamicClientTest me = new DynamicClientTest();
		me.setUp();
		me.testEnum();
		me.tearDown();
	}
}
