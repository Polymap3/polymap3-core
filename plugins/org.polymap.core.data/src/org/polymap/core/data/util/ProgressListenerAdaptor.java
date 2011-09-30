package org.polymap.core.data.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 *
 * <p/>
 * beginTask() has to be called on the underlying monitor!
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class ProgressListenerAdaptor
        implements ProgressListener {

    private String              description;

    private int                 progress;

    private InternationalString task;

    private IProgressMonitor    monitor;


    public ProgressListenerAdaptor( IProgressMonitor monitor ) {
        this.monitor = monitor;
    }

    public void complete() {
        monitor.done();
    }

    public void dispose() {
        description = null;
    }

    public void exceptionOccurred( Throwable e ) {
        e.printStackTrace();
        monitor.setCanceled( true );
    }

    public String getDescription() {
        return description;
    }

    public boolean isCanceled() {
        return monitor.isCanceled();
    }

    public void progress( float amount ) {
//        int current = (int)(100.0 * amount);
//        monitor.worked( current - progress );
//        progress = current;
        monitor.worked( (int)amount );
    }

    public void setCanceled( boolean arg0 ) {
        monitor.setCanceled( true );
    }

    public void setDescription( String text ) {
        description = text;
    }

    public void started() {
        // don't call beginTask() here as we dont know what the total amount of work is;
        // let the caller do this on the underlying monitor.
//        monitor.beginTask( description, 100 );
    }

    public void warningOccurred( String arg0, String arg1, String arg2 ) {
    }

    public InternationalString getTask() {
        return task;
    }

    public void setTask( InternationalString task ) {
        this.task = task;
        monitor.subTask( task.toString() );
    }

    public float getProgress() {
        return progress;
    }

}