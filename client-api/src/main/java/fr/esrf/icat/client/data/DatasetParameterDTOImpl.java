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
