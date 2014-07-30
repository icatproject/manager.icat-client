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
