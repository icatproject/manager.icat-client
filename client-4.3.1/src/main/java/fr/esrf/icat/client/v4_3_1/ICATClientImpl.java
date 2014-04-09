package fr.esrf.icat.client.v4_3_1;

/*
 * #%L
 * ICAT client 4.3.1
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


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.icatproject_4_3_1.Datafile;
import org.icatproject_4_3_1.DatafileFormat;
import org.icatproject_4_3_1.Dataset;
import org.icatproject_4_3_1.DatasetParameter;
import org.icatproject_4_3_1.DatasetType;
import org.icatproject_4_3_1.EntityBaseBean;
import org.icatproject_4_3_1.Facility;
import org.icatproject_4_3_1.ICAT;
import org.icatproject_4_3_1.ICATService;
import org.icatproject_4_3_1.IcatException_Exception;
import org.icatproject_4_3_1.Instrument;
import org.icatproject_4_3_1.Investigation;
import org.icatproject_4_3_1.InvestigationInstrument;
import org.icatproject_4_3_1.InvestigationType;
import org.icatproject_4_3_1.Login.Credentials;
import org.icatproject_4_3_1.Login.Credentials.Entry;
import org.icatproject_4_3_1.ParameterType;
import org.icatproject_4_3_1.ParameterValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.DatafileDTO;
import fr.esrf.icat.client.DatasetParameterDTO;
import fr.esrf.icat.client.ICATClient;
import fr.esrf.icat.client.ICATClientException;

public class ICATClientImpl extends ICATClient {

	private final static Logger LOG = LoggerFactory.getLogger(ICATClient.class);
	
	private ICAT icat;

	private String sessionId;

	private Facility facility;
	
	private DatasetType dtsType;
	
	private Map<String, Instrument> cachedInstruments;
	
	private Map<String, DatafileFormat> cachedDatafileFormats;

	private Map<String, InvestigationType> cachedInvestigationTypes;
	
	private Map<String, ParameterType> cachedParameterType;

	private DatatypeFactory datatypeFactory;

	@Override
	public void doInit() {
		try {
			datatypeFactory = DatatypeFactory.newInstance();
			URL base = new URL(getIcatBaseUrl());
			URL icatUrl = new URL(base, ICAT_SERVICE_URL);
			LOG.debug("Using ICAT service at " + icatUrl.toString());
			QName qName = new QName(ICATPROJECT_NAMESPACE, ICAT_SERVICE_NAME);
			ICATService service = new ICATService(icatUrl, qName);
			icat = service.getICATPort();
			if(LOG.isDebugEnabled()) {
				LOG.debug("ICAT Version: "+ icat.getApiVersion());
			}
		} catch (IcatException_Exception | DatatypeConfigurationException | MalformedURLException e) {
			throw new IllegalStateException("Unable to initialise ICATClient v4.3.1", e);
		}
	}

	@Override
	public long initiateConnection() throws ICATClientException {
		LOG.debug("Connecting to ICAT as '" + getIcatUsername() + "'");

		Credentials credentials = new Credentials();
		java.util.List<Entry> entries = credentials.getEntry();
		Entry e = new Entry();
		e.setKey("username");
		e.setValue(getIcatUsername());
		entries.add(e);

		e = new Entry();
		e.setKey("password");
		e.setValue(getIcatPassword());
		entries.add(e);
		
		try {
			sessionId = icat.login(getIcatAuthnPlugin(), credentials);
			LOG.debug("Connected to ICAT [" + sessionId + "]");
			long remainingMinutes = (long) Math.floor(icat.getRemainingMinutes(sessionId));
			return System.currentTimeMillis() + remainingMinutes * ONE_MINUTE_IN_MS;
		} catch (IcatException_Exception e1) {
			LOG.error("Unable to create connection", e1);
			throw new ICATClientException(e1);
		}
	}

	@Override
	public long refreshConnection() throws ICATClientException {
		try {
			icat.refresh(sessionId);
			long remainingMinutes = (long) Math.floor(icat.getRemainingMinutes(sessionId));
			return System.currentTimeMillis() + remainingMinutes * ONE_MINUTE_IN_MS;
		} catch (IcatException_Exception e) {
			LOG.error("Unable to refresh connection", e);
			throw new ICATClientException(e);
		}
	}

	@Override
	public void closeConnection() {
		if(null != icat && null != sessionId) {
			try {
				LOG.debug("Closing session " + sessionId);
				icat.logout(sessionId);
				icat = null;
				sessionId = null;
				LOG.debug("Session closed");
			} catch (IcatException_Exception e) {
				LOG.warn("Unable to close ICAT session", e);
			}
		}
	}

	@Override
	public void populateObjectCache() throws ICATClientException {
		try {
			facility = (Facility) icat.search(sessionId, "Facility [name='ESRF']").get(0);
			dtsType = (DatasetType) icat.search(sessionId, "DatasetType [name='acquisition' AND facility.id='" + facility.getId() + "']").get(0);
			
			// cached instruments
			List<? extends Object> instruments = icat.search(sessionId, "Instrument [facility.id='" + facility.getId() + "']");
			cachedInstruments = new HashMap<String, Instrument>(instruments.size());
			for(Object o : instruments) {
				Instrument ins = (Instrument) o;
				cachedInstruments.put(ins.getName(), ins);
			}
			
			// cached datafileFormats
			List<? extends Object> datafileFormats = icat.search(sessionId, "DatafileFormat [facility.id='" + facility.getId() + "']");
			cachedDatafileFormats = new HashMap<String, DatafileFormat>(datafileFormats.size());
			for(Object o : datafileFormats) {
				DatafileFormat dtff = (DatafileFormat) o;
				cachedDatafileFormats.put(dtff.getName(), dtff);
			}

			// cached InvestigationTypes
			List<? extends Object> investigationTypes = icat.search(sessionId, "InvestigationType [facility.id='" + facility.getId() + "']");
			cachedInvestigationTypes = new HashMap<String, InvestigationType>(investigationTypes.size());
			for(Object o : investigationTypes) {
				InvestigationType invt = (InvestigationType) o;
				cachedInvestigationTypes.put(invt.getName(), invt);
			}

			// cached ParameterTypes
			List<? extends Object> parameterTypes = icat.search(sessionId, "ParameterType [facility.id='" + facility.getId() + "']");
			cachedParameterType = new HashMap<String, ParameterType>(parameterTypes.size());
			for(Object o : parameterTypes) {
				ParameterType paramt = (ParameterType) o;
				cachedParameterType.put(paramt.getName(), paramt);
			}

		} catch (IcatException_Exception e) {
			throw new ICATClientException(e);
		}
	}

	@Override
	public boolean investigationExists(final String investigation, final String visit) throws ICATClientException {
		try {
			checkConnection();
			List<Object> response = icat.search(sessionId, "Investigation [name ='" + investigation + "' AND visitId = '" + visit + "' AND facility.id = " + facility.getId() + "]");
			if(LOG.isDebugEnabled()) {
				LOG.debug(((null != response && response.size() > 0) ? "Found " + response.size() + " Investigation" : "Found no Investigation")
						+ " with name " + investigation);
			}
			return null != response && response.size() == 1;
		} catch (IcatException_Exception e) {
			LOG.error("Unable to check investigation " + investigation, e);
			throw new ICATClientException(e);
		}
	}

	@Override
	public long createInvestigation(final String name, final String type, final String visit, final String title, final String instrument, final GregorianCalendar startDate) throws ICATClientException {
		try {
			checkConnection();
			Investigation icatInvestigation = new Investigation();
			icatInvestigation.setFacility(facility);
			InvestigationType invtype = getInvestigationType(type);
			icatInvestigation.setType(invtype);
			icatInvestigation.setVisitId(visit);
			icatInvestigation.setName(name);
			icatInvestigation.setTitle(title);
			if(null != startDate) {
				icatInvestigation.setStartDate(datatypeFactory.newXMLGregorianCalendar(startDate));
			}
			create(icatInvestigation);
			// check investigation instrument exists, create it if not
			Instrument inst = getInstrument(instrument.toUpperCase());
			createInvestigationInstrumentIfAbsent(inst, icatInvestigation);
			return icatInvestigation.getId();
		} catch (IcatException_Exception e) {
			LOG.error("Unable to create investigation [" + name + ", " + type + ", " + visit + ", " + title + "]", e);
			throw new ICATClientException(e);
		}
	}

	@Override
	public long createDataset(final String investigation, final String visit, final String name, final String location, final GregorianCalendar startDate, final GregorianCalendar endDate) throws ICATClientException {
		try {
			checkConnection();
			// retrieve the investigation
			Investigation inv = getInvestigation(investigation, visit);
			// create the dataset
			Dataset dataset = new Dataset();
			dataset.setInvestigation(inv);
			dataset.setName(name);
			dataset.setLocation(location);
			XMLGregorianCalendar endDateXMLCal = null;
			if(null != endDate) {
				endDateXMLCal = datatypeFactory.newXMLGregorianCalendar(endDate);
				dataset.setEndDate(endDateXMLCal);
			}
			if(null != startDate) {
				dataset.setStartDate(datatypeFactory.newXMLGregorianCalendar(startDate));
			}
			dataset.setType(dtsType);
			long dts_id = create(dataset);
			if(null != endDateXMLCal) {
				inv.setEndDate(endDateXMLCal);
				update(inv);
			}
			return dts_id;
		} catch (IcatException_Exception e) {
			LOG.error("Unable to create dataset [" + investigation + ", " + visit + ", " + name + ", " + location + "]", e);
			throw new ICATClientException(e);
		}
	}

	private void createInvestigationInstrumentIfAbsent(final Instrument inst, final Investigation inv) throws IcatException_Exception, ICATClientException {
		if(!investigationInstrumentExists(inst, inv)) {
			createInvestigationInstrument(inst, inv);
		}
	}
	
	private boolean investigationInstrumentExists(final Instrument inst, final Investigation inv) throws IcatException_Exception, ICATClientException {
		checkConnection();
		List<Object> response = icat.search(sessionId, "InvestigationInstrument [instrument.id = '" + inst.getId() + "' AND investigation.id = '" + inv.getId() + "']");
		return response.size() == 1;
	}

	private void createInvestigationInstrument(final Instrument inst, final Investigation inv) throws IcatException_Exception {
		// create the investigation instrument
		InvestigationInstrument invinstr = new InvestigationInstrument();
		invinstr.setInvestigation(inv);
		invinstr.setInstrument(inst);
		create(invinstr);
	}

	@Override
	public long createDatafile(final long datasetID, final String filename, final String location, final String format) throws ICATClientException {
		try {
			checkConnection();
			// retrieve the dataset
			Dataset dts = (Dataset) icat.get(sessionId, ENTITY_DATASET, datasetID);
			// retrieve the dataformat
			DatafileFormat fmt = getDatafileFormat(format);
			// create datafile
			Datafile dtf = new Datafile();
			dtf.setDataset(dts);
			dtf.setName(filename);
			dtf.setLocation(location);
			dtf.setDatafileFormat(fmt);
			return create(dtf);
		} catch (IcatException_Exception e) {
			LOG.error("Unable to create datafile [" + datasetID + ", " + filename + ", " + location+ ", " + format + "]", e);
			throw new ICATClientException(e);
		}
	}
	
	@Override
	public void createDatafiles(long datasetID, Collection<DatafileDTO> datafileCollection) throws ICATClientException {
		try {
			checkConnection();
			// retrieve the dataset
			Dataset dts = (Dataset) icat.get(sessionId, ENTITY_DATASET, datasetID);
			
			List<EntityBaseBean> dtfCollection = new LinkedList<EntityBaseBean>();
			for(DatafileDTO dtfd : datafileCollection) {
				// retrieve the dataformat
				DatafileFormat fmt = getDatafileFormat(dtfd.getFormat());
				// create datafile
				Datafile dtf = new Datafile();
				dtf.setDataset(dts);
				dtf.setName(dtfd.getFilename());
				dtf.setLocation(dtfd.getLocation());
				dtf.setDatafileFormat(fmt);
				dtfCollection.add(dtf);
			}
			
			icat.createMany(sessionId, dtfCollection);
		} catch (IcatException_Exception e) {
			LOG.error("Unable to create datafiles for dataset [" + datasetID + "]", e);
			throw new ICATClientException(e);
		}
	}

	@Override
	public long createDatasetParameter(final long datasetID, final String parameter, final String value) throws ICATClientException {
		try {
			checkConnection();
			// retrieve the dataset
			Dataset dts = (Dataset) icat.get(sessionId, ENTITY_DATASET, datasetID);
			// retrieve the parameter type
			ParameterType type = getParameterType(parameter);
			// check it is not null
			if(null == type) {
				LOG.error("Parameter type '" + parameter + "' not defined");
				throw new ICATClientException("Parameter type does not exist: " + type);
			}
			DatasetParameter dtsparam = new DatasetParameter();
			dtsparam.setDataset(dts);
			dtsparam.setType(type);
			ParameterValueType valueType = type.getValueType();
			if(valueType.equals(ParameterValueType.NUMERIC)) {
				Double dvalue = Double.parseDouble(value);
				dtsparam.setNumericValue(dvalue);
			} else {
				dtsparam.setStringValue(value);
			}
			return create(dtsparam);
		} catch (IcatException_Exception e) {
			LOG.error("Unable to create dataset parameter [" + datasetID + ", " + parameter + ", " + value + "]", e);
			throw new ICATClientException(e);
		}
	}

	@Override
	public void createDatasetParameters(long datasetID, Collection<DatasetParameterDTO> datasetParamCollection) throws ICATClientException {
		try {
			checkConnection();
			// retrieve the dataset
			Dataset dts = (Dataset) icat.get(sessionId, ENTITY_DATASET, datasetID);
			
			List<EntityBaseBean> dtspCollection = new LinkedList<EntityBaseBean>();
			for(DatasetParameterDTO dtspd : datasetParamCollection) {
				// retrieve the parameter type
				String parameter = dtspd.getParameter();
				ParameterType type = getParameterType(parameter);
				// check it is not null
				if(null == type) {
					LOG.error("Parameter type '" + parameter + "' not defined");
					throw new ICATClientException("Parameter type does not exist: " + type);
				}
				DatasetParameter dtsparam = new DatasetParameter();
				dtsparam.setDataset(dts);
				dtsparam.setType(type);
				ParameterValueType valueType = type.getValueType();
				if(valueType.equals(ParameterValueType.NUMERIC)) {
					Double dvalue = Double.parseDouble(dtspd.getValue());
					dtsparam.setNumericValue(dvalue);
				} else {
					dtsparam.setStringValue(dtspd.getValue());
				}
				dtspCollection.add(dtsparam);
			}
			
			icat.createMany(sessionId, dtspCollection);
		} catch (IcatException_Exception e) {
			LOG.error("Unable to create dataset parameters for dataset [" + datasetID + "]", e);
			throw new ICATClientException(e);
		}
	}

	private Instrument getInstrument(final String instrument) throws IcatException_Exception, ICATClientException {
		Instrument inst = cachedInstruments.get(instrument);
		if(null != inst) {
			return inst;
		}
		checkConnection();
		List<Object> response = icat.search(sessionId, "Instrument [name ='" + instrument  + "' AND facility.id = '" + facility.getId() + "']");
		inst = (Instrument) ((null == response || response.size() == 0) ? null : response.get(0));
		if(null != inst) {
			cachedInstruments.put(instrument, inst);
		}
		return inst;
	}

	private Investigation getInvestigation(final String investigation, final String visit) throws IcatException_Exception, ICATClientException {
		checkConnection();
		List<Object> response = icat.search(sessionId, "Investigation INCLUDE 1 [name ='" + investigation + "' AND visitId = '" + visit + "' AND facility.id = '" + facility.getId() +"']");
		return (Investigation) ((null == response || response.size() == 0) ? null : response.get(0));
	}

	private DatafileFormat getDatafileFormat(final String format) throws IcatException_Exception, ICATClientException {
		DatafileFormat dtff = cachedDatafileFormats.get(format);
		if(null != dtff) { 
			return dtff;
		}
		checkConnection();
		List<Object> response = icat.search(sessionId, "DatafileFormat [name ='" + format + "' AND facility.id = '" + facility.getId() + "']");
		dtff = (DatafileFormat) ((null == response || response.size() == 0) ? null : response.get(0));
		if(null != dtff) {
			cachedDatafileFormats.put(format, dtff);
		}
		return dtff;
	}

	private InvestigationType getInvestigationType(final String type) throws IcatException_Exception, ICATClientException {
		InvestigationType invt = cachedInvestigationTypes.get(type);
		if(null != invt) {
			return invt;
		}
		checkConnection();
		List<Object> response = icat.search(sessionId, "InvestigationType [name ='" + type + "' AND facility.id = '" + facility.getId() + "']");
		invt = (InvestigationType) ((null == response || response.size() == 0) ? null : response.get(0));
		if(null != invt) {
			cachedInvestigationTypes.put(type, invt);
		}
		return invt;
	}

	private ParameterType getParameterType(final String parameter) throws IcatException_Exception, ICATClientException {
		ParameterType paramt = cachedParameterType.get(parameter);
		if(null != paramt) {
			return paramt;
		}
		checkConnection();
		List<Object> response = icat.search(sessionId, "ParameterType [name ='" + parameter + "' AND facility.id = '" + facility.getId() + "']");
		paramt = (ParameterType) ((null == response || response.size() == 0) ? null : response.get(0));
		if(null != paramt) {
			cachedParameterType.put(parameter, paramt);
		}
		return paramt;
	}

	private long create(EntityBaseBean entity)  throws IcatException_Exception {
		final long id = icat.create(sessionId, entity);
		entity.setId(id);
		return id;
	}
	
	private void update(EntityBaseBean entity)  throws IcatException_Exception {
		icat.update(sessionId, entity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void deleteEntities(final String entityName, final Long... ids) throws ICATClientException {
		if(null == ids || ids.length == 0) {
			return;
		}
		StringBuilder query = new StringBuilder();
		query.append(entityName);
		query.append(" [id IN (");
		for(long id : ids) {
			query.append(Long.toString(id));
			query.append(",");
		}
		query.setCharAt(query.length() - 1, ')');
		query.append("]");
		try {
			checkConnection();
			List<? extends Object> result = icat.search(sessionId, query.toString());
			icat.deleteMany(sessionId, (List<EntityBaseBean>) result);
		} catch (IcatException_Exception e) {
			throw new ICATClientException(e);
		}
	}
}
