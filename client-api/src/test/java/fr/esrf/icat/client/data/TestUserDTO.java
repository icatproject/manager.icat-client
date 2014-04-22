package fr.esrf.icat.client.data;

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
