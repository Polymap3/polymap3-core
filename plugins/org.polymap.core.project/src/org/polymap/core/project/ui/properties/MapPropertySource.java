package org.polymap.core.project.ui.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Labeled;
import org.polymap.core.project.MapStatus;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.SetPropertyOperation;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * The property source of {@link IMap}. Allows the user to directly
 * change some properties.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class MapPropertySource
        implements IPropertySource, IPropertySource2 {

    private static Log log = LogFactory.getLog( MapPropertySource.class );
    
    private IMap                map;

    
    public MapPropertySource( IMap map ) {
        this.map = map;
    }

    
    protected String i18n( String key, Object...args ) {
        return Messages.get( "MapProperty_" + key, args );
    }
        

    public IPropertyDescriptor[] getPropertyDescriptors() {
        NumberFormat nf = NumberFormat.getInstance( Polymap.getSessionLocale() );
        nf.setMaximumFractionDigits( 0 );

        IPropertyDescriptor[] result = new IPropertyDescriptor[] {
                new RWTTextPropertyDescriptor( IMap.PROP_LABEL, i18n( "label_name" ) ),
                new CrsPropertyDescriptor( IMap.PROP_CRSCODE, i18n( "label_crs" ) ),
                new PropertyDescriptor( IMap.PROP_MAXEXTENT, i18n( "label_maxExtent" ) ),
                new RWTTextPropertyDescriptor( IMap.PROP_SCALES, i18n( "label_scales" ) ),
                new NumberPropertyDescriptor( IMap.PROP_DPI, "DPI" ).setFormat( nf ).setEditable( true ) 
        };
        return result;
    }

    
    public Object getPropertyValue( Object id ) {
        //map.setMapStatus( MapStatus.STATUS_OK );
        try {
            if (id.equals( IMap.PROP_LABEL )) {
                return map.getLabel();
            }
            else if (id.equals( IMap.PROP_CRSCODE )) {
                return map.getCRS();
            }
            else if (id.equals( IMap.PROP_MAXEXTENT )) {
                EnvelopPropertySource result = new EnvelopPropertySource( map.getMaxExtent() );
                result.setEditable( new PropertyChangeListener() {
                    public void propertyChange( PropertyChangeEvent ev ) {
                        try {
                            SetPropertyOperation op = new SetPropertyOperation();
                            op.init( IMap.class, map, IMap.PROP_MAXEXTENT, ev.getNewValue() );
                            OperationSupport.instance().execute( op, false, false );
                        }
                        catch (Exception e) {
                            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, "Unable to change extent of the map.", e );
                        }
                    }
                });
                return result;
            }
            else if (id.equals( IMap.PROP_SCALES )) {
                StringBuilder buf = new StringBuilder( 256 );
                for (int s : map.getScales() ) {
                    buf.append( buf.length()>0 ? "," : "" ).append( s );
                }
                return buf.toString();
            }
            else if (id.equals( IMap.PROP_DPI )) {
                return map.getDPI();
            }
            else {
                return i18n( "unknownValue" );
            }
        }
        catch (Exception e) {
            log.error( "Error while getting property: " + id, e );
            MapStatus error = new MapStatus( MapStatus.ERROR, MapStatus.UNSPECIFIED, i18n( "valueError", id ), e );
            if (map.getMapStatus().isOK()) {
                map.setMapStatus( error );
            } else {
                map.getMapStatus().add( error );
            }
            return "Fehler: " + e.getMessage();
        }
    }

    
    public void setPropertyValue( Object id, Object value ) {
        try {
            SetPropertyOperation op = new SetPropertyOperation();

            if (id.equals( IMap.PROP_LABEL )) {
                op.init( Labeled.class, map, IMap.PROP_LABEL, value );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( IMap.PROP_CRSCODE )) {
                String srs = CRS.toSRS( (CoordinateReferenceSystem)value );
                if (srs != null) {
                    op.init( IMap.class, map, IMap.PROP_CRSCODE, srs );
                    OperationSupport.instance().execute( op, false, false );
                }
            }
            else if (id.equals( IMap.PROP_SCALES )) {
                String[] a = StringUtils.split( (String)value, ", " );
                int[] scales = new int[a.length];
                int i = 0;
                for (String s : a) {
                    scales[i++] = Integer.parseInt( s );
                }
                op.init( IMap.class, map, IMap.PROP_SCALES, scales );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( IMap.PROP_DPI )) {
                op.init( IMap.class, map, IMap.PROP_DPI, ((Long)value).intValue() );
                OperationSupport.instance().execute( op, false, false );
            }
            else {
                log.error( "Property is read-only: " + id );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, "Unable to change property: " + id, e );
        }
    }
    
    public boolean isPropertyResettable( Object id ) {
        return false;
    }

    public boolean isPropertySet( Object id ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public Object getEditableValue() {
        return null;
    }

    public void resetPropertyValue( Object id ) {
        throw new RuntimeException( "not yet implemented." );
    }

}