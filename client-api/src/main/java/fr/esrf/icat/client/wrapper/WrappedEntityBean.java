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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WrappedEntityBean {

	private final Object wrapped;
	private final BeanFieldMapping mapping;
	
	public WrappedEntityBean(final Object wrapped) {
		super();
		this.wrapped = wrapped;
		this.mapping = MappingManager.getInstance().getMapping(this.wrapped.getClass());
	}

	public List<String> getMutableFields() {
		return mapping.getMutableFields();
	}

	public List<String> getImmutableFields() {
		return mapping.getImmutableFields();
	}

	public List<String> getAssociationFields() {
		return mapping.getAssociationFields();
	}
	
	public List<String> getEntityFields() {
		return mapping.getEntityFields();
	}

	public Object getWrapped() {
		return wrapped;
	}
	
	public Object get(final String field) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Method getter = mapping.getGetter(field);
		if(null == getter) {
			throw new NoSuchMethodException("Getter for " + field + " not found");
		}
		final Object value = getter.invoke(wrapped, (Object[])null);
		// handling null
		if(null == value) {
			return null;
		}
		// if method returned an entity type, wrap it
		if(BeanFieldMapping.isEntityBean(value.getClass())) {
			return new WrappedEntityBean(value);
		}
		// if method returned a list of entities, returns a list of wrapped entities
		if(value instanceof Collection) {
			List<WrappedEntityBean> nList = new LinkedList<WrappedEntityBean>();
			for(Object o : (Collection<?>)value) {
				nList.add(new WrappedEntityBean(o));
			}
			return nList;
		}
		// otherwise return the plain value
		return value;
	}
	
	public void set(final String field, final Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(isImmutable(field)) {
			throw new NoSuchMethodException("Field " + field + " is not mutable");
		}
		final Method setter = mapping.getSetter(field);
		if(null == setter) {
			throw new NoSuchMethodException("Setter for " + field + " not found");
		}
		// if value is an wrapped, unwrap it first
		if(value instanceof WrappedEntityBean) {
			setter.invoke(wrapped, ((WrappedEntityBean)value).wrapped);
		// otherwise directly set the value
		} else {
			setter.invoke(wrapped, value);
		}
	}

	public boolean exists(final String field) {
		return mapping.exists(field);
	}

	public boolean isMutable(final String field) {
		return mapping.isMutable(field);
	}

	public boolean isImmutable(final String field) {
		return mapping.isImmutable(field);
	}
	
	public boolean isEntity(final String field) {
		return mapping.isEntity(field);
	}

	public boolean isAssociation(final String field) {
		return mapping.isAssociation(field);
	}
	
	public Class<?> getReturnType(final String field) {
		return mapping.getReturnType(field);
	}
	
}
