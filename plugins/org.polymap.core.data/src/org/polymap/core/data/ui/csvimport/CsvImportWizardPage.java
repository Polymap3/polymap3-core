/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.polymap.core.data.ui.csvimport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.IOException;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.service.IServiceHandler;
import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.rwt.widgets.UploadListener;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class CsvImportWizardPage extends WizardPage {

    public static final String        ID = "CsvImportWizardPage"; //$NON-NLS-1$

    private CoordinateReferenceSystem readCrs;

    private File                      csvFile     = null;

    private CsvImporter               csvImporter = new CsvImporter( null );
    
    private List<Object[]>            tableValues = new ArrayList<Object[]>();

    private TableViewer               tableViewer;

    private boolean                   is3d        = false;
    
    /** The line to be used to fill the table; see {@link #fillTableView(int)}. */
    private int                       lineIndex   = 1;


    public CsvImportWizardPage( String pageName, Map<String, String> params ) {
        super(ID);
        setTitle(pageName);
        setDescription(Messages.getString("CsvImportWizardPage.importasshape")); //$NON-NLS-1$
    }

    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite( parent, SWT.NONE );
        fileSelectionArea.setLayout( new GridLayout( 2, true ) );

        Group inputGroup = new Group(fileSelectionArea, SWT.None);
        inputGroup.setText("Choose the CSV file and encoding");
        inputGroup.setLayout(new GridLayout(3, false));
        GridData ld = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        ld.horizontalSpan = 2;
        inputGroup.setLayoutData( ld );
        GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData1.horizontalSpan = 3;

        String dummy = IServiceHandler.REQUEST_PARAM;
        
//        final Text csvText = new Text(inputGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
//        csvText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//        csvText.setText("");
        final Upload upload = new Upload( inputGroup, SWT.BORDER, /*Upload.SHOW_PROGRESS |*/ Upload.SHOW_UPLOAD_BUTTON );
        upload.setBrowseButtonText( "Browse" );
        upload.setUploadButtonText( "Upload" );
        upload.addUploadListener( new UploadListener() {
            public void uploadInProgress( UploadEvent ev ) {
            }
            public void uploadFinished( UploadEvent ev ) {
                UploadItem item = upload.getUploadItem();
                try {
                    System.out.println( "Item name: " + item.getFileName() );
//                    csvFile = File.createTempFile( item.getFileName() + "_", ".tmp");
//                    FileOutputStream out = new FileOutputStream( csvFile );
//                    StreamUtils.copyThenClose( item.getFileInputStream(), out );
//                    System.out.println( "## copied to: " + csvFile );

                    csvImporter.setInputStream( item.getFileInputStream() );
                    fillTableView(lineIndex);
                } 
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                checkFinish();
            }
        });

//        final Button csvButton = new Button(inputGroup, SWT.PUSH);
//        csvButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//        csvButton.setText("...");
//        csvButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
//            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
//                FileDialog fileDialog = new FileDialog(csvButton.getShell(), SWT.OPEN);
//                String path = fileDialog.open();
//                //String path = "/home/falko/jboss-4.0.3/polymap2.atlas/freiberg/Kita.csv";
//                if (path != null) {
//                    File f = new File(path);
//                    if (f.exists()) {
//                        csvText.setText(path);
//                        csvFile = f;
//                        csvImporter.setCsvFile( csvFile );
//                        try {
//                            fillTableView();
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                }
//                checkFinish();
//            }
//        });
        final Combo charsetCombo = new Combo(inputGroup, SWT.READ_ONLY);
        charsetCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        charsetCombo.setItems(new String [] {"ISO-8859-1", "UTF8"});
        charsetCombo.setSize(200, 200);
        charsetCombo.select( 0 );
        charsetCombo.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                csvImporter.prefs().setFileEncoding( charsetCombo.getText() );
                try {
                    fillTableView(lineIndex);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                checkFinish();
            }
        });

        // delimiter/quote char group
        Group separatorGroup = new Group(fileSelectionArea, SWT.None);
        separatorGroup.setLayout(new GridLayout(2, false));
        separatorGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        separatorGroup.setText( "The CSV delimiter and quote char" );

        final Text separatorText = new Text(separatorGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        separatorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        separatorText.setText(String.valueOf((char)csvImporter.prefs().getDelimiterChar()));
        separatorText.addKeyListener(new KeyAdapter(){
            public void keyReleased( KeyEvent e ) {
                String sep = separatorText.getText();
                if (sep.length() > 0) {
                    csvImporter.prefs().setDelimiterChar( sep.codePointAt( 0 ) );
                    try {
                        fillTableView(lineIndex);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    checkFinish();
                }
            }
        });

        final Text quoteText = new Text( separatorGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER );
        quoteText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        quoteText.setText( String.valueOf( (char)csvImporter.prefs().getQuoteChar() ) );
        quoteText.addKeyListener( new KeyAdapter() {
            public void keyReleased( KeyEvent e ) {
                String quote = quoteText.getText();
                if (quote.length() > 0) {
                    csvImporter.prefs().setQuoteChar( (char)quote.codePointAt( 0 ) );
                    try {
                        fillTableView(lineIndex);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    checkFinish();
                }
            }
        });

        // the crs choice group
        Group crsGroup = new Group(fileSelectionArea, SWT.None);
        crsGroup.setLayout(new GridLayout(2, false));
        crsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        crsGroup.setText("Coordinate reference system for the data");

        final Text crsText = new Text(crsGroup, SWT.BORDER);
        crsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        crsText.setEditable(true);
        try {
            crsText.setText( "EPSG:31468" );
            readCrs = CRS.decode( crsText.getText() );
        }
        catch (Exception e2) {
            throw new RuntimeException( e2 );
        }
        crsText.addKeyListener( new KeyListener() {            
            public void keyReleased( KeyEvent e ) {
                try {
                    readCrs = CRS.decode( crsText.getText() );
                    checkFinish();
                    setMessage( null );
                }
                catch (Exception e1) {
                    setMessage( e1.getMessage(), DialogPage.ERROR );
                }
            }
            public void keyPressed( KeyEvent e ) {
            }
        });

        final Button crsButton = new Button(crsGroup, SWT.BORDER);
        crsButton.setText(" Choose CRS ");
        crsButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                final ChooseCoordinateReferenceSystemDialog crsChooser = new ChooseCoordinateReferenceSystemDialog();
                crsChooser.open(new Shell(Display.getDefault()));
                CoordinateReferenceSystem crs = crsChooser.getCrs();
                if (crs == null)
                    return;
                crsText.setText(crs.getName().toString());
                readCrs = crs;
                checkFinish();
            }
        });

        createTableArea(fileSelectionArea);

        // bottom group
        Group bottomGroup = new Group(fileSelectionArea, SWT.None);
        bottomGroup.setLayout(new GridLayout(2, false));
        bottomGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        bottomGroup.setText("Line index and header line");

        // lineIndex spinner
        final Spinner lineSpinner = new Spinner(bottomGroup, SWT.BORDER);
        lineSpinner.setMinimum(1);
        lineSpinner.setSelection(lineIndex);
        lineSpinner.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                int old = lineIndex;
                try {
                    lineIndex = lineSpinner.getSelection();
                    System.out.println( "lineIndex= " + lineIndex );
                    fillTableView(lineIndex);
                }
                catch (IndexOutOfBoundsException e1) {
                    lineSpinner.setSelection( old );
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                checkFinish();
            }
        });
        
        // CSV header checkbox
        final Button headerCheck = new Button(bottomGroup, SWT.CHECK);
        headerCheck.setSelection( csvImporter.prefs().isUseHeader() );
        headerCheck.addSelectionListener( new SelectionListener() {
            public void widgetSelected( SelectionEvent arg0 ) {
                try {
                    System.out.println( "useHeader= " + headerCheck.getSelection() );
                    csvImporter.prefs().setUseHeader( headerCheck.getSelection() );
                    fillTableView(lineIndex);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    setMessage( e.getMessage(), DialogPage.ERROR );
                }
            }
            public void widgetDefaultSelected( SelectionEvent arg0 ) {
            }
        });
        
        // extensions group
        Group extensionsGroup = new Group( fileSelectionArea, SWT.None );
        extensionsGroup.setLayout( new GridLayout( 2, false ) );
        extensionsGroup.setLayoutData( new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL) );
        extensionsGroup.setText( "Operations" );
        createExtensionsArea( extensionsGroup );

        checkFinish();
        setControl(fileSelectionArea);
    }

    private void createTableArea( Composite fileSelectionArea ) {
        GridData gridData1 = new GridData();
        gridData1.horizontalSpan = 2;
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.grabExcessVerticalSpace = true;
        gridData1.verticalAlignment = GridData.FILL;
        Composite comp = new Composite(fileSelectionArea, SWT.NONE);
        comp.setLayout(new FillLayout());
        comp.setLayoutData(gridData1);
        // table
        tableViewer = new TableViewer(comp, SWT.BORDER | SWT.V_SCROLL);
        final Table table = tableViewer.getTable();
        //table.setLayoutData(gridData1);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        final String nameString = "Fieldname";
        final String valueString = "Example value";
        final String typeString = "Type";
        // columns
        TableColumn name = new TableColumn(table, SWT.NONE);
        name.setText(nameString);
        TableColumn value = new TableColumn(table, SWT.NONE);
        value.setText(valueString);
        TableColumn type = new TableColumn(table, SWT.NONE);
        type.setText(typeString);
        // layout
        TableColumnLayout layout = new TableColumnLayout();
        layout.setColumnData(name, new ColumnWeightData(40, true));
        layout.setColumnData(value, new ColumnWeightData(40, true));
        layout.setColumnData(type, new ColumnWeightData(20, true));
        comp.setLayout(layout);
        comp.pack();

        // activate editing
        tableViewer.setColumnProperties(new String[]{nameString, valueString, typeString});
        tableViewer.setCellModifier(new ICellModifier(){

            public boolean canModify( Object element, String property ) {
                if (property.equals(valueString)) {
                    return false;
                }
                return true;
            }

            public Object getValue( Object element, String property ) {
                Object[] e = (Object[]) element;
                if (property.equals(nameString)) {
                    return e[0];
                }
                if (property.equals(typeString)) {
                    return e[2];
                }
                return "";
            }

            public void modify( Object element, String property, Object value ) {
                TableItem tabItem = (TableItem) element;
                Object[] data = (Object[]) tabItem.getData();
                if (property.equals(nameString)) {
                    data[0] = value;
                }
                if (property.equals(typeString)) {
                    data[2] = value;
                }
                tableViewer.refresh(data);
                checkFinish();
            }

        });
        tableViewer.setCellEditors(new CellEditor[]{new TextCellEditor(table),
                new TextCellEditor(table),
                new ComboBoxCellEditor(table, new String[]{"X", "Y", "Z", "String", "Double", "Integer"})});  //JGrassConstants.CSVTYPESARRAY)});

        // the label provider
        tableViewer.setLabelProvider(new ITableLabelProvider(){

            public Image getColumnImage( Object element, int columnIndex ) {
                return null;
            }

            public String getColumnText( Object element, int columnIndex ) {
                Object[] e = (Object[]) element;
                switch( columnIndex ) {
                case 0:
                    return (String) e[0];
                case 1:
                    return (String) e[1];
                case 2:
                    return JGrassConstants.CSVTYPESARRAY[(Integer) e[2]];
                default:
                    break;
                }
                return "";
            }

            public void addListener( ILabelProviderListener listener ) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty( Object element, String property ) {
                return false;
            }

            public void removeListener( ILabelProviderListener listener ) {
            }

        });
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setInput(tableValues);

    }

    private void fillTableView(int _lineIndex) throws IOException {
        tableValues = csvImporter.getColumns( _lineIndex, tableValues );
        tableViewer.setInput( tableValues );
    }

    public CsvImporter getCsvImporter() {
        return csvImporter;
    }

    public String getSeparator() {
        return String.valueOf( csvImporter.prefs().getDelimiterChar() );
    }

    public LinkedHashMap<String, Integer> getFieldsAndTypesIndex() {
        LinkedHashMap<String, Integer> fieldNamesToTypesIndex = new LinkedHashMap<String, Integer>();
        for( int i = 0; i < tableValues.size(); i++ ) {
            Object[] values = tableValues.get(i);
            fieldNamesToTypesIndex.put((String) values[0], (Integer) values[2]);
        }
        return fieldNamesToTypesIndex;
    }

    public boolean is3d() {
        return is3d;
    }

    public CoordinateReferenceSystem getCrs() {
        return readCrs;
    }

    private void checkFinish() {
        boolean hasX = false;
        boolean hasY = false;
        for (int i=0; i<tableValues.size(); i++) {
            Object[] values = tableValues.get(i);
            Integer type = (Integer) values[2];
            if (type == 0) {
                hasX = true;
            }
            if (type == 1) {
                hasY = true;
            }
        }

        if (!hasX || !hasY || csvImporter == null || csvImporter.getData() == null || readCrs == null) {
            ((CsvImportWizard)getWizard()).canFinish = false;
            setPageComplete(false);
        } 
        else {
            ((CsvImportWizard)getWizard()).canFinish = true;
            setPageComplete(true);
        }

//        // complete -> job: create fc
//        if (isPageComplete()) {
//            PlatformJobs.runInProgressDialog( "Reading data", false, new IRunnableWithProgress() {
//                public void run( IProgressMonitor monitor )
//                        throws InvocationTargetException, InterruptedException {
//                    ((CsvImportWizard)getWizard()).createCsvFeatureCollection();
//                }
//            }, false );
//        }
        
        getWizard().getContainer().updateButtons();
    }

    
    private void createExtensionsArea( Group parent ) {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] exts = reg.getConfigurationElementsFor( CsvOperation.EXTENSION_POINT );
        for (int i=0; i < exts.length; i++) {
            try {
                final CsvOperation op = (CsvOperation)exts[i].createExecutableExtension( "class" );
                Button btn = new Button( parent, SWT.BORDER );
                btn.setSize( 0, 22 );
                btn.setText( exts[i].getAttribute( "label" ));
                btn.setToolTipText( exts[i].getAttribute( "tooltip" ));
                btn.addSelectionListener( new org.eclipse.swt.events.SelectionAdapter() {
                    public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                        try {
                            op.perform( csvImporter );
                            fillTableView( lineIndex );
                            checkFinish();
                        }
                        catch (Exception e1) {
                            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, null, e1 );
                        }
                    }
                });
            }
            catch (CoreException e) {
                PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
    }
    
}
