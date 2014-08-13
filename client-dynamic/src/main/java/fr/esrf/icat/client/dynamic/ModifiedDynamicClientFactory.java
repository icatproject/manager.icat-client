package fr.esrf.icat.client.dynamic;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

public class ModifiedDynamicClientFactory extends JaxWsDynamicClientFactory {

	private List<String> classNameList;
	
	protected ModifiedDynamicClientFactory(Bus bus) {
		super(bus);
	}
	
    /**
     * Create a new instance using a specific <tt>Bus</tt>.
     * 
     * @param b the <tt>Bus</tt> to use in subsequent operations with the
     *            instance
     * @return the new instance
     */
    public static ModifiedDynamicClientFactory newInstance(Bus b) {
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
	protected boolean compileJavaSrc(String classPath, List<File> srcList, String dest) {
		classNameList = new LinkedList<>();
		for(File sf : srcList) {
			final String name = sf.getName();
			classNameList.add(name.substring(0, name.lastIndexOf('.')));
		}
		return super.compileJavaSrc(classPath, srcList, dest);
	}

	public List<String> getClassNameList() {
		return classNameList;
	}

}
