package aQute.miniframework;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

import org.osgi.framework.*;

public class Context extends URLClassLoader implements Bundle, BundleContext {
	long					id;
	MiniFramework			fw;
	String					location;
	int						state	= Bundle.INSTALLED;
	JarFile					jar;
	Manifest				manifest;
	TreeSet<String>			keys;
	private TreeSet<String>	paths;
	final List<BundleListener> bundleListeners = new ArrayList<BundleListener>();
	final List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();
	final List<FrameworkListener> frameworkListeners = new ArrayList<FrameworkListener>();
	
	class Dict extends Dictionary<String, Object> {

		public Enumeration<Object> elements() {
			return Collections.enumeration(manifest.getMainAttributes().values());
		}

		public Object get(Object key) {
			Object o = manifest.getMainAttributes().getValue((String) key);
			return o;
		}

		public boolean isEmpty() {
			return manifest.getMainAttributes().isEmpty();
		}

		public Enumeration<String> keys() {
			Vector v = new Vector();
			for (Iterator i = manifest.getMainAttributes().keySet().iterator(); i.hasNext();) {
				Attributes.Name name = (Attributes.Name) i.next();
				v.add(name.toString());
			}
			return v.elements();
		}

		public Object put(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		public Object remove(Object key) {
			throw new UnsupportedOperationException();
		}

		public int size() {
			return manifest.getMainAttributes().size();
		}

	}

	public Context(MiniFramework fw, ClassLoader parent, int id, String location)
			throws IOException {
		super(new URL[] { new File(location).toURL() }, parent);
		this.fw = fw;
		this.id = id;
		this.location = location;
		jar = new JarFile(new File(location));
		manifest = jar.getManifest();
		jar.close();
	}

	public BundleContext getBundleContext() {
		return this;
	}

	public long getBundleId() {
		return id;
	}

	public URL getEntry(String path) {
		return getResource(path);
	}

	public Enumeration getEntryPaths(String path) {
		throw new UnsupportedOperationException();
	}

	public Dictionary getHeaders() {
		return new Dict();
	}

	public Dictionary getHeaders(String locale) {
		return new Dict();
	}

	public long getLastModified() {
		return 0;
	}

	public String getLocation() {
		return location;
	}

	public Enumeration findEntries(String path, String filePattern, boolean recurse) {

		try {
			if (path.startsWith("/"))
				path = path.substring(1);
			if (!path.endsWith("/"))
				path += "/";

			Vector paths = new Vector();
			for (Iterator i = getPaths().iterator(); i.hasNext();) {
				String entry = (String) i.next();
				if (entry.startsWith(path)) {
					if (recurse || entry.indexOf('/', path.length()) < 0) {
						if (filePattern == null || matches(entry, filePattern)) {
							URL url = getResource(entry);
							if (url == null) {
								System.err.println("Cannot load resource that should be there: "
										+ entry);
							} else
								paths.add(url);
						}
					}
				}
			}
			return paths.elements();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean matches(String path, String filePattern) {
		do {
			int part = filePattern.indexOf('*');
			if (part < 0) {
				return path.indexOf(filePattern) >= 0;
			} else {
				String match = filePattern.substring(0, part);
				int m = path.indexOf(match);
				if (m < 0)
					return false;

				path = path.substring(m + match.length());
				filePattern = filePattern.substring(part + 1);
			}
		} while (true);
	}

	private Collection getPaths() throws Exception {
		if (paths != null)
			return paths;

		paths = new TreeSet();
		JarFile jar = new JarFile(new File(location));
		try {
			for (Enumeration e = jar.entries(); e.hasMoreElements();) {
				ZipEntry entry = (JarEntry) e.nextElement();
				paths.add(entry.getName());
			}
		} finally {
			jar.close();
		}
		return paths;
	}

	public ServiceReference[] getRegisteredServices() {
		return null;
	}

	public ServiceReference[] getServicesInUse() {
		return null;
	}

	public Map getSignerCertificates(int signersType) {
		throw new UnsupportedOperationException();
	}

	public int getState() {
		return state;
	}

	public String getSymbolicName() {
		return location;
	}

	public Version getVersion() {
		return new Version("0");
	}

	public boolean hasPermission(Object permission) {
		return true;
	}

	public void start() throws BundleException {
		state = Bundle.ACTIVE;
	}

	public void start(int options) throws BundleException {
		state = Bundle.ACTIVE;
	}

	public void stop() throws BundleException {
		state = Bundle.RESOLVED;
	}

	public void stop(int options) throws BundleException {
		state = Bundle.RESOLVED;
	}

	public void uninstall() throws BundleException {
		state = Bundle.UNINSTALLED;
	}

	public void update() throws BundleException {
		throw new UnsupportedOperationException();
	}

	public void update(InputStream in) throws BundleException {
		throw new UnsupportedOperationException();
	}

	public void addBundleListener(BundleListener listener) {
		throw new UnsupportedOperationException();
	}

	public void addFrameworkListener(FrameworkListener listener) {
		throw new UnsupportedOperationException();
	}

	public void addServiceListener(ServiceListener listener) {
		throw new UnsupportedOperationException();
	}

	public void addServiceListener(ServiceListener listener, String filter) {
		throw new UnsupportedOperationException();
	}

	public Filter createFilter(String filter) throws InvalidSyntaxException {
		throw new UnsupportedOperationException();
	}

	public ServiceReference[] getAllServiceReferences(String clazz, String filter)
			throws InvalidSyntaxException {
		throw new UnsupportedOperationException();
	}

	public Bundle getBundle() {
		return this;
	}

	public Bundle getBundle(long id) {
		return fw.getBundle(id);
	}

	public Bundle[] getBundles() {
		return fw.getBundles();
	}

	public File getDataFile(String filename) {
		return null;
	}

	public String getProperty(String key) {
		return null;
	}

	public Object getService(ServiceReference reference) {
		throw new UnsupportedOperationException();
	}

	public ServiceReference getServiceReference(String clazz) {
		throw new UnsupportedOperationException();
	}

	public ServiceReference[] getServiceReferences(String clazz, String filter)
			throws InvalidSyntaxException {
		throw new UnsupportedOperationException();
	}

	public Bundle installBundle(String location) throws BundleException {
		return fw.installBundle(location);
	}

	public Bundle installBundle(String location, InputStream input) throws BundleException {
		return fw.installBundle(location, input);
	}

	public ServiceRegistration registerService(String[] clazzes, Object service,
			Dictionary properties) {
		Class<?> c;
		try {
			c = fw.loadClass(clazz);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class not found: " + clazz);
		}
		LocalServiceRegistration reg = new LocalServiceRegistration(this, fw.nextId(), c, service,
				properties);
		synchronized (fw.registry) {
			fw.registry.add(reg);
		}
		return reg;
	}

	public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
		registerService(new String[] {clazz}, service, properties);
	}

	public void removeBundleListener(BundleListener listener) {
		bundleListeners.remove(listener);
	}

	public void removeFrameworkListener(FrameworkListener listener) {
		frameworkListeners.remove(listener);
	}

	public synchronized void removeServiceListener(ServiceListener listener) {
		serviceListeners.remove(listener);
	}

	public boolean ungetService(ServiceReference reference) {
		LocalServiceRegistration reg = (LocalServiceRegistration) reference;
		return reg.unget(this);
	}

	public String toString() {
		return id + " " + location;
	}
}
