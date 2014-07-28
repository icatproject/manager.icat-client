package fr.esrf.icat.client.wrapper;

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


import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WrappedEntityBean {

	private final static Logger LOG = LoggerFactory.getLogger(WrappedEntityBean.class);
	
	private static final String GETTER_PREFIX = "get";
	private static final String BOOLEAN_PREFIX = "is";
	private static final String SETTER_PREFIX = "set";

	private Object wrapped;
	
	private List<String> _rwFields;
	private List<String> _roFields;
	private List<String> _asFields;
	private List<String> _enFields;
	private Map<String, Class<?>> _fieldTypes;

	public WrappedEntityBean(final Object wrapped) {
		super();

		_rwFields = new LinkedList<String>();
		_roFields = new LinkedList<String>();
		_asFields = new LinkedList<String>();
		_enFields = new LinkedList<String>();
		_fieldTypes = new HashMap<>();
		
		this.wrapped = wrapped;
		processClass(this.wrapped.getClass());
	}

	private void processClass(final Class<?> wClass) {
		if(null == wClass) {
			return;
		}
		if(wClass.getSuperclass().equals(Object.class)) {
			processTop(wClass);
		} else {
			processHierachy(wClass);
			processClass(wClass.getSuperclass());
		}
	}

	private void processHierachy(Class<?> wClass) {
		XmlType directXmlTypes = wClass.getAnnotation(XmlType.class);
		if(null == directXmlTypes) {
			return;
		}
		for (String field : directXmlTypes.propOrder()) {
			try {
				Class<?> returnType = wClass.getDeclaredField(field).getType();
				_fieldTypes.put(field, returnType);
				if (Arrays.asList(returnType.getInterfaces()).contains(Collection.class)) {
					_asFields.add(field);
				} else {
					_rwFields.add(field);
					if (isEntityBean(returnType)){
						_enFields.add(field);
					}
				}
			} catch (SecurityException | NoSuchFieldException e) {
				LOG.warn("Unable to analyse field " + field, e);
			}
		}
	}

	private void processTop(final Class<?> wClass) {
		XmlType directXmlTypes = wClass.getAnnotation(XmlType.class);
		if(null == directXmlTypes) {
			return;
		}
		for (String field : directXmlTypes.propOrder()) {
			try {
				Class<?> returnType = wClass.getDeclaredField(field).getType();
				_fieldTypes.put(field, returnType);
				_roFields.add(field);
			} catch (NoSuchFieldException | SecurityException e) {
				LOG.warn("Unable to analyse parent field " + field, e);
			}
		}
	}

	protected abstract boolean isEntityBean(Class<?> returnType);

	private String getterName(final String field) {
		return (isBooleanField(field) ? BOOLEAN_PREFIX : GETTER_PREFIX) + StringUtils.capitalize(field);
	}

	private boolean isBooleanField(final String field)  {
		Class<?> clazz = _fieldTypes.get(field);
		return null == clazz ? false : (clazz.equals(boolean.class) || clazz.equals(Boolean.class));
	}

	private String setterName(final String field) {
		return SETTER_PREFIX + StringUtils.capitalize(field);
	}

	public List<String> getMutableFields() {
		return Collections.unmodifiableList(_rwFields);
	}

	public List<String> getImmutableFields() {
		return  Collections.unmodifiableList(_roFields);
	}

	public List<String> getAssociationFields() {
		return  Collections.unmodifiableList(_asFields);
	}
	
	public List<String> getEntityFields() {
		return  Collections.unmodifiableList(_enFields);
	}

	public Object getWrapped() {
		return wrapped;
	}
	
	protected Object getRaw(final String field) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return wrapped.getClass().getMethod(getterName(field), (Class<?>[])null).invoke(wrapped, (Object[])null);
	}
	
	protected void setRaw(final String field, final Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(isImmutable(field)) {
			throw new NoSuchMethodException("Field " + field + " is not mutable");
		}
		wrapped.getClass().getMethod(setterName(field), value.getClass()).invoke(wrapped, value);
	}

	public abstract Object get(String field) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;
	
	public abstract void set(String field, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;
	
	public boolean exists(final String field) {
		return _asFields.contains(field) || _roFields.contains(field) || _rwFields.contains(field);
	}

	public boolean isMutable(final String field) {
		return _rwFields.contains(field);
	}

	public boolean isImmutable(final String field) {
		return _roFields.contains(field) || _asFields.contains(field);
	}
	
	public boolean isEntity(final String field) {
		return _enFields.contains(field);
	}

	public boolean isAssociation(final String field) {
		return _asFields.contains(field);
	}
	
	public Class<?> getReturnType(final String field) {
		return _fieldTypes.get(field);
	}
	
}
