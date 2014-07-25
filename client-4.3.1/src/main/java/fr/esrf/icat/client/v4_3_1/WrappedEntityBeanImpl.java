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


import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.icatproject_4_3_1.EntityBaseBean;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class WrappedEntityBeanImpl extends WrappedEntityBean {

	public WrappedEntityBeanImpl(Object wrapped) {
		super(wrapped);
	}

	@Override
	public Object get(String field) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Object raw = getRaw(field);
		if(raw instanceof EntityBaseBean) {
			return new WrappedEntityBeanImpl(raw);
		}
		if(raw instanceof Collection) {
			List<WrappedEntityBean> nList = new LinkedList<WrappedEntityBean>();
			for(Object o : (Collection<?>)raw) {
				nList.add(new WrappedEntityBeanImpl(o));
			}
			return nList;
		}
		return raw;
	}

	@Override
	public void set(String field, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if(value instanceof WrappedEntityBean) {
			setRaw(field, ((WrappedEntityBean) value).getWrapped());
			return;
		}
		if(value instanceof Collection) {
			List<EntityBaseBean> nList = new LinkedList<EntityBaseBean>();
			for(Object o : (Collection<?>)value) {
				nList.add((EntityBaseBean) ((WrappedEntityBean)o).getWrapped());
			}
			setRaw(field, nList);
			return;
		}
		setRaw(field, value);
	}

	@Override
	protected boolean isEntityBean(Class<?> returnType) {
		return EntityBaseBean.class.isAssignableFrom(returnType);
	}

}
