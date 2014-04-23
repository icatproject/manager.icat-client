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


import fr.esrf.icat.client.DatafileDTO;

public class DatafileDTOImpl implements DatafileDTO {

	private String filename;
	private String location;
	private String format;
	private long size;
	
	public DatafileDTOImpl() {
		super();
	}

	public DatafileDTOImpl(final String filename, final String location, final String format, final long size) {
		super();
		this.filename = filename;
		this.location = location;
		this.format = format;
		this.size = size;
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

	@Override
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	
}
