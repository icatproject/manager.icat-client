package fr.esrf.icat.client.data;

/*
 * #%L
 * ICAT client API
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

import org.junit.Test;

import fr.esrf.icat.client.DatafileDTO;

public class TetsDatafileDTO {

	@Test
	public void test() {
		DatafileDTO dtfdto = new DatafileDTOImpl("filename", "/file/location/", "EDF", 1024L);
		
		assertTrue("Wrong filename", "filename".equals(dtfdto.getFilename()));
		assertTrue("Wrong location", "/file/location/".equals(dtfdto.getLocation()));
		assertTrue("Wrong format", "EDF".equals(dtfdto.getFormat()));
		assertTrue("Wrong size", 1024L == dtfdto.getSize());
	}

}
