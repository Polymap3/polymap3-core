package org.polymap.core.workbench;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.GeoTools;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.Platform;

import org.polymap.core.CorePlugin;
import org.polymap.core.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>24.06.2009: created</li>
 * @version $Revision: $
 */
public class PolymapActionBarAdvisor
        extends ActionBarAdvisor {

    private static final Log    log = LogFactory.getLog( PolymapActionBarAdvisor.class );

    private IWebBrowser      browser;

    private IWorkbenchAction exitAction;

    private IWorkbenchAction newAction, new2Action;
    
    private IWorkbenchAction undoAction, redoAction;
    
    //private UndoRedoActionGroup undoRedoGroup;

    private IWorkbenchAction importAction;

    private IWorkbenchAction exportAction;

    private Action           aboutAction;

    private Action           polymapWebSiteAction;

    private MenuManager      showViewMenuMgr;

    private IWorkbenchAction preferencesAction;

    private Action           wizardAction;

    private Action           browserAction;

    public IWorkbenchAction  saveAction;

    private IWorkbenchAction saveAllAction;

    private Action           newEditorAction;

    private IWorkbenchAction closePerspectiveAction, resetPerspectiveAction;

    private IWorkbenchAction helpContentsAction, helpSearchAction;

    private static int       browserIndex;


    public PolymapActionBarAdvisor( final IActionBarConfigurer configurer ) {
        super( configurer );
    }


    protected void makeActions( final IWorkbenchWindow window ) {
        ImageDescriptor quitActionImage = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.rap.demo", "icons/ttt.gif" ); //$NON-NLS-1$ //$NON-NLS-2$
        ImageDescriptor helpActionImage = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.rap.demo", "icons/help.gif" ); //$NON-NLS-1$ //$NON-NLS-2$
        ImageDescriptor wizardActionImage = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.rap.demo", "icons/login.gif" ); //$NON-NLS-1$ //$NON-NLS-2$
        ImageDescriptor browserActionImage = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.rap.demo", "icons/internal_browser.gif" ); //$NON-NLS-1$ //$NON-NLS-2$
        ImageDescriptor rapWebSiteActionImage = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.rap.demo", "icons/browser.gif" ); //$NON-NLS-1$ //$NON-NLS-2$
        exitAction = ActionFactory.QUIT.create( window );
        exitAction.setImageDescriptor( quitActionImage );
        register( exitAction );

        newAction = ActionFactory.NEW.create( window );
        newAction.setText( Messages.get( "PolymapActionBarAdvisor_new" ) );
        register( newAction );

        new2Action = ActionFactory.NEW_WIZARD_DROP_DOWN.create( window );
        new2Action.setText( Messages.get( "PolymapActionBarAdvisor_newDropDown" ) );
        register( new2Action );

        importAction = ActionFactory.IMPORT.create( window );
        register( importAction );

        exportAction = ActionFactory.EXPORT.create( window );
        register( exportAction );

        saveAction = ActionFactory.SAVE.create( window );
        register( saveAction );

        saveAllAction = ActionFactory.SAVE_ALL.create( window );
        register( saveAllAction );

        preferencesAction = ActionFactory.PREFERENCES.create( window );
        register( preferencesAction );

        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create( window );
        register( resetPerspectiveAction );

        closePerspectiveAction = ActionFactory.CLOSE_PERSPECTIVE.create( window );
        register( closePerspectiveAction );

        undoAction = ActionFactory.UNDO.create( window );
        register( undoAction );

        redoAction = ActionFactory.REDO.create( window );
        register( undoAction );
        
        helpContentsAction = ActionFactory.HELP_CONTENTS.create( window );
        helpContentsAction.setText( Messages.get( "PolymapActionBarAdvisor_helpContents" ) );
        register( helpContentsAction );
        
        helpSearchAction = ActionFactory.HELP_SEARCH.create( window );
        helpSearchAction.setText( Messages.get( "PolymapActionBarAdvisor_helpSearch" ) );
        register( helpSearchAction );
        
//        IWorkbench workbench = window.getWorkbench();
//        IOperationHistory operationHistory = workbench.getOperationSupport().getOperationHistory();
//        IUndoContext undoContext = workbench.getOperationSupport().getUndoContext();
//        undoRedoGroup = new UndoRedoActionGroup( window.getActivePage().get, undoContext, true );

//        introAction = ActionFactory.INTRO.create( window );
//        register( introAction );

        newEditorAction = new Action() {
            public void run() {
                log.info( "action is commented out!" ); //$NON-NLS-1$
//                try {
//                    window.getActivePage().openEditor(
//                            new FooEditorInput( PolymapActionBarAdvisor.this ),
//                            "org.eclipse.rap.demo.editor", true );
//                }
//                catch (PartInitException e) {
//                    e.printStackTrace();
//                }
            }
        };
        newEditorAction.setText( "Open new editor" ); //$NON-NLS-1$
        newEditorAction.setId( "org.eclipse.rap.demo.neweditor" ); //$NON-NLS-1$
        newEditorAction.setImageDescriptor( window.getWorkbench().getSharedImages()
                .getImageDescriptor( ISharedImages.IMG_TOOL_NEW_WIZARD ) );
        register( newEditorAction );

        aboutAction = new Action() {
            public void run() {
                Shell shell = window.getShell();
                
                Bundle rapBundle = Platform.getBundle( PlatformUI.PLUGIN_ID );
                Object rapVersion = rapBundle.getHeaders().get( Constants.BUNDLE_VERSION );
                
                Bundle coreBundle = Platform.getBundle( CorePlugin.PLUGIN_ID );
                Object coreVersion = coreBundle.getHeaders().get( Constants.BUNDLE_VERSION );
                
                Bundle anta2Bundle = Platform.getBundle( "org.polymap.anta2" );
                Object anta2Version = anta2Bundle.getHeaders().get( Constants.BUNDLE_VERSION );
                
                MessageDialog.openInformation( shell, Messages.get( "PolymapActionBarAdvisor_about" ),
                        "Vanko " + anta2Version + " -- www.polymap.org/vanko/\n" +
                        "POLYMAP " + coreVersion + " -- www.polymap.org/polymap3/\n" +
                        "Eclipse/RAP version " + rapVersion + "\n" +
                        "GeoTools version " + GeoTools.getVersion() );
            }
        };
        aboutAction.setText( Messages.get( "PolymapActionBarAdvisor_about" ) ); //$NON-NLS-1$
        aboutAction.setId( "org.eclipse.rap.demo.about" ); //$NON-NLS-1$
        aboutAction.setImageDescriptor( helpActionImage );
        register( aboutAction );
        
        polymapWebSiteAction = new Action() {
            public void run() {
                IWorkbenchBrowserSupport browserSupport;
                browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
                try {
                    int style = IWorkbenchBrowserSupport.AS_EXTERNAL;
                    browser = browserSupport
                            .createBrowser( style, polymapWebSiteAction.getId(), "", "" ); //$NON-NLS-1$ //$NON-NLS-2$
                    browser.openURL( new URL( "http://www.polymap.org" ) ); //$NON-NLS-1$
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        polymapWebSiteAction.setText( "POLYMAP Home Page" ); //$NON-NLS-1$
        polymapWebSiteAction.setId( "polymap3.core.rapWebSite" ); //$NON-NLS-1$
        polymapWebSiteAction.setImageDescriptor( rapWebSiteActionImage );
        register( polymapWebSiteAction );
        
//        showViewMenuMgr = new MenuManager( "Show View", "showView" );
//        IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create( window );
//        showViewMenuMgr.add( showViewMenu );
        
//        wizardAction = new Action() {
//            public void run() {
//                log.info( "action is commented out!" );
////                SurveyWizard wizard = new SurveyWizard();
////                WizardDialog dlg = new WizardDialog( window.getShell(), wizard );
////                dlg.open();
//            }
//        };
//        wizardAction.setText( "Open wizard" );
//        wizardAction.setId( "org.eclipse.rap.demo.wizard" );
//        wizardAction.setImageDescriptor( wizardActionImage );
//        register( wizardAction );

//        browserAction = new Action() {
//            public void run() {
//                browserIndex++;
//                try {
//                    window.getActivePage().showView( "org.eclipse.rap.demo.DemoBrowserViewPart",
//                            String.valueOf( browserIndex ), IWorkbenchPage.VIEW_ACTIVATE );
//                }
//                catch (PartInitException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        browserAction.setText( "Open new Browser View" );
//        browserAction.setId( "org.eclipse.rap.demo.browser" );
//        browserAction.setImageDescriptor( browserActionImage );
//        register( browserAction );
    }


    protected void fillMenuBar( final IMenuManager menuBar ) {
        // file menu
        IMenuManager fileMenu = menuBar.findMenuUsingPath( IWorkbenchActionConstants.M_FILE );
        if (fileMenu == null) {
            fileMenu = new MenuManager( Messages.get( "PolymapActionBarAdvisor_file" ), //$NON-NLS-1$
                    IWorkbenchActionConstants.M_FILE );
            if (menuBar.getItems().length > 0) {
                menuBar.insertBefore( menuBar.getItems()[0].getId(), fileMenu );
            }
            else {
                menuBar.add( fileMenu );
            }
        }
        fileMenu.add( new2Action );
        fileMenu.add( importAction );
        fileMenu.add( exportAction );
//        fileMenu.add( exitAction );
        
        // edit
        IMenuManager editMenu = new MenuManager( Messages.get( "PolymapActionBarAdvisor_edit" ), //$NON-NLS-1$
                IWorkbenchActionConstants.M_EDIT );
//        editMenu.add( undoAction );
//        editMenu.add( redoAction );
        menuBar.add( editMenu );
        
        // window menu
        IMenuManager windowMenu = new MenuManager( Messages.get( "PolymapActionBarAdvisor_window" ), //$NON-NLS-1$
                IWorkbenchActionConstants.M_WINDOW );
        fillWindowMenu( windowMenu );
        menuBar.add( windowMenu );

        // help menu
        MenuManager helpMenu = new MenuManager( Messages.get( "PolymapActionBarAdvisor_help" ), IWorkbenchActionConstants.M_HELP ); //$NON-NLS-1$
        helpMenu.add( helpContentsAction );
        helpMenu.add( helpSearchAction );
        helpMenu.add( polymapWebSiteAction );
        helpMenu.add( new Separator( "about" ) ); //$NON-NLS-1$
        helpMenu.add( aboutAction );
        menuBar.add( fileMenu );
//        windowMenu.add( showViewMenuMgr );

        windowMenu.add( preferencesAction );
        menuBar.add( windowMenu );
        menuBar.add( helpMenu );
    }


    /**
     * Define the Window Menu according to RCP "custom".
     * <p>
     * The window menu is mostly concerned with the care and feeding of application wide
     * customisations and settings; from access to application preferences to opening up views and
     * switching perspectives.
     * <p>
     * window/wbStart window/... window/additions window/wbEnd
     * 
     * @param windowMenu
     */
    protected void fillWindowMenu( IMenuManager windowMenu ) {
        IWorkbenchWindow window = getActionBarConfigurer().getWindowConfigurer().getWindow();

        windowMenu.add( new GroupMarker( IWorkbenchActionConstants.WB_START ) );

        IAction openNewWindow = ActionFactory.OPEN_NEW_WINDOW.create( window );
        openNewWindow.setText( Messages.get( "PolymapActionBarAdvisor_newWindow" ) ); //$NON-NLS-1$
        windowMenu.add( openNewWindow );

        windowMenu.add( new Separator() );

        IMenuManager perspectiveMenu = new MenuManager(
                Messages.get().PolymapActionBarAdvisor_openPerspective,
                ContributionItemFactory.PERSPECTIVES_SHORTLIST.getId() );
        perspectiveMenu.add( ContributionItemFactory.PERSPECTIVES_SHORTLIST.create( window ) );
        windowMenu.add( perspectiveMenu );

        IMenuManager viewMenu = new MenuManager( Messages.get().PolymapActionBarAdvisor_showView,
                ContributionItemFactory.VIEWS_SHORTLIST.getId() );
        viewMenu.add( ContributionItemFactory.VIEWS_SHORTLIST.create( window ) );
        windowMenu.add( viewMenu );
        windowMenu.add( new Separator() );

        windowMenu.add( resetPerspectiveAction );
        windowMenu.add( closePerspectiveAction );

        IAction closeAllPerspectives = ActionFactory.CLOSE_ALL_PERSPECTIVES.create( window );
        closeAllPerspectives.setText( Messages.get().PolymapActionBarAdvisor_closeAllPerspective );
        windowMenu.add( closeAllPerspectives );

        IAction editActionSets = ActionFactory.EDIT_ACTION_SETS.create( window );
        windowMenu.add( editActionSets );

        windowMenu.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );

        windowMenu.add( new Separator() );

        IAction preferences = ActionFactory.PREFERENCES.create( window );
        preferences.setText( Messages.get( "PolymapActionBarAdvisor_preferences" ) ); //$NON-NLS-1$
        IContributionItem item = new ActionContributionItem( preferences );
        item.setVisible( !Platform.OS_MACOSX.equals( Platform.getOS() ) );

        windowMenu.add( item );

        // FIXME: compile problem
        // windowMenu.add(ContributionItemFactory.OPEN_WINDOWS.create(window));

        windowMenu.add( new GroupMarker( IWorkbenchActionConstants.WB_END ) );
    }
    

    protected void fillCoolBar( final ICoolBarManager coolBar ) {
        createToolBar( coolBar, "main" ); //$NON-NLS-1$
        createToolBar( coolBar, "editor" ); //$NON-NLS-1$
    }


    private void createToolBar( final ICoolBarManager coolBar, final String name ) {
        IToolBarManager toolbar = new ToolBarManager( SWT.FLAT | SWT.RIGHT );
        coolBar.add( new ToolBarContributionItem( toolbar, name ) );
        if (name != "editor") { //$NON-NLS-1$
//            toolbar.add( wizardAction );
//            toolbar.add( browserAction );

//            toolbar.add( aboutAction );
//            toolbar.add( exitAction );
        }
        else {
            toolbar.add( new2Action );
//            toolbar.add( newEditorAction );
//            toolbar.add( saveAction );
//            toolbar.add( undoAction );
//            toolbar.add( redoAction );
        }
    }


    protected void fillStatusLine( final IStatusLineManager statusLine ) {
        statusLine.add( aboutAction );
    }
}
