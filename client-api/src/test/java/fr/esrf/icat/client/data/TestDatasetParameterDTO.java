package fr.esrf.icat.client.data;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.esrf.icat.client.DatasetParameterDTO;

public class TestDatasetParameterDTO {

	@Test
	public void test() {
		DatasetParameterDTO dtsp = new DatasetParameterDTOImpl("param", "value");
		
		assertTrue("Wrong parameter name", "param".equals(dtsp.getParameter()));
		assertTrue("Wrong parameter value", "value".equals(dtsp.getValue()));
	}

}
