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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WrappedEntityBean {

	public static final String ID_FIELD = "id";
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

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * Wrapped Beans are equals if they encapsulate the same object identified by its id.
	 * In case of error accessing the id objects are considered different. 
	 */
	@Override
	public boolean equals(Object obj) {
		if(null == obj) return false;
		if(this == obj) return true;		
		if (!(obj instanceof WrappedEntityBean)) return false;
		WrappedEntityBean other = (WrappedEntityBean) obj;
		return this.wrapped.getClass().equals(other.wrapped.getClass()) && this.getId().equals(other.getId());
	}

	private Object getId() {
		try {
			final Object object = this.get(ID_FIELD);
			return object != null ? object : new Object(); // this way we are sure 2 wrapped beans with null ids won't be considered the same 
		} catch (NoSuchMethodException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			return new Object(); // this way we are sure 2 wrapped beans giving an error won't be considered the same 
		}
	}
	
	public String getEntityName() {
		return wrapped.getClass().getSimpleName();
	}
	
	public String getAssociatedEntityName(final String field) throws NoSuchMethodException {
		if(isEntity(field)) {
			return getReturnType(field).getClass().getSimpleName();
		} else if(isAssociation(field)) {
			final Type genericReturnType = mapping.getGetter(field).getGenericReturnType();
			if(genericReturnType instanceof ParameterizedType) {
			    return ((Class<?>)((ParameterizedType) genericReturnType).getActualTypeArguments()[0]).getSimpleName();
			} else {
				throw new NoSuchMethodException("Field " + field + " returns a plain collection");
			}
		} else {
			throw new NoSuchMethodException("Field " + field + " is not an associated entity");
		}
	}
	
	public String getAssociatedMethodName(final String field) throws NoSuchMethodException {
		if(isAssociation(field)) {
			final Type genericReturnType = mapping.getGetter(field).getGenericReturnType();
			if(genericReturnType instanceof ParameterizedType) {
				final Class<?> entityClass = (Class<?>)((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
				final BeanFieldMapping entityMapping = MappingManager.getInstance().getMapping(entityClass);
				String methodName = null;
				for(String entityField : entityMapping.getEntityFields()) {
					if(entityMapping.getReturnType(entityField).equals(wrapped.getClass())) {
						if(null == methodName) {
							methodName = entityField; 
						} else {
							throw new NoSuchMethodException("Duplicate methods found in " + 
									entityMapping.getMappedClass().getSimpleName() + " returning " + entityClass.getSimpleName());
						}
					}
				}
				return methodName;
			} else {
				throw new NoSuchMethodException("Field " + field + " returns a plain collection");
			}
		} else {
			throw new NoSuchMethodException("Field " + field + " is not an associated entity");
		}
	}
	
}
