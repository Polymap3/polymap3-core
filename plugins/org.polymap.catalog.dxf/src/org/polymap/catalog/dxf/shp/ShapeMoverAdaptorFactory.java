package org.polymap.catalog.dxf.shp;

import java.io.IOException;

import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveAdapterFactory;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class has the responsibility of looking at the provided
 * DxfServiceImpl and deciding if it is is movable.
 * <p>
 * DxfServiceImpl by itself can represent a shape file on disk
 * (which is movable) or on a web service (which is not).
 * 
 * @deprecated Example code from Shapefile plugin.
 * @author Jody Garnett (Refractions Research)
 */
public class ShapeMoverAdaptorFactory implements IResolveAdapterFactory {

	/**
	 * Check the provided resolve (should be a DxfServiceImpl) and check
	 * if we provided the requested adapter.
	 */
    @SuppressWarnings("unchecked")
    public Object adapt( IResolve resolve, Class adapter, IProgressMonitor monitor )
            throws IOException {

        if (adapter.isAssignableFrom(ShapeMover.class)) {
        	// Note we create a new adapter each time; adapters
        	// are not supposed to hold much in the way of resources
        	// (ie the are lightweight)
        	// If that is too hard a programming model please let us
        	// know and we can cache this result (or provided session
        	// properties for you do store your adapter in
            return new ShapeMover(resolve);
        }
        return null;
    }


	/**
	 * We only know how to to the ShapeMover class, so client code
	 * can ask for ShapeMover by name; or the more generic ServiceMover.
	 * @param resolve IResolve handle; should be a ShpService
	 * @param adapter Interface we are being asked to adapt to
	 * @return true if the requested adapter is ShapeMover or SeviceMover.
	 */
	public boolean canAdapt(IResolve resolve, Class<? extends Object> adapter) {
		return adapter.isAssignableFrom(ShapeMover.class);
	}

}
