package fr.esrf.icat.client.v4_3_1;

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
	public Object get(String field) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
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
	public void set(String field, Object value) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
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
			return;
		}
		setRaw(field, value);
	}

}
