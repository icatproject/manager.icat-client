package fr.esrf.icat.client.data;

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
