package fr.esrf.icat.client.data;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.esrf.icat.client.DatafileDTO;

public class TetsDatafileDTO {

	@Test
	public void test() {
		DatafileDTO dtfdto = new DatafileDTOImpl("filename", "/file/location/", "EDF");
		
		assertTrue("Wrong filename", "filename".equals(dtfdto.getFilename()));
		assertTrue("Wrong location", "/file/location/".equals(dtfdto.getLocation()));
		assertTrue("Wrong format", "EDF".equals(dtfdto.getFormat()));
	}

}
