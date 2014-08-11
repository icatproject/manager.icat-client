package fr.esrf.icat.client.dynamic;

import static org.junit.Assert.*;

import java.io.InputStream;
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

	public static void main(String[] args) throws Exception {
		DynamicClientTest me = new DynamicClientTest();
		me.setUp();
		me.testEnum();
		me.tearDown();
	}
}
