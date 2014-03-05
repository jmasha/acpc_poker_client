/*
 * SwordfishApp.java
 */

package swordfish.view;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class SwordfishApp extends SingleFrameApplication {
    private static SwordfishView view;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        //System.setProperty( "swing.aatext", "true" );
        view = new SwordfishView(this);
        show(view);
        view.newGame();
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SwordfishApp
     */
    public static SwordfishApp getApplication() {
        return Application.getInstance(SwordfishApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(SwordfishApp.class, args);
    }
}
