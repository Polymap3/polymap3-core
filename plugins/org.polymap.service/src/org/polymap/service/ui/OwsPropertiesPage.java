/* 
 * polymap.org
 * Copyright (C) 2010-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service.ui;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.AdaptableForwarder;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.operations.SetPropertyOperation;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.service.IProvidedService;
import org.polymap.service.Messages;
import org.polymap.service.ServiceContext;
import org.polymap.service.ServiceRepository;
import org.polymap.service.ServicesPlugin;
import org.polymap.service.model.operations.NewServiceOperation;

import net.refractions.udig.ui.CRSChooserDialog;

/**
 * Properties of OWS services of an {@link IMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class OwsPropertiesPage 
        extends FieldEditorPreferencePage
        implements IWorkbenchPropertyPage {

    private static Log log = LogFactory.getLog( OwsPropertiesPage.class );

	private static final int       TEXT_FIELD_WIDTH = 50;
	
	private IMap                   map;
	
	private IProvidedService       providedService;
	

    public OwsPropertiesPage() {
        super( FLAT );
        setDescription( Messages.get( "OwsPropertiesPage_description", ServicesPlugin.getDefault().getServicesBaseUrl() ) );
    }

    
    @Override
    protected Control createContents( Composite parent ) {
        noDefaultAndApplyButton();
        
        // XXX see PropertyDialogAction for more detail
        final ACL acl = getElement() instanceof AdaptableForwarder
                ? (ACL)((AdaptableForwarder)getElement()).getAdapter( ACL.class )
                : (ACL)getElement();
        
        // check permission
        if (!ACLUtils.checkPermission( acl, AclPermission.WRITE, false )) {
            Label l = new Label( parent, SWT.NONE );
            l.setText( "Keine Zugriffsberechtigung." );
            return l;
        }
        else {
            return super.createContents( parent );
        }
    }

    
    public IAdaptable getElement() {
        return map;
    }

    
    public void setElement( IAdaptable element ) {
        log.info( "element= " + element );
        map = (IMap)element;
        providedService = ServiceRepository.instance().findService( map, ServicesPlugin.SERVICE_TYPE_WMS );

        // create service entity if none exists
        if (providedService == null) {
            try {
                log.info( "No Service found, creating new..." );
                NewServiceOperation op = new NewServiceOperation();
                op.init( map, ServicesPlugin.SERVICE_TYPE_WMS );
                OperationSupport.instance().execute( op, false, false );

                providedService = ServiceRepository.instance().findService( map, ServicesPlugin.SERVICE_TYPE_WMS );
                
                ServiceContext context = ServicesPlugin.getDefault().initServiceContext( providedService );
                context.stopService();
                context.startService();
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( ServicesPlugin.PLUGIN_ID, this, "Fehler beim Anlegen des Service.", e );
            }
        }
        log.info( "    Provided Service: " + providedService );
    }


    protected void createFieldEditors() {
        IPreferenceStore store = new PreferenceStore() {
            public boolean needsSaving() {
                return false;
            }
        };
        setPreferenceStore( store );
        store.setDefault( "WMS", providedService.isEnabled() );
        store.setDefault( "WFS", false );
        store.setDefault( IProvidedService.PROP_PATHSPEC, providedService.getPathSpec() );
        store.setDefault( IProvidedService.PROP_NAMESPACE, providedService.getNamespace() );
        store.setDefault( IProvidedService.PROP_MAINTAINER, providedService.getMaintainer() );
        store.setDefault( IProvidedService.PROP_DESCRIPTION, providedService.getDescription() );
        store.setDefault( IProvidedService.PROP_SRS, StringUtils.join( providedService.getSRS(), "," ) );
        
        Composite uriParent = getFieldEditorParent();
        final Composite pathParent = getFieldEditorParent();

        // URI
        final StringFieldEditor uriField = new StringFieldEditor( "URI", "URI*", uriParent );
        addField( uriField );
        uriField.setStringValue( ServicesPlugin.createServiceUrl( providedService.getPathSpec() ) );
        uriField.getTextControl( uriParent ).setToolTipText( "The complete URI of this service." );
        uriField.setEnabled( false, uriParent );

        // service path
        StringFieldEditor pathField = new StringFieldEditor2(
                IProvidedService.PROP_PATHSPEC, "Service Name/Pfad*", "Der Name/Pfad, unter dem der Service erreichbar ist.\nBeginnend mit einem \"/\".", pathParent ) {

            protected boolean doCheckState() {
                String value = getStringValue();
                uriField.setStringValue( ServicesPlugin.createServiceUrl( value ) );

                String validName = ServicesPlugin.validPathSpec( value );
                if (!value.equals( validName )) {
                    setErrorMessage( "Der Name darf nur Buchstaben, Zahlen oder '-', '_', '.' enthalten." );
                    return false;
                }
                return true;
            }
        };
        addField( pathField );
        
        // WMS
        BooleanFieldEditor wmsField = new BooleanFieldEditor(
                "WMS", "WMS aktivieren", getFieldEditorParent() );
        addField( wmsField );
        
        // WFS
        BooleanFieldEditor wfsField = new BooleanFieldEditor(
                "WFS", "WFS aktivieren", getFieldEditorParent() );
//        wfsField.setEnabled( false, getFieldEditorParent() );

        // maintainer
        addField( new StringFieldEditor( IProvidedService.PROP_MAINTAINER, "Verantwortlich", pathParent ) );

        // description
        //    MultiLineTextFieldEditor descriptionField = new MultiLineTextFieldEditor(
        addField( new StringFieldEditor2( IProvidedService.PROP_DESCRIPTION, 
                "Beschreibung*", "Eine kurze Beschreibung des Dienstes", pathParent ) );

        // SRS
        ListEditor srsField = new ListEditor( IProvidedService.PROP_SRS, "Referenzsysteme", pathParent ) {
            @Override
            protected void doFillIntoGrid( Composite parent, int numColumns ) {
                super.doFillIntoGrid( parent, numColumns );
                GridData gd = (GridData)getListControl( parent ).getLayoutData();
                gd.grabExcessHorizontalSpace = false;
                gd.widthHint = 50;
            }
            @Override
            protected String[] parseString( String stringList ) {
                return StringUtils.split( stringList, "," );
            }
            @Override
            protected String getNewInputObject() {
                CRSChooserDialog dialog = new CRSChooserDialog( getShell(), null );
                dialog.setBlockOnOpen( true );
                if (dialog.open() == Window.OK) {
                    return Geometries.srs( dialog.getResult() );
                }
                else {
                    return null;
                }
            }
            @Override
            protected String createList( String[] items ) {
                return StringUtils.join( items, "," );
            }
        };
        addField( srsField );
        
        // load default values
        performDefaults();
    }

    
    public boolean performOk() {
        log.info( "performOk()..." );
        super.performOk();

        try {
            IPreferenceStore store = getPreferenceStore();
    
            storeString( store, IProvidedService.PROP_PATHSPEC );
            storeString( store, IProvidedService.PROP_DESCRIPTION );
            storeString( store, IProvidedService.PROP_NAMESPACE );
            storeString( store, IProvidedService.PROP_MAINTAINER );
            
            if (!store.isDefault( "WMS" )) {
                boolean value = store.getBoolean( "WMS" );
                SetPropertyOperation op = new SetPropertyOperation();
                op.init( IProvidedService.class, providedService, IProvidedService.PROP_ENABLED, value );
                OperationSupport.instance().execute( op, false, false );
            }
            
            if (!store.isDefault( IProvidedService.PROP_SRS )) {
                String value = store.getString( IProvidedService.PROP_SRS );
                List<String> srs = Arrays.asList( StringUtils.split( value, ", " ) ); 
                SetPropertyOperation op = new SetPropertyOperation();
                op.init( IProvidedService.class, providedService, IProvidedService.PROP_SRS, srs );
                OperationSupport.instance().execute( op, false, false );
            }
            
            // message box
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    MessageBox mbox = new MessageBox( 
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            SWT.OK | SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL );
                    mbox.setMessage( "Die Änderungen werden erst nach dem nächsten Speichern wirksam." );
                    mbox.setText( "Hinweis" );
                    mbox.open();
                }
            });
            return true;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( ServicesPlugin.PLUGIN_ID, this, "Fehler beim Speichern der Einstellungen.", e );
            return false;
        }
    }
    
    
    protected void storeString( IPreferenceStore store, String propName ) throws ExecutionException {
        if (!store.isDefault( propName )) {
            Object value = store.getString( propName );
            log.info( "    value: " + value );
            SetPropertyOperation op = new SetPropertyOperation();
            op.init( IProvidedService.class, providedService, propName, value );
            OperationSupport.instance().execute( op, false, false );
        }
    }

}