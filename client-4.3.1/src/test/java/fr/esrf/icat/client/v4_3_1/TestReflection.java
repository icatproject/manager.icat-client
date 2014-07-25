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


import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestReflection {

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
	public void BasicReflectionTest() throws Exception {
		Object o = client.get("Instrument", 393);
		
		System.out.println("Methods:");		
		for(Method m : o.getClass().getMethods()) {
			System.out.println("Method " + m.getName() + " returns " + m.getReturnType().getName());
		}
		
		System.out.println("XmlTypes:");		
		javax.xml.bind.annotation.XmlType t = o.getClass().getAnnotation(javax.xml.bind.annotation.XmlType.class);
		if(null != t) {
			for (String s : t.propOrder()) {
				System.out.println(s);
			}
		}
		
		System.out.println("Parent XmlTypes:");		
		javax.xml.bind.annotation.XmlType pt = o.getClass().getSuperclass().getAnnotation(javax.xml.bind.annotation.XmlType.class);
		if(null != pt) {
			for (String s : pt.propOrder()) {
				System.out.println(s);
			}
		}
	}
	
}
