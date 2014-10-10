package fr.esrf.icat.client.dynamic;

/*
 * #%L
 * Dynamic ICAT client
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


import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.tools.ToolProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.common.util.Compiler;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

public class ModifiedDynamicClientFactory extends JaxWsDynamicClientFactory {

	private List<String> classNameList;
	
	protected ModifiedDynamicClientFactory(final Bus bus) {
		super(bus);
	}
	
    /**
     * Create a new instance using a specific <tt>Bus</tt>.
     * 
     * @param b the <tt>Bus</tt> to use in subsequent operations with the
     *            instance
     * @return the new instance
     */
    public static ModifiedDynamicClientFactory newInstance(final Bus b) {
        return new ModifiedDynamicClientFactory(b);
    }

    /**
     * Create a new instance using a default <tt>Bus</tt>.
     * 
     * @return the new instance
     * @see CXFBusFactory#getDefaultBus()
     */
    public static ModifiedDynamicClientFactory newInstance() {
        Bus bus = CXFBusFactory.getThreadDefaultBus();
        return new ModifiedDynamicClientFactory(bus);
    }

	@Override
	protected boolean compileJavaSrc(final String classPath, final List<File> srcList, final String dest) {
		classNameList = new LinkedList<>();
		for(File sf : srcList) {
			final String name = sf.getName();
			classNameList.add(name.substring(0, name.lastIndexOf('.')));
		}
		return super.compileJavaSrc(classPath, srcList, dest);
	}
	
	public static boolean isCompilerAvailable() {
		return Boolean.getBoolean(Compiler.class.getName() + "-fork") || (null != ToolProvider.getSystemJavaCompiler());
	}

	public List<String> getClassNameList() {
		return classNameList;
	}

}
