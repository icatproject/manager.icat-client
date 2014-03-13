package fr.esrf.icat.client.data;

import fr.esrf.icat.client.DatafileDTO;

public class DatafileDTOImpl implements DatafileDTO {

	private String filename;
	private String location;
	private String format;
	
	public DatafileDTOImpl(String filename, String location, String format) {
		super();
		this.filename = filename;
		this.location = location;
		this.format = format;
	}
	
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.DatafileDTO#getFilename()
	 */
	@Override
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.DatafileDTO#getLocation()
	 */
	@Override
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	/* (non-Javadoc)
	 * @see fr.esrf.icat.client.DatafileDTO#getFormat()
	 */
	@Override
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	
}
