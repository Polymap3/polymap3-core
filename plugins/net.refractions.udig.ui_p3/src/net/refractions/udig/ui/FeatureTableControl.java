package net.refractions.udig.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import java.lang.reflect.Array;

import net.refractions.udig.core.IProvider;
import net.refractions.udig.internal.ui.Trace;
import net.refractions.udig.internal.ui.UiPlugin;
import net.refractions.udig.ui.internal.Messages;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import org.eclipse.ui.part.PageBook;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A TreeViewer control for viewing a table of SimpleFeature attributes.
 * <p>
 * The object is used by using a FeatureCollection. In this case the control hangs on to a reference
 * to the FeatureCollection and populates the table entries directory from it. This method results
 * in a single page containing all features.
 * </p>
 * <p>
 * If the FeatureCollection implements the {@link IAdaptable} interface and adapts to
 * {@link ICellModifier} then the table is editable. The {@link ICellModifier} is used to modify the
 * features. The Column properties passed to the {@link ICellModifier} are the attribute name of the
 * attribute being modified.
 * </p>
 * <p>
 * If the FeatureCollection implements the {@link IAdaptable} interface and adapts to
 * {@link CellEditor[]} then the cell editors will be used to edit the cells. This is optional for
 * editing. By default a {@link TextCellEditor} is used for editing most cells, and an
 * {@link AttributeValidator} is used to validate the new values. The first column is for the fid
 * column and will not be used since FIDS are assigned by the datastore and can not be modified. The
 * number of Items the array (this is the same for the cell editor validators and cell editor
 * listeners) must be either the number of attributes in the feature type or the number of
 * attributes + 1 (one for the FID column). If the number of editors it Attributes+1 then the first
 * element in the array will not be used as it is assumed to be a placeholder for the fid column.
 * </p>
 * <p>
 * If the FeatureCollection implements the {@link IAdaptable} interface and adapts to
 * {@link ICellEditorValidator[]} then the validators will be used to validate the cells.
 * </p>
 * <p>
 * If the FeatureCollection implements the {@link IAdaptable} interface and adapts to
 * {@link ICellEditorListener[]} then the listeners will be added to the {@link CellEditor}s.
 * </p>
 * 
 * @author jdeolive
 * @author jeichar
 * @since 0.3
 */
public class FeatureTableControl implements ISelectionProvider {

    public static final String FEATURE_ID_COLUMN_PROPERTY = "FeatureIDProperty"; //$NON-NLS-1$

    public static final Object ERROR_COLUMN_PROPERTY = "ErrorProperty"; //$NON-NLS-1$

    public static final Object LOADING = new Object();

    /** results per page * */
    private int pageSize = 10; // XXX: actual put this as a user pref

    /** table viewer control * */
    private TableViewer tableViewer;

    private PageBook book;

    private Text message;

    FeatureCollection<SimpleFeatureType, SimpleFeature>  features;

    private final IProvider<IProgressMonitor> progressMonitorProvider;

    /** This the {@link #tableViewer}; see http://polymap.org/svn-anta2/ticket/185 */ 
    private ISelectionProvider selectionProvider;

    private Color messageBackground;

    private Color messageForeground;

    private Set<IFeatureTableLoadingListener> loadingListeners=new CopyOnWriteArraySet<IFeatureTableLoadingListener>();

    // 185: Suchergebnisse mit Doppelklick �ffnen (http://polymap.org/svn-anta2/ticket/185)
    private Set<IDoubleClickListener> dclickListeners=new CopyOnWriteArraySet<IDoubleClickListener>();

    private Set<ISelectionChangedListener> selectionListeners=new CopyOnWriteArraySet<ISelectionChangedListener>();

    private Comparator<SimpleFeature> currentComparator;

    private MenuManager contextMenu;

    private IProvider<RGB> selectionColor;

    private boolean shown;
    
    /**
     * Construct <code>FeatureTableControl</code>.
     * <p>
     * Must call setFeatures before use.
     * </p>
     */
    public FeatureTableControl() {
        this(ProgressManager.instance());
    }

    /**
     * Construct a <code>FeatureTableControl</code>.
     * 
     * @param monitorProvider a provider that will provider progress monitors for displaying loading
     *        information.
     * @param fReader The FeatureReader that returns the actual features.
     * @param resPerPage Results per page to be shown in the table.
     */
    public FeatureTableControl( Composite parent, FeatureCollection<SimpleFeatureType, SimpleFeature>  features ) {
        this(ProgressManager.instance(), parent, features);
    }
    /**
     * Construct <code>FeatureTableControl</code>.
     * <p>
     * Must call setFeatures before use.
     * </p>
     * 
     * @param monitorProvider a provider that will provider progress monitors for displaying loading
     *        information.
     */
    public FeatureTableControl( final IProvider<IProgressMonitor> monitorProvider ) {
        this.progressMonitorProvider = monitorProvider;
    }

    /**
     * Construct a <code>FeatureTableControl</code>.
     * 
     * @param monitorProvider a provider that will provider progress monitors for displaying loading
     *        information.
     * @param fReader The FeatureReader that returns the actual features.
     * @param resPerPage Results per page to be shown in the table.
     */
    public FeatureTableControl( final IProvider<IProgressMonitor> monitorProvider,
            Composite parent, FeatureCollection<SimpleFeatureType, SimpleFeature>  features ) {
        this(monitorProvider);
        this.features = features;
        createTableControl(parent);
    }

    /**
     * Sets the number of features viewed in the table per page.
     * 
     * @param resPerPage positive integer.
     */
    public void setPageSize( int resPerPage ) {
        this.pageSize = resPerPage;
    }

    /**
     * Returns the number of features viewed in the table per page.
     * 
     * @return positive integer.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns the control representing the table control.
     * 
     * @return The internal table viewer control.
     */
    public Control getControl() {
        return book;
    }
    
    public void dispose(){
        disposeTableViewer();
    }

    /**
     * Creates the table control.
     * 
     * @param parent The to be parent of the control.
     */
    public void createTableControl( Composite parent ) {
        book = new PageBook(parent, SWT.NONE);
        message = new Text(book, SWT.WRAP);
        messageBackground=message.getBackground();
        messageForeground=message.getForeground();
        createTableViewer(book);
    }

    /**
     * Key for indicating whether the warning should be displayed.  false if the warning is displayed
     */
    public static final String CACHING_WARNING = "FEATURE_TABLE_CACHING_IN_MEMORY_WARNING"; //$NON-NLS-1$

    /**
     * Indicates that all attribute types will be searched by the select method
     * @see #select(String, String[], boolean)
     */
    public static final String[] ALL = new String[0];

    private static final boolean SHOW_PATH = false;

    /**
     * show the warning about loading features into memory.
     * 
     * @returns true if the user wishes to continue to load features; false otherwise.
     * 
     */
    public boolean showWarning( Display display ) {
        
        IPreferenceStore preferenceStore = UiPlugin.getDefault().getPreferenceStore();
        if (!preferenceStore.getBoolean(CACHING_WARNING) && !shown) {
            shown=true;
           
            MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(display
                    .getActiveShell(), Messages.FeatureTableControl_warningTitle,
                    Messages.FeatureTableControl_warningMessage,
                    Messages.FeatureTableControl_warningToggle, false, null, null);
//            MessageDialogWithToggle dialog = MessageDialogWithToggle.openWarning(display
//                    .getActiveShell(), Messages.FeatureTableControl_warningTitle,
//                    Messages.FeatureTableControl_warningMessage,
//                    Messages.FeatureTableControl_warningToggle, false, null, null);
            preferenceStore.setValue(CACHING_WARNING, dialog.getToggleState());
            if (dialog.getReturnCode() ==MessageDialogWithToggle.OK){
                return true;
            }else{
                return false;
            }

        }
        return true;

    }

    /**
     * Updates the table control with the current set of features.
     * <p>
     * This method will ensure that the column information gets updated
     * </p>
     */
    public void update() {
        checkWidget();
        if( tableViewer!=null ){
            tableViewer.setInput(features);
            tableViewer.getTable().clearAll();
        }
    }

    /**
     * Creates the table control itself.
     * 
     * @param parent
     */
    protected void createTableViewer( Composite parent ) {
        int style = SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL;
        if (tableViewer != null) {
            disposeTableViewer();
        }
        final Table table = new Table(parent, style);
        table.setLinesVisible(true);
        TableLayout layout = new TableLayout();
        table.setLayout(layout);

        FeatureTableContentProvider ftp = new FeatureTableContentProvider(this, this.progressMonitorProvider);
        FeatureTableLabelProvider flp = new FeatureTableLabelProvider(this);
        flp.setSelectionColor(selectionColor);
        tableViewer = new TableViewer(table);
        
        tableViewer.setContentProvider(ftp);
        tableViewer.setLabelProvider(flp);
        tableViewer.setSorter( new FeatureTableViewerSorter() );
        
//        // #185: Suchergebnisse mit Doppelklick �ffnen (http://polymap.org/svn-anta2/ticket/185)
        selectionProvider = tableViewer;
        tableViewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                for (ISelectionChangedListener l : selectionListeners) {
                    l.selectionChanged( ev );
                }
            }
        });
        tableViewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                notifyDClickListeners( ev );
            }
        });

        // create columns after tableViewer is created because Column listeners need to access the
        // tableViewer.
        createAttributeColumns(table, tableViewer, layout);
        table.setHeaderVisible(true);
        
        if (features instanceof IAdaptable
                && ((IAdaptable) features).getAdapter(ICellModifier.class) != null) {

            IAdaptable adaptable = (IAdaptable) features;
            SimpleFeatureType schema = features.getSchema();
            int attributeCount = schema.getAttributeCount();
            setCellEditors(adaptable, attributeCount);

            setCellValidators(adaptable);
            addCellEditorListeners(adaptable);
            tableViewer.setCellModifier((ICellModifier) adaptable.getAdapter(ICellModifier.class));

            String[] properties = new String[attributeCount + 1];
            for( int i = 0; i < properties.length; i++ ) {
                if (i == 0)
                    properties[i] = FEATURE_ID_COLUMN_PROPERTY;
                else {
                    properties[i] = schema.getDescriptor(i - 1).getName().getLocalPart();
                }
            }
            tableViewer.setColumnProperties(properties);
        }
        if( contextMenu!=null ){
            Menu menu = contextMenu.createContextMenu(tableViewer.getControl());
            tableViewer.getControl().setMenu(menu);
        }
        book.showPage(tableViewer.getControl());

        UiPlugin.trace(Trace.FEATURE_TABLE, getClass(), "createTableViewer(): showing table View", SHOW_PATH?new Exception():null); //$NON-NLS-1$

        if (features != null) {
            tableViewer.setInput(features);
        }
        
    }

    private void disposeTableViewer() {
        if( tableViewer==null )
            return;
        IContentProvider contentProvider = tableViewer.getContentProvider();
        if( contentProvider!=null)
            contentProvider.dispose();
        IBaseLabelProvider labelProvider = tableViewer.getLabelProvider();
        if( labelProvider!=null )
            labelProvider.dispose();
        Control control = tableViewer.getControl();
        if( control!=null )
            control.dispose();
        tableViewer = null;
    }

    private void setCellEditors( IAdaptable adaptable, int attributeCount ) {
        if (adaptable.getAdapter(CellEditor[].class) != null) {
            CellEditor[] editors = (CellEditor[]) adaptable.getAdapter(Array.class);
            if (editors.length < attributeCount) {
                UiPlugin.log(
                        "not enough cell editors for feature type so not used", new Exception()); //$NON-NLS-1$
                createCellEditors();
            } else {
                CellEditor[] copy = new CellEditor[editors.length + 1];
                if (editors.length == attributeCount) {
                    // there is an editor for each attribute. First element in copy if for the
                    // fid column (which is not editable).
                    System.arraycopy(editors, 0, copy, 1, attributeCount);
                } else {

                    // ignore 1st element in editors because it is for the FID column which is read
                    // only.
                    System.arraycopy(editors, 1, copy, 1, attributeCount);

                }
                tableViewer.setCellEditors(copy);
            }
        } else {
            createCellEditors();
        }
    }

    private void addCellEditorListeners( IAdaptable adaptable ) {
        CellEditor[] editors = tableViewer.getCellEditors();
        // offset is usually 1 but if the number of validators==number of attributes then the offset
        // is 0
        // because the first column is the FID column which doesn't have a listener.
        int offset = 1;

        ICellEditorListener[] listener = null;
        if (adaptable.getAdapter(ICellEditorListener[].class) != null) {
            listener = (ICellEditorListener[]) adaptable.getAdapter(ICellEditorListener[].class);
            int attributeCount = features.getSchema().getAttributeCount();
            if (listener.length < attributeCount) {
                UiPlugin.log(
                        "not enough cell editors for feature type so not used", new Exception()); //$NON-NLS-1$
                return;
            } else if (listener.length == attributeCount+1) {
                offset = 0;
            }
        }

        for( int i = 0; i < editors.length - offset; i++ ) {
            final CellEditor editor = editors[i + offset];
            if (editor == null)
                continue;
            if (listener != null && listener[i] != null)
                editor.addListener(listener[i]);
            editor.addListener(new DisplayErrorCellListener(editor));
        }
    }

    private void setCellValidators( IAdaptable adaptable ) {
        CellEditor[] editors = tableViewer.getCellEditors();
        SimpleFeatureType schema = features.getSchema();
        // offset is usually 1 but if the number of validators==number of attributes then the offset
        // is 0
        // because the first column is the FID column which doesn't have a listener.
        int offset = 1;

        ICellEditorValidator[] validators = null;
        if (adaptable.getAdapter(ICellEditorValidator[].class) != null) {
            validators = (ICellEditorValidator[]) adaptable.getAdapter(ICellEditorValidator[].class);
            int attributeCount = features.getSchema().getAttributeCount();
            if (validators.length < attributeCount) {
                UiPlugin.log(
                        "not enough cell editors for feature type so not used", new Exception()); //$NON-NLS-1$
                validators = null;
            } else if (validators.length == attributeCount) {
                offset = 0;
            }
        }

        for( int i = 0; i < editors.length - offset; i++ ) {
            CellEditor editor = editors[i + offset];
            if (editor == null)
                continue;
            if (validators != null && validators[i] != null)
                editor.setValidator(validators[i]);
            else
                editor.setValidator(new AttributeValidator(schema.getDescriptor(i), schema));
        }
    }

    @SuppressWarnings("unchecked") 
    private void createCellEditors() {
        SimpleFeatureType schema = features.getSchema();
        org.eclipse.jface.viewers.CellEditor[] editors = new org.eclipse.jface.viewers.CellEditor[schema
                .getAttributeCount() + 1];

        for( int i = 0; i < schema.getAttributeCount(); i++ ) {
            AttributeDescriptor aType = schema.getDescriptor(i);
            Class< ? extends Object> concreteType = aType.getType().getBinding();
            Composite control = (Composite) tableViewer.getControl();
            if (concreteType.isAssignableFrom(String.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control, String.class);
                editors[i + 1] = textCellEditor;
            } else if (concreteType.isAssignableFrom(Integer.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control, Integer.class);
                editors[i + 1] = textCellEditor;
            } else if (concreteType.isAssignableFrom(Double.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control, Double.class);
                editors[i + 1] = textCellEditor;
            } else if (concreteType.isAssignableFrom(Float.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control, Float.class);
                editors[i + 1] = textCellEditor;

            } else if (concreteType.isAssignableFrom(Boolean.class)) {
                BooleanCellEditor textCellEditor = new BooleanCellEditor(control);
                editors[i + 1] = textCellEditor;

            } else if (concreteType.isAssignableFrom(Character.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control,
                        Character.class);
                editors[i + 1] = textCellEditor;

            }
            // else if( concreteType.isAssignableFrom(Date.class)){
            // WarningCellEditor textCellEditor = new WarningCellEditor(control, "The Date type does
            // not yet have a editor, please make a bug report for this Attribute Type");
            // editors[i+1]=textCellEditor;
            //                
            // }
            else if (concreteType.isAssignableFrom(Byte.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control, Byte.class);
                editors[i + 1] = textCellEditor;

            } else if (concreteType.isAssignableFrom(Short.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control, Short.class);
                editors[i + 1] = textCellEditor;

            } else if (concreteType.isAssignableFrom(Long.class)) {
                BasicTypeCellEditor textCellEditor = new BasicTypeCellEditor(control, Long.class);
                editors[i + 1] = textCellEditor;

            } else {
                WarningCellEditor textCellEditor = new WarningCellEditor(control,
                        Messages.FeatureTableControl_noEditor1 + concreteType.getSimpleName()
                                + Messages.FeatureTableControl_noEditor2);
                editors[i + 1] = textCellEditor;
            }
        }
        tableViewer.setCellEditors(editors);
    }

    private void createAttributeColumns( final Table table, TableViewer viewer, TableLayout layout ) {

        if (features == null) {
            TableColumn column = new TableColumn(table, SWT.CENTER | SWT.BORDER);
            column.setText(Messages.FeatureTableControl_1); 
            layout.addColumnData(new ColumnWeightData(1));
        }else{

            SimpleFeatureType schema = features.getSchema();
            

            TableColumn column = new TableColumn(table, SWT.CENTER | SWT.BORDER);
            column.setText("ID"); //$NON-NLS-1$
            layout.addColumnData(new ColumnWeightData(1, 50, true));
            column.setMoveable(true);

            column.addListener(SWT.Selection, new AttributeColumnSortListener(this,
                    FEATURE_ID_COLUMN_PROPERTY));

            for( int i = 0; i < schema.getAttributeCount(); i++ ) {
                AttributeDescriptor aType = schema.getDescriptor(i);
                
//                column = Number.class.isAssignableFrom( aType.getType().getBinding() )
//                        ? new TableColumn(table, SWT.RIGHT | SWT.BORDER)
//                        : new TableColumn(table, SWT.CENTER | SWT.BORDER);
                column = new TableColumn(table, SWT.CENTER | SWT.BORDER);
                
                if (Geometry.class.isAssignableFrom(aType.getType().getBinding())) { // was aType.isGeometry()
                    // jg: wot is this maddness? jd: paul said so
                    column.setText("GEOMETRY"); //$NON-NLS-1$
                    //layout.addColumnData(new ColumnWeightData(2, 50, true));
                } else {
                    // _p3: the capitalize does not work since some other, wonderful code seem to depend
                    // on the column text for property name :(
                    column.setText( aType.getName().getLocalPart() ); //StringUtils.capitalize(aType.getName().getLocalPart()));
                }

                // _p3: column width depending on column data type
                if (Boolean.class.isAssignableFrom( aType.getType().getBinding() )) {
                    layout.addColumnData(new ColumnWeightData(2, 50, true));
                }
                else if (Number.class.isAssignableFrom( aType.getType().getBinding() )) {
                    layout.addColumnData(new ColumnWeightData(3, 80, true));
                }
                else if (Date.class.isAssignableFrom( aType.getType().getBinding() )) {
                    layout.addColumnData(new ColumnWeightData(5, 80, true));
                }
                else {
                    layout.addColumnData(new ColumnWeightData(10, 120, true));
                }
                column.setMoveable(true);

                column.addListener(SWT.Selection, new AttributeColumnSortListener(this,
                        aType.getName().getLocalPart()));
            }
            
        }
    }

    /**
     * Does nothing.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // do nothing.
    }

    /**
     * Contents of the current page of features
     * 
     * @return
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature>  getFeatures() {
        return features;
    }

    /** Set up for a single page of content */
    public void setFeatures( FeatureCollection<SimpleFeatureType, SimpleFeature> features ) {
        checkWidget();
        if (this.features != null && this.features == features)
            return;

        this.features = features;
        
        createTableViewer(book);
    }

    private void checkWidget() {
        if (Display.getCurrent() == null)
            SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
    }

    /**
     * Displays a message.  If text == null or "" then the message is hidden and tableViewer is shown again.
     *
     * @param text message to display
     * @param background color of the background of the text widget.  If null the default color is used
     * @param foreground color of the foreground of the text widget.  If null the default color is used
     */
    public void message( String text, Color background, Color foreground ) {
        checkWidget();
        Color background2=background;
        Color foreground2=foreground;
        if( background2==null  ){
            background2=messageBackground;
        }
        if( foreground2==null  ){
            foreground2=messageForeground;
        }
        message.setBackground(background2);
        message.setForeground(foreground2);
        if( text==null || text.trim().length()==0 ){
            message.setText(""); //$NON-NLS-1$
            if( tableViewer!=null ){
                book.showPage(tableViewer.getControl());
                UiPlugin.trace(Trace.FEATURE_TABLE, getClass(), "message(String,Color,Color): showing table View", SHOW_PATH?new Exception():null); //$NON-NLS-1$
            }
        }else{
            message.setText(text);
            book.showPage(message);
            UiPlugin.trace(Trace.FEATURE_TABLE, getClass(), "message(String,Color,Color): showing message", SHOW_PATH?new Exception():null); //$NON-NLS-1$
        }
    }


    /**
     * Displays a message.  If text == null or "" then the message is hidden and tableViewer is shown again.
     *
     * @param text message to display
     */
    public void message( String text ) {
        message(text, null, null );
    }

    
    /**
     * Returns a selection with a single Id indicating the features selected
     */
    public ISelection getSelection() {
        checkWidget();
        return selectionProvider != null ? selectionProvider.getSelection() : null;
    }

    /**
     * Returns a selection with a single Id indicating the features selected
     */
    public Collection<String> getSelectionFids() {
        checkWidget();
        ISelection sel = selectionProvider.getSelection();
        List result = new ArrayList( ((IStructuredSelection)sel).size() );
        for (Object obj : ((IStructuredSelection)sel).toArray()) {
            result.add( ((Feature)obj).getIdentifier().getID() );
        }
        return result;
        //return selectionProvider.getSelectionFids();
    }

    
    public void addSelectionChangedListener( ISelectionChangedListener listener ) {
        selectionListeners.add( listener );
//        selectionProvider.addSelectionChangedListener(listener);
    }
    public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
        selectionListeners.remove( listener );
//        selectionProvider.removeSelectionChangedListener(listener);
    }

    /**
     * Useable selections are:
     * selection of features, FIDS and Filters/Queries that adapt to a FeatureSource
     */
    public void setSelection( final ISelection newSelection ) {
        checkWidget();
        selectionProvider.setSelection(newSelection);
    }

    /**
     * Sorts the features in the tableView.
     *
     * @param comparator comparator to use for the sorting.
     * @param dir the direction to set the column SWT.UP or SWT.DOWN.  
     * If SWT.UP then the table item with index 0 is at the top of the table otherwise 
     * it is at the bottom of the table.
     * @param sortColumn the column that is being sorted
     */
    public void sort( Comparator<SimpleFeature> comparator, int dir, TableColumn sortColumn ) {
        checkWidget();
        
        FeatureTableContentProvider provider=(FeatureTableContentProvider) tableViewer.getContentProvider();
        
        boolean sorted=false;
        if( !comparator.equals(currentComparator) ){
            sorted=true;
            currentComparator=comparator;
            Collections.sort(provider.features, currentComparator);
        }
        Table table = tableViewer.getTable();
        if( table.getSortColumn()!=sortColumn){
            sorted=true;
            table.setSortColumn(sortColumn);
            while( Display.getCurrent().readAndDispatch() );
        }
        if( table.getSortColumn()!=null && dir!=table.getSortDirection() ){
            sorted=true;
            table.setSortDirection(dir);
            while( Display.getCurrent().readAndDispatch() );
        }
        if(sorted){
            table.deselectAll();
            table.clearAll();
        }
        
    }

    /**
     * Resorts the table using the last comparator.  This is useful for cases where features have been added to the table
     * @param refreshTable 
     */
    void sort(boolean refreshTable ){
        if( currentComparator==null )
            return;
        
        FeatureTableContentProvider provider=(FeatureTableContentProvider) tableViewer.getContentProvider();
        
        Collections.sort(provider.features, currentComparator);

        tableViewer.getTable().deselectAll();
        if( refreshTable )
        tableViewer.getTable().clearAll();
    }
    
    public TableViewer getViewer() {
        return tableViewer;
    }

//    FeatureTableSelectionProvider getSelectionProvider() {
//        return selectionProvider;
//    }

    public void setSelection( StructuredSelection selection, boolean reveal ) {
        selectionProvider.setSelection(selection/*, reveal*/);
    }

    private Pattern compilePattern( final String text ) {
        
        String[] parts = text.split("\\|");
        
        StringBuilder builder = new StringBuilder();
        
        for( String string : parts ) {
            String pre = ".*";
            String post = ".*";
            if( string.startsWith("^") || string.startsWith(".") || string.startsWith("\\A") ){
                pre="";
            }
            if( string.startsWith("&") || string.startsWith("\\Z") || string.startsWith("\\z") ){
                post="";
            }
            builder.append(pre);
            builder.append(string);
            builder.append(post);
            builder.append('|');
        }
        if( builder.length()>0 ){
        	builder.deleteCharAt(builder.length()-1);
        }
        Pattern pattern;
        try{
            pattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
        }catch (IllegalArgumentException e) { 
            try{
                pattern=Pattern.compile(".*"+convertToLiteral(text)+".*");  //$NON-NLS-1$//$NON-NLS-2$
            }catch (IllegalArgumentException e2) { 
                return null ;
            }
        }
        return pattern;
    }


    private String convertToLiteral( String text ) {
        String text2=text.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("*", "\\*"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("+", "\\+"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace(".", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("?", "\\?"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("[", "\\["); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("]", "\\]"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("^", "\\^"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("-", "\\-"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("&", "\\&"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("(", "\\("); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace(")", "\\)"); //$NON-NLS-1$ //$NON-NLS-2$
        text2=text2.replace("|", "\\|"); //$NON-NLS-1$ //$NON-NLS-2$
        return text2;
    }

    private boolean matches( Pattern pattern, Object attribute ) {
    	if( attribute==null){
    		attribute = "";
    	}
        String stringValue = attribute.toString();
        return pattern.matcher(stringValue).matches();
    }
    
    public void addLoadingListener(IFeatureTableLoadingListener listener){
        loadingListeners.add(listener);
    }
    
    public void remove(IFeatureTableLoadingListener listener) {
        loadingListeners.remove(listener);
    }
    
    protected void notifyLoadingListeners(LoadingEvent event) {
        this.checkWidget();
        if( event.loading ){
            if( event.monitor==null )
                throw new NullPointerException();
            for( IFeatureTableLoadingListener listener : loadingListeners ) {
                try{
                    listener.loadingStarted(event.monitor);
                }catch (Throwable e) {
                    UiPlugin.log(listener+" threw an exception", e); //$NON-NLS-1$
                }
            }
        }else{
            for( IFeatureTableLoadingListener listener : loadingListeners ) {
                try{
                    listener.loadingStopped(event.canceled);
                }catch (Throwable e) {
                    UiPlugin.log(listener+" threw an exception", e); //$NON-NLS-1$
                }
            }
        }
    }

    public boolean addDClickListener( IDoubleClickListener listener ) {
        return dclickListeners.add( listener );    
    }
    
    public boolean removeDClickListener( IDoubleClickListener listener ) {
        return dclickListeners.remove( listener );    
    }
    
    protected void notifyDClickListeners(DoubleClickEvent ev) {
        checkWidget();
        for (IDoubleClickListener listener : dclickListeners) {
            try {
                listener.doubleClick( ev );
            }
            catch (Throwable e) {
                UiPlugin.log(listener+" threw an exception", e); //$NON-NLS-1$
            }
        }
    }
    
    /**
     * Updates the features that have the same feature ID to match the new feature or adds the features if they are not part of the
     * current collection.  
     *
     * @param features2 the feature collection that contains the modified or new features.  
     */
    public void update( FeatureCollection<SimpleFeatureType, SimpleFeature> features2 ) {
        if( features==null )
            return; // nothing to update since the table is not in use... Should this be an exception?
        FeatureTableContentProvider provider=(FeatureTableContentProvider) tableViewer.getContentProvider();
        provider.update(features2);
    }

    /**
     * Checks all the lists, caches, content providers, etc... are consistent with each other. 
     * This is an expensive method so should be called with care.  A test is a good example.
     */
    public void assertInternallyConsistent() {
        if( tableViewer.getContentProvider() != null ){
            FeatureTableContentProvider provider=(FeatureTableContentProvider) tableViewer.getContentProvider();
            provider.assertInternallyConsistent();
        }
    }

//    /**
//     * Removes the selected features (the features selected by the owning {@link FeatureTableControl}).
//     * @return returns a collection of the deleted features
//     * 
//     * @see #setSelection(ISelection)
//     * @see #setSelection(StructuredSelection, boolean)
//     */
//    public FeatureCollection<SimpleFeatureType, SimpleFeature> deleteSelection() {
//        FeatureTableContentProvider provider=(FeatureTableContentProvider) tableViewer.getContentProvider();
//        return provider.deleteSelection();
//    }

    /**
     * Sets the context Menu used by the table view.  Not menu is used for the message box.
     *
     * @param contextMenu menu manager used for creating the menu.
     */
    public void setMenuManager( MenuManager contextMenu ) {
        checkWidget();
        this.contextMenu=contextMenu;
        if( tableViewer!=null && tableViewer.getControl()!=null ){
            Menu oldMenu = tableViewer.getControl().getMenu();
            if( oldMenu!=null )
                oldMenu.dispose();
            Menu menu = contextMenu.createContextMenu(tableViewer.getControl());
            tableViewer.getControl().setMenu(menu);
        }
    }

    public void setSelectionColor( IProvider<RGB> selectionColor ) {
        this.selectionColor = selectionColor;
        if( tableViewer!=null ){
            FeatureTableLabelProvider labelProvider = (FeatureTableLabelProvider) tableViewer
                    .getLabelProvider();
            labelProvider.setSelectionColor(selectionColor);
        }
    }

}