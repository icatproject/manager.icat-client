package fr.esrf.icat.client.data;

/*
 * #%L
 * ICAT client API
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
