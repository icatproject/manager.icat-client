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
