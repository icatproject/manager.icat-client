package fr.esrf.icat.client.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

public abstract class WrappedEntityBean {

	private static final String GETTER_PREFIX = "get";
	private static final String SETTER_PREFIX = "set";

	private Object wrapped;
	
	private List<String> _rwFields;
	private List<String> _roFields;
	private List<String> _asFields;

	public WrappedEntityBean(Object wrapped) {
		super();
		this.wrapped = wrapped;
		_rwFields = new LinkedList<String>();
		_roFields = new LinkedList<String>();
		_asFields = new LinkedList<String>();
		
		XmlType directXmlTypes = wrapped.getClass().getAnnotation(XmlType.class);
		if(null != directXmlTypes) {
			for (String s : directXmlTypes.propOrder()) {
				String m = getterName(s);
				try {
					if (Arrays.asList(wrapped.getClass().getMethod(m, (Class<?>[])null).getReturnType().getInterfaces())
							.contains(Collection.class)) {
						_asFields.add(s);
					} else {
						_rwFields.add(s);
					}
				} catch (NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		XmlType parentXmlTypes = wrapped.getClass().getSuperclass().getAnnotation(XmlType.class);
		_asFields.addAll(Arrays.asList(parentXmlTypes.propOrder()));
	}

	private String getterName(String field) {
		return GETTER_PREFIX + StringUtils.capitalize(field);
	}

	private String setterName(String field) {
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
	
	public Object getWrapped() {
		return wrapped;
	}
	
	protected Object getRaw(String field) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return wrapped.getClass().getMethod(getterName(field), (Class<?>[])null).invoke(wrapped, (Object[])null);
	}
	
	protected void setRaw(String field, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(_roFields.contains(field) || _asFields.contains(field)) {
			throw new NoSuchMethodException("Field " + field + " is not mutable");
		}
		wrapped.getClass().getMethod(setterName(field), value.getClass()).invoke(wrapped, value);
	}

	public abstract Object get(String field) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;
	
	public abstract void set(String field, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;
}
