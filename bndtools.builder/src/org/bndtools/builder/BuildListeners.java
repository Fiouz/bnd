package org.bndtools.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bndtools.api.BndtoolsConstants;
import org.bndtools.api.ILogger;
import org.bndtools.api.Logger;
import org.bndtools.build.api.BuildListener;
import org.bndtools.utils.Function;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import bndtools.central.Central;

public class BuildListeners {
    private static final ILogger logger = Logger.getLogger(BuildListeners.class);

    private final List<BuildListener> listeners;
    private final ServiceTracker<BuildListener,BuildListener> listenerTracker;

    public BuildListeners() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(BndtoolsConstants.CORE_PLUGIN_ID, "buildListeners");
        listeners = new ArrayList<BuildListener>(elements.length);

        for (IConfigurationElement elem : elements) {
            try {
                BuildListener listener = (BuildListener) elem.createExecutableExtension("class");
                listeners.add(listener);
            } catch (Exception e) {
                logger.logError("Unable to instantiate build listener: " + elem.getAttribute("name"), e);
            }
        }

        BundleContext context = FrameworkUtil.getBundle(BuildListeners.class).getBundleContext();

        listenerTracker = new ServiceTracker<BuildListener,BuildListener>(context, BuildListener.class, null);
        listenerTracker.open();
    }

    public void fireBuildStarting(final IProject project) {
        forEachListener(new Function<BuildListener,Object>() {
            @Override
            public Object run(BuildListener listener) {
                listener.buildStarting(project);
                return null;
            }
        });
    }

    public void fireBuiltBundles(final IProject project, final IPath[] paths) {
        forEachListener(new Function<BuildListener,Object>() {
            @Override
            public Object run(BuildListener listener) {
                listener.builtBundles(project, paths);
                return null;
            }
        });
    }

    public void fireReleased(final IProject project) {
        forEachListener(new Function<BuildListener,Object>() {
            @Override
            public Object run(BuildListener listener) {
                listener.released(project);
                return null;
            }
        });
    }

    private void forEachListener(Function<BuildListener, ? extends Object> function) {
        for (BuildListener listener : listeners)
            function.run(listener);

        Object[] services = listenerTracker.getServices();
        if (services != null) {
            for (Object service : services) {
                if (service != null)
                    function.run((BuildListener) service);
            }
        }
    }

    /**
     * Call this to make sure that any references to the listeners are no longer held.
     */
    public void release(IProject project) {
        fireReleased(project);
        listeners.clear();
        listenerTracker.close();
    }

    public void updateListeners(File[] buildFiles, IProject project) throws Exception {
        // Notify the build listeners
        if (buildFiles.length > 0) {
            IPath[] paths = new IPath[buildFiles.length];
            for (int i = 0; i < buildFiles.length; i++)
                paths[i] = Central.toPath(buildFiles[i]);
            fireBuiltBundles(project, paths);
        }
    }

}
