package fr.esrf.icat.client.v4_3_1;

/*
 * #%L
 * ICAT client 4.3.1
 * %%
 * Copyright (C) 2014 - 2015 ESRF - The European Synchrotron
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


import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;

import fr.esrf.icat.client.ICATClient;
import fr.esrf.icat.client.exception.ICATSessionException;

public class TestErrorHandling {

//	@Test(expected=IOException.class)
	@Test(expected=MalformedURLException.class)
	public void testInitWrongURL() throws Exception {
		ICATClient client = new ICATClientImpl();
		client.setIcatBaseUrl("thisiswrong");
		client.init();
		client.stop();
	}

	@Test(expected=IOException.class)
	public void testInitNoWS() throws Exception {
		ICATClient client = new ICATClientImpl();
		client.setIcatBaseUrl("http://ovm-icat-wrong");
		client.init();
		client.stop();
	}

	@Test(expected=IOException.class)
	public void testInitNotIcat() throws Exception {
		ICATClient client = new ICATClientImpl();
		client.setIcatBaseUrl("http://ovm-smis.esrf.fr:8080/jboss-net/services/SMISWebService");
		client.init();
		client.stop();
	}

	@Test(expected=ICATSessionException.class)
	public void testInitWrongCreds() throws Exception {
		ICATClient client = new ICATClientImpl();
		client.setIcatBaseUrl("https://ovm-icat-sandbox.esrf.fr:8181");
		client.setIcatAuthnPlugin("db");
		client.setIcatUsername("admin");
		client.setIcatPassword("NotTheAdminPassword");
		client.init();
		client.stop();
	}
}
