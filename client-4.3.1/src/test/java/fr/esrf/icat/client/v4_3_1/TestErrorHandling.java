package fr.esrf.icat.client.v4_3_1;

import static org.junit.Assert.*;

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
