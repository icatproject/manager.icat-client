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

import fr.esrf.icat.client.UserDTO;

public class TestUserDTO {

	@Test
	public void test() {
		UserDTO u = new UserDTOImpl("name", "fullname", "Scientist");
		
		assertTrue("Wrong name", "name".equals(u.getName()));
		assertTrue("Wrong full name", "fullname".equals(u.getFullName()));
		assertTrue("Wrong role", "Scientist".equals(u.getRole()));
	}

}
