package fr.esrf.icat.client.wrapper;

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


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanFieldMapping {

	private final static Logger LOG = LoggerFactory.getLogger(WrappedEntityBean.class);
	
	private static final String GETTER_PREFIX = "get";
	private static final String BOOLEAN_PREFIX = "is";
	private static final String SETTER_PREFIX = "set";

	private final List<String> _rwFields;
	private final List<String> _roFields;
	private final List<String> _asFields;
	private final List<String> _enFields;
	private final Map<String, Class<?>> _fieldTypes;
	private final Map<String, Method> _setters;
	private final Map<String, Method> _getters;

	public BeanFieldMapping(final Class<?> clazz) {
		super();
		_rwFields = new LinkedList<String>();
		_roFields = new LinkedList<String>();
		_asFields = new LinkedList<String>();
		_enFields = new LinkedList<String>();
		_fieldTypes = new HashMap<>();
		_setters = new HashMap<>();
		_getters = new HashMap<>();
		LOG.debug("Processing class " + clazz.getName());
		processClass(clazz);
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

	private void processHierachy(final Class<?> wClass) {
		XmlType directXmlTypes = wClass.getAnnotation(XmlType.class);
		if(null == directXmlTypes) {
			return;
		}
		for (String field : directXmlTypes.propOrder()) {
			try {
				Class<?> returnType = wClass.getDeclaredField(field).getType();
				_fieldTypes.put(field, returnType);
				_getters.put(field, wClass.getMethod(getterName(field), (Class<?>[])null));
				if (Arrays.asList(returnType.getInterfaces()).contains(Collection.class)) {
					_asFields.add(field);
				} else {
					_rwFields.add(field);
					_setters.put(field, wClass.getMethod(setterName(field), returnType));
					if (isEntityBean(returnType)){
						_enFields.add(field);
					}
				}
			} catch (SecurityException | NoSuchFieldException | NoSuchMethodException e) {
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
				_getters.put(field, wClass.getMethod(getterName(field), (Class<?>[])null));
				_roFields.add(field);
			} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
				LOG.warn("Unable to analyse parent field " + field, e);
			}
		}
	}

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

	public static boolean isEntityBean(final Class<?> returnType) {
		return returnType.getAnnotation(XmlType.class) != null && returnType.getAnnotation(XmlEnum.class) == null;
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
	
	public Method getGetter(final String field) {
		return _getters.get(field);
	}

	public Method getSetter(final String field) {
		return _setters.get(field);
	}
}
