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


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MappingManager {

	private static final MappingManager instance = new MappingManager(); 
	
	public static MappingManager getInstance() {
		return instance;
	}
	
	private final ConcurrentMap<Class<?>, BeanFieldMapping> _mappingMap;
	
	private MappingManager() {
		super();
		_mappingMap = new ConcurrentHashMap<>();
	}
	
	public BeanFieldMapping getMapping(final Class<?> clazz) {
		BeanFieldMapping mapping = _mappingMap.get(clazz);
		BeanFieldMapping m = null;
		if(null == mapping) {
			mapping = new BeanFieldMapping(clazz);
			m = _mappingMap.putIfAbsent(clazz, mapping);
		}
		return null == m ? mapping : m;
	}

}
