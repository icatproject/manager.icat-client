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
