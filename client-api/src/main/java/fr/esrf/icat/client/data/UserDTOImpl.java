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


import fr.esrf.icat.client.UserDTO;

public class UserDTOImpl implements UserDTO {

	private String name;
	private String fullName;
	private String role;
	
	public UserDTOImpl(String name, String fullName, String role) {
		super();
		this.name = name;
		this.fullName = fullName;
		this.role = role;
	}

	@Override
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof UserDTO)) return false;
		UserDTO other = (UserDTO) obj;
		if (name == null) {
			return null == other.getName();
		} else {
			return name.equals(other.getName());
		}
	}
	
}
