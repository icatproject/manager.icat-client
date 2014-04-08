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


import fr.esrf.icat.client.DatasetParameterDTO;

public class DatasetParameterDTOImpl implements DatasetParameterDTO {

	private String parameter;
	private String value;
	
	public DatasetParameterDTOImpl(String parameter, String value) {
		super();
		this.parameter = parameter;
		this.value = value;
	}
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.DatasetParameterDTO#getParameter()
	 */
	@Override
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.DatasetParameterDTO#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
