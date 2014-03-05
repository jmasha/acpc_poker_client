package swordfish.view;

import glassfrog.model.Gamedef;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import swordfish.model.Player;
import swordfish.model.Gamestate;
import swordfish.tools.URLTools;

/**
 * The Swordfish View class is the main frame of the application.  This is essentially
 * the GUI for the user to interact with the Glassfrog server for playing against 
 * bots that support the AAAI format.  Currently the GUI only works with a 2 player 
 * heads up reverse blinds limit or no limit texas holdem as there are hard coded 
 * constants in the @TablePanel that would have to be changed to support any other 
 * games.  As well, there should be some changes made to the way the parser handles 
 * @Gamestate updates to use @Player objects for a more comprehensible and easy
 * to scale solution
 */
public class SwordfishView extends FrameView {

    private List<String> consoleHistory;
    private int historyIndex;
    private String name = "Human",  logPath,  gamestateString, showdownString, key;
    private Socket roomConnection,  playerConnection;
    private BufferedReader br;
    private PrintWriter pw;
    private boolean gameOver;
    private NumberFormat betFormat;
    private Integer seatPref,  buyIn;
    private LinkedList<Player> players;
    private Gamedef gamedef;
    private Gamestate gamestate;
    private JDialog connectDialog;
    private JFrame tableFrame;
    private static final String ERROR_LOG = "errorLog.log";
    private static final String ERROR_LOGGER = "swordfish.errorlogger";
    private static final String MATCH_LOG = "matchLog.log";
    private static final String MATCH_LOGGER = "swordfish.matchlogger";
    private static final String GAMESTATE_LOG = "gamestateLog.log";
    private static final String GAMESTATE_LOGGER = "swordfish.gamestatelogger";
    private static final boolean onlineClient = true;

    /**
     * Main constructor for the GUI.  This sets up the title, some constants and
     * formatters, the Frame and an override for the windowClosing event
     * @param app The application handleing the call to this frame.
     */
    public SwordfishView(final SingleFrameApplication app) {
        super(app);
        app.getMainFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        tableFrame = this.getFrame();
        tableFrame.setTitle("Swordfish v2.1.0");
        tableFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        tableFrame.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                confirmClose();
            }
        });
        betFormat = NumberFormat.getNumberInstance();

        initComponents();
        initGameComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ApplicationContext ctxt = getContext();
        ResourceManager mgr = ctxt.getResourceManager();
        ResourceMap resourceMap = mgr.getResourceMap(SwordfishView.class);

        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                }
            }
        });
    }

    /**
     * Confirm that the user will forfeit the hand if they close the application
     * once they have acted in a hand
     */
    @Action
    public void confirmClose() {
        //default icon, custom title
        int n = JOptionPane.YES_OPTION;
        if (!gameOver && !gamestate.isSafeExit()) {
            n = JOptionPane.showConfirmDialog(tableFrame,
                    "Exiting will fold your current hand, do you wish to do so?",
                    "Exit?", JOptionPane.YES_NO_OPTION);
        }
        if (n == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Show the about box to the user
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SwordfishApp.getApplication().getMainFrame();
            aboutBox = new SwordfishAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SwordfishApp.getApplication().show(aboutBox);
    }

    /**
     * Show the about box, formatted for the help.html
     */
    @Action
    public void showHelp() {
        showAboutBox();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        gamePanel = new javax.swing.JPanel();
        tablePanel = new swordfish.view.TablePanel();
        buttonPanel = new javax.swing.JPanel();
        blankPanel0 = new javax.swing.JPanel();
        foldButton = new javax.swing.JButton();
        blankPanel1 = new javax.swing.JPanel();
        callButton = new javax.swing.JButton();
        blankPanel2 = new javax.swing.JPanel();
        raiseButton = new javax.swing.JButton();
        blankPanel3 = new javax.swing.JPanel();
        betsizeSlider = new swordfish.view.NoLimitSlider();
        chatPanel = new javax.swing.JPanel();
        console = new javax.swing.JTextField();
        consoleScrollPane = new javax.swing.JScrollPane();
        log = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        connectMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        helpMenuItem = new javax.swing.JMenuItem();
        settingsMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();

        mainPanel.setMinimumSize(new java.awt.Dimension(800, 600));
        mainPanel.setName("mainPanel"); // NOI18N

        gamePanel.setName("gamePanel"); // NOI18N

        tablePanel.setName("tablePanel"); // NOI18N

        buttonPanel.setMinimumSize(new java.awt.Dimension(710, 40));
        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new java.awt.GridBagLayout());

        blankPanel0.setMinimumSize(new java.awt.Dimension(20, 35));
        blankPanel0.setName("blankPanel0"); // NOI18N
        blankPanel0.setOpaque(false);
        blankPanel0.setPreferredSize(new java.awt.Dimension(240, 40));

        org.jdesktop.layout.GroupLayout blankPanel0Layout = new org.jdesktop.layout.GroupLayout(blankPanel0);
        blankPanel0.setLayout(blankPanel0Layout);
        blankPanel0Layout.setHorizontalGroup(
            blankPanel0Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 240, Short.MAX_VALUE)
        );
        blankPanel0Layout.setVerticalGroup(
            blankPanel0Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );

        buttonPanel.add(blankPanel0, new java.awt.GridBagConstraints());

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(swordfish.view.SwordfishApp.class).getContext().getActionMap(SwordfishView.class, this);
        foldButton.setAction(actionMap.get("fold")); // NOI18N
        foldButton.setMaximumSize(new java.awt.Dimension(80, 25));
        foldButton.setMinimumSize(new java.awt.Dimension(80, 25));
        foldButton.setName("foldButton"); // NOI18N
        foldButton.setPreferredSize(new java.awt.Dimension(100, 25));
        buttonPanel.add(foldButton, new java.awt.GridBagConstraints());

        blankPanel1.setMinimumSize(new java.awt.Dimension(20, 35));
        blankPanel1.setName("blankPanel1"); // NOI18N
        blankPanel1.setOpaque(false);
        blankPanel1.setPreferredSize(new java.awt.Dimension(15, 40));

        org.jdesktop.layout.GroupLayout blankPanel1Layout = new org.jdesktop.layout.GroupLayout(blankPanel1);
        blankPanel1.setLayout(blankPanel1Layout);
        blankPanel1Layout.setHorizontalGroup(
            blankPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 15, Short.MAX_VALUE)
        );
        blankPanel1Layout.setVerticalGroup(
            blankPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );

        buttonPanel.add(blankPanel1, new java.awt.GridBagConstraints());

        callButton.setAction(actionMap.get("call")); // NOI18N
        callButton.setMaximumSize(new java.awt.Dimension(80, 25));
        callButton.setMinimumSize(new java.awt.Dimension(80, 25));
        callButton.setName("callButton"); // NOI18N
        callButton.setPreferredSize(new java.awt.Dimension(100, 25));
        buttonPanel.add(callButton, new java.awt.GridBagConstraints());

        blankPanel2.setMinimumSize(new java.awt.Dimension(20, 35));
        blankPanel2.setName("blankPanel2"); // NOI18N
        blankPanel2.setOpaque(false);
        blankPanel2.setPreferredSize(new java.awt.Dimension(15, 40));

        org.jdesktop.layout.GroupLayout blankPanel2Layout = new org.jdesktop.layout.GroupLayout(blankPanel2);
        blankPanel2.setLayout(blankPanel2Layout);
        blankPanel2Layout.setHorizontalGroup(
            blankPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 15, Short.MAX_VALUE)
        );
        blankPanel2Layout.setVerticalGroup(
            blankPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );

        buttonPanel.add(blankPanel2, new java.awt.GridBagConstraints());

        raiseButton.setAction(actionMap.get("raise")); // NOI18N
        raiseButton.setMaximumSize(new java.awt.Dimension(80, 25));
        raiseButton.setMinimumSize(new java.awt.Dimension(80, 25));
        raiseButton.setName("raiseButton"); // NOI18N
        raiseButton.setPreferredSize(new java.awt.Dimension(100, 25));
        buttonPanel.add(raiseButton, new java.awt.GridBagConstraints());

        blankPanel3.setMinimumSize(new java.awt.Dimension(10, 35));
        blankPanel3.setName("blankPanel3"); // NOI18N
        blankPanel3.setOpaque(false);
        blankPanel3.setPreferredSize(new java.awt.Dimension(35, 40));

        org.jdesktop.layout.GroupLayout blankPanel3Layout = new org.jdesktop.layout.GroupLayout(blankPanel3);
        blankPanel3.setLayout(blankPanel3Layout);
        blankPanel3Layout.setHorizontalGroup(
            blankPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 35, Short.MAX_VALUE)
        );
        blankPanel3Layout.setVerticalGroup(
            blankPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );

        buttonPanel.add(blankPanel3, new java.awt.GridBagConstraints());

        betsizeSlider.setName("betsizeSlider"); // NOI18N
        buttonPanel.add(betsizeSlider, new java.awt.GridBagConstraints());

        org.jdesktop.layout.GroupLayout tablePanelLayout = new org.jdesktop.layout.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE)
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tablePanelLayout.createSequentialGroup()
                .addContainerGap(430, Short.MAX_VALUE)
                .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        buttonPanel.getAccessibleContext().setAccessibleParent(gamePanel);

        chatPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        chatPanel.setName("chatPanel"); // NOI18N

        console.setMinimumSize(new java.awt.Dimension(800, 19));
        console.setName("console"); // NOI18N
        console.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consoleKeyPressed(evt);
            }
        });

        consoleScrollPane.setMinimumSize(new java.awt.Dimension(800, 22));
        consoleScrollPane.setName("consoleScrollPane"); // NOI18N

        log.setColumns(20);
        log.setEditable(false);
        log.setRows(5);
        log.setMinimumSize(new java.awt.Dimension(800, 15));
        log.setName("log"); // NOI18N
        consoleScrollPane.setViewportView(log);

        org.jdesktop.layout.GroupLayout chatPanelLayout = new org.jdesktop.layout.GroupLayout(chatPanel);
        chatPanel.setLayout(chatPanelLayout);
        chatPanelLayout.setHorizontalGroup(
            chatPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chatPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(chatPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, consoleScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 803, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, console, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 803, Short.MAX_VALUE))
                .addContainerGap())
        );
        chatPanelLayout.setVerticalGroup(
            chatPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chatPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(consoleScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(console, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout gamePanelLayout = new org.jdesktop.layout.GroupLayout(gamePanel);
        gamePanel.setLayout(gamePanelLayout);
        gamePanelLayout.setHorizontalGroup(
            gamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(chatPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(gamePanelLayout.createSequentialGroup()
                .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        gamePanelLayout.setVerticalGroup(
            gamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, gamePanelLayout.createSequentialGroup()
                .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chatPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gamePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gamePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        menuBar.setMinimumSize(new java.awt.Dimension(800, 2));
        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(swordfish.view.SwordfishApp.class).getContext().getResourceMap(SwordfishView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        connectMenuItem.setAction(actionMap.get("connect")); // NOI18N
        connectMenuItem.setText(resourceMap.getString("connectMenuItem.text")); // NOI18N
        connectMenuItem.setName("connectMenuItem"); // NOI18N
        fileMenu.add(connectMenuItem);

        exitMenuItem.setAction(actionMap.get("confirmClose")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        helpMenuItem.setAction(actionMap.get("showHelp")); // NOI18N
        helpMenuItem.setText(resourceMap.getString("helpMenuItem.text")); // NOI18N
        helpMenuItem.setName("helpMenuItem"); // NOI18N
        helpMenu.add(helpMenuItem);

        settingsMenuItem.setText(resourceMap.getString("settingsMenuItem.text")); // NOI18N
        settingsMenuItem.setName("settingsMenuItem"); // NOI18N
        helpMenu.add(settingsMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setMinimumSize(new java.awt.Dimension(800, 0));
        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 807, Short.MAX_VALUE)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel))
                .add(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

private void consoleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_consoleKeyPressed

    switch (evt.getKeyCode()) {
        case KeyEvent.VK_ENTER: {
            String consoleText = console.getText();
            if (!consoleText.equalsIgnoreCase("")) {
                if (consoleText.startsWith("::")) {
                    //executeCommand(consoleText.substring(2));
                    console.setText("");
                } else {
                    writeToLog(consoleText);
                    console.setText("");
                }
            }
            addToHistory(consoleText);
            historyIndex = consoleHistory.size();
            break;
        }
        case KeyEvent.VK_UP: {
            String consoleText = console.getText();
            historyIndex--;
            if (historyIndex >= consoleHistory.size()) {
                historyIndex = consoleHistory.size() - 1;
            }
            if (historyIndex < 0) {
                historyIndex = 0;
            }
            if (consoleHistory.isEmpty()) {
                console.setText(consoleText);
            } else {
                console.setText(consoleHistory.get(historyIndex).toString());
            }
            break;
        }
        case KeyEvent.VK_DOWN: {
            String consoleText = console.getText();
            historyIndex++;
            if (historyIndex >= consoleHistory.size()) {
                historyIndex = consoleHistory.size() - 1;
            }
            if (historyIndex < 0) {
                historyIndex = 0;
            }
            if (consoleHistory.isEmpty()) {
                console.setText(consoleText);
            } else {
                console.setText(consoleHistory.get(historyIndex).toString());
            }
            break;
        }
    }
}//GEN-LAST:event_consoleKeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private swordfish.view.NoLimitSlider betsizeSlider;
    private javax.swing.JPanel blankPanel0;
    private javax.swing.JPanel blankPanel1;
    private javax.swing.JPanel blankPanel2;
    private javax.swing.JPanel blankPanel3;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton callButton;
    private javax.swing.JPanel chatPanel;
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JTextField console;
    private javax.swing.JScrollPane consoleScrollPane;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton foldButton;
    private javax.swing.JPanel gamePanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JTextArea log;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton raiseButton;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private swordfish.view.TablePanel tablePanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;  //Disabled for online play settingsDialog,  connectDialog;

    /**
     * Initialize the game specific components, such as the players list, the
     * console history, set the gamedef and gamestate to null and the player specific 
     * options such as buyin, and seat preference
     */
    private void initGameComponents() {
        consoleHistory = new LinkedList<String>();
        players = new LinkedList<Player>();
        buyIn = 200;
        seatPref = 1;
        gamedef = null;
        gamestate = null;
        historyIndex = 0;

        enableButtons(false);

        /* Disabled for online play  */
        if (!onlineClient) {
            logPath = "logs/";
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers.length > 0) {
                if (handlers[0] instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handlers[0]);
                }
            }
            initLogs();
            loadSettings();
        } else {
            helpMenu.remove(settingsMenuItem);
            helpMenu.remove(aboutMenuItem);
            fileMenu.remove(connectMenuItem);
            helpMenu.validate();
            fileMenu.validate();
        }
    }

    /**
     * Connect to the server specified by the ip and port given in the config file
     * and the key passed in by the dialog.  The IP and PORT are hardcoded right now
     * to point to the poker server set up at the U of A
     * @param ip An IP to connect to
     * @param port a PORT on the IP that the server lives
     * @param key A Key to check for a valid connection
     */
    public void connectToServer(String ip, int port, String key) {
        try {
            Socket serverConnection = new Socket(ip, port);
            BufferedReader serverBR = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
            PrintWriter serverPW = new PrintWriter(serverConnection.getOutputStream(), true);
            serverPW.println("AUTOCONNECT:" + key);
            serverPW.flush();
            String response = serverBR.readLine();
            StringTokenizer st = new StringTokenizer(response, ":");
            String status = st.nextToken();
            if (status.equalsIgnoreCase("ERROR")) {
                JOptionPane.showMessageDialog(this.getFrame(), st.nextToken(), "Server Error", JOptionPane.ERROR_MESSAGE);
                ((keyValidationDialog) connectDialog).reset();
                connectDialog.setVisible(true);
            } else if (status.equalsIgnoreCase("MAINTENANCE")) {
                JOptionPane.showMessageDialog(this.getFrame(), st.nextToken(), "Server Maintenance", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            } else {
                connectDialog.setVisible(false);
                name = st.nextToken();
                this.key = key;
                response = serverBR.readLine();
                st = new StringTokenizer(response, ":");
                st.nextToken();
                int roomPort = new Integer(st.nextToken()).intValue();
                connectToRoom(ip, roomPort);
            }
        } catch (UnknownHostException ex) {
            logError(ex.toString());
            connectDialog.setVisible(false);
            JOptionPane.showMessageDialog(this.getFrame(), "Server Unavailable.\n " +
                    "Please try again later", "Server Unavailable", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (IOException ex) {
            logError(ex.toString());
            connectDialog.setVisible(false);
            JOptionPane.showMessageDialog(this.getFrame(), "Server Unavailable.\n " +
                    "Please try again later", "Server Unavailable", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (NumberFormatException ex) {
            logError(ex.toString());
        }



    }

    /**
     * Connect to the Room.  Once the server has sent the IP and PORT of the room
     * to which the GUI is supposed to connect, the GUI will auto connect to the
     * room and send the room the name, buyin and seat preference. The Room in turn
     * will send back a @Gamedef for the game as well as some @Player info
     * @param ip the IP of the room to connect to.
     * @param port the PORT of the room to connect to.
     */
    @SuppressWarnings("empty-statement")
    public void connectToRoom(String ip, Integer port) {
        try {
            roomConnection = new Socket(ip, port);
            BufferedReader roomBR = new BufferedReader(new InputStreamReader(roomConnection.getInputStream()));
            PrintWriter roomPW = new PrintWriter(roomConnection.getOutputStream(), true);
            roomPW.println("GUIPlayer:" + name + ":" + buyIn + ":" + seatPref);
            roomPW.flush();
            ObjectInputStream roomOIS = new ObjectInputStream(roomConnection.getInputStream());
            try {
                gamedef = (Gamedef) roomOIS.readObject();
                if (!gamedef.isNoLimit()) {
                    betsizeSlider.invisible();
                    tablePanel.setHideStack(true);
                } else {
                    betsizeSlider.getSlider().addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent arg0) {
                            String raiseText = (gamestate.getRaiseFlag() ? "Raise " : "Bet ");
                            raiseButton.setText(raiseText + betsizeSlider.getValue());
                        }
                    });
                    betsizeSlider.initSlider(gamedef.getMinBet(), gamedef.getStackSize());
                    betsizeSlider.addButtonActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if ("potsize".equals(e.getActionCommand())) {
                                betsizeSlider.getSlider().setValue(potSizeBet());
                            } else if ("halfpotsize".equals(e.getActionCommand())) {
                                betsizeSlider.getSlider().setValue(Math.round(potSizeBet() / 2));
                            } else if ("allin".equals(e.getActionCommand())) {
                                betsizeSlider.getSlider().setValue(gamedef.getStackSize());
                            }
                        }
                    });
                }
                gamestate = new Gamestate(gamedef);
            } catch (ClassNotFoundException ex) {
                System.out.println(ex.toString());
            }
            String connectionString = roomBR.readLine();
            System.out.println(connectionString);
            StringTokenizer st = new StringTokenizer(connectionString, ":");
            if(st.nextToken().equalsIgnoreCase("ERROR")) {
                String errorMessage = st.nextToken();
                JOptionPane.showMessageDialog(this.getFrame(), errorMessage, "Server Unavailable", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            };
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.toString());
            }
            int playerPort = new Integer(st.nextToken()).intValue();
            playerConnection = new Socket(ip, playerPort);
            br = new BufferedReader(new InputStreamReader(playerConnection.getInputStream()));
            pw = new PrintWriter(playerConnection.getOutputStream(), true);
            pw.println("Version:1.0.0");
            pw.flush();
            startGame();
        } catch (UnknownHostException ex) {
            logError(ex.toString());
            JOptionPane.showMessageDialog(this.getFrame(), "Server Unavailable.\n" +
                    "Please try again later", "Server Unavailable", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (IOException ex) {
            logError(ex.toString());
            JOptionPane.showMessageDialog(this.getFrame(), "Server Unavailable.\n" +
                    "Please try again later", "Server Unavailable", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    /**
     * Initialize the client side logging.  This is disabled at the moment to 
     * ensure that there are no security warning with the webstart client
     */
    private void initLogs() {
        Date d = new Date();
        String timeStamp = d.toString();
        try {
            FileHandler errorFileHandler = new FileHandler(logPath + timeStamp + ":" + ERROR_LOG, true);
            errorFileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger(ERROR_LOGGER).addHandler(errorFileHandler);
            FileHandler matchFileHandler = new FileHandler(logPath + timeStamp + ":" + MATCH_LOG, true);
            matchFileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger(MATCH_LOGGER).addHandler(matchFileHandler);
            FileHandler gamestateFileHandler = new FileHandler(logPath + timeStamp + ":" + GAMESTATE_LOG, true);
            gamestateFileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger(GAMESTATE_LOGGER).addHandler(gamestateFileHandler);
        } catch (IOException ex) {
            logError(ex.toString());
        } catch (SecurityException ex) {
            logError(ex.toString());
        }
    }

    /**
     * Log an error to the error logger.  Currently the error logger is disabled
     * so stderr is used instead
     * @param errorMessage the error message
     */
    public void logError(String errorMessage) {
        System.err.println(errorMessage);
    // Logger.getLogger(ERROR_LOGGER).log(Level.SEVERE, errorMessage);
    }

    /**
     * Log a warning to the error logger. Currently the error logger is disabled
     * so stderr is used instead
     * @param warningMessage the warning message
     */
    public void logWarning(String warningMessage) {
        System.err.println(warningMessage);
    // Logger.getLogger(ERROR_LOGGER).log(Level.WARNING, warningMessage);
    }

    /**
     * Send a raise to the server.  This check the raise value in NoLimit games
     * to ensure that a raise is posible and will warn the user that the betsize
     * is invalid upon failure.  In Limit, this will simply send a raise to the 
     * server
     */
    @Action
    public void raise() {
        if (gamedef.isNoLimit()) {
            int betSize = betsizeSlider.getValue() + gamestate.getCurrentBet();
            if (betSize == gamestate.getCallAmount(gamestate.getPlayerPosition())) {
                call();
                return;
            }
            if (checkValidBetSize(betSize)) {
                if (betSize > gamedef.getStackSize()) {
                    betSize = gamedef.getStackSize();
                }
                pw.println(gamestateString + ":r" + betSize);
            } else {
                int minValidBet = (gamestate.getLastRaise() > gamedef.getMinBet() ? gamestate.getLastRaise() : gamedef.getMinBet());
                String raiseText = (gamestate.getRaiseFlag() ? "Raise" : "Bet");
                JOptionPane.showMessageDialog(this.getFrame(), "Invalid Bet Size, "+
                        raiseText+" must be at least " + minValidBet, "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            pw.println(gamestateString + ":r");
            pw.flush();
        }
        update();
    }

    /**
     * Send the call action to the server
     */
    @Action
    public void call() {
        pw.println(gamestateString + ":c");
        update();
    }

    /**
     * Send the fold action to the server
     */
    @Action
    public void fold() {
        String action = (gamestate.getRaiseFlag() ? ":f" : ":c");
        pw.println(gamestateString + action);
        update();
    }

    /**
     * Set the bet to potSize and make the potSize bet
     * @return
     */
    private int potSizeBet() {
        int amountToCall = gamestate.getCallAmount(gamestate.getPlayerPosition());
        return amountToCall + gamestate.getPotSize();
    }

    /**
     * Check to see if the bet is of a valid size and is allowed.
     * @param bet The size of the bet to check
     * @return True for valid bets, False otherwise
     */
    private boolean checkValidBetSize(Integer bet) {
        if (bet >= gamestate.getStackSize(gamestate.getPlayerPosition())) {
            //Allin always valid
            return true;
        } else if (bet < gamedef.getMinBet()) {
            //The bet is less than the min bet
            return false;
        } else if (bet > gamedef.getMaxBet() && !gamedef.isNoLimit()) {
            //The bet is greater than the max bet
            return false;
        } else if (bet < (gamestate.getLastRaise() + gamestate.getCurrentBet()) && gamedef.isNoLimit()) {
            //The raise is less than the size of the last raise
            return false;
        }
        return true;
    }

    /**
     * Load the settings from the settings.xml file.  This is disabled for online play
     * @return True for a successfull load, False otherwise
     */
    private boolean loadSettings() {
        boolean isLoaded = false;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = (Document) docBuilder.parse(new File("settings/settings.xml"));
            doc.getDocumentElement().normalize();

            NodeList settingsList = doc.getElementsByTagName("PlayerSettings");
            Node settingNode = settingsList.item(0);
            if (settingNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) settingNode;
                name = element.getElementsByTagName("PlayerName").item(0).getChildNodes().item(0).getNodeValue().trim();
                seatPref = new Integer(element.getElementsByTagName("PlayerSeat").item(0).getChildNodes().item(0).getNodeValue().trim());
                logPath = element.getElementsByTagName("LogPath").item(0).getChildNodes().item(0).getNodeValue().trim();
                isLoaded = true;
            }
        } catch (ParserConfigurationException ex) {
            logError(ex.toString());
        } catch (IOException ex) {
            logError(ex.toString());
        } catch (SAXParseException ex) {
            logError("** Parsing error" + ", line " + ex.getLineNumber() + ", uri " + ex.getSystemId());
            logError(ex.toString());
        } catch (SAXException ex) {
            logError(ex.toString());
        }
        return isLoaded;
    }

    /**
     * Start a new game, Opening the Room Selection Dialog.  This is disabled for
     * online play
     */
    @Action
    public void newGame() {
        connect();
    }

    /**
     * Open the connection dialog.  Open the validation dialog for online play,
     * use the server Room Selection Dialog for standalone.
     */
    @Action
    public void connect() {
        /* Auto Connect for Online Client */
        if (connectDialog == null) {
            JFrame mainFrame = SwordfishApp.getApplication().getMainFrame();
            if (onlineClient) {
                connectDialog = new keyValidationDialog(mainFrame, this, true);
            } else {
                connectDialog = new SwordfishRoomDialog(mainFrame, this, true);
            }
            connectDialog.setLocationRelativeTo(mainFrame);
        }
        SwordfishApp.getApplication().show(connectDialog);
    }

    /**
     * Write a message to the log file.
     * @param logText The message to insert into the log
     */
    private void writeToLog(String logText) {
        log.insert(logText + '\n', 0);
    }

    /**
     * Add the text to the consoleHistory.
     * @param consoleText The text to be added to the console history
     */
    private void addToHistory(String consoleText) {
        consoleHistory.add(consoleText);
    }

    /**
     * Not Yet Implemented, Intention for execution of commands from within the
     * console
     * @param command The command to execture
     */
    private void executeCommand(String command) {
        writeToLog("Command " + command + " executed");
    }

    /**
     * Begin the game.  This sets the stackSize to the starting stack size,
     * the gameOver to false, and starts the GameLoop Thread
     */
    public void startGame() {
        gameOver = false;
        writeToLog("Game Started");
        GameLoop gl = new GameLoop();
        Thread glThread = new Thread(gl);
        glThread.start();
    }

    /**
     * Get the path to which the logs are currently written
     * @return A String representing a fully qualified path to the logsfiles
     */
    public String getLogPath() {
        return logPath;
    }

    /**
     * Set the log path for client side logging
     * @param logPath A String representing a fully qualified pathname to log to.
     */
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    /**
     * Return the current players name
     * @return a String representing the name of the person playing.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the current player
     * @param name a String representing the new name of the player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the current seat preference of the player
     * @return an int representing the seat preference of the player
     */
    public Integer getSeatPref() {
        return seatPref;
    }

    /**
     * Set the preferred seat for the player
     * @param seatPref an int representing the player's preferred seat
     */
    public void setSeatPref(Integer seatPref) {
        this.seatPref = seatPref;
    }

    /**
     * Enable or disable the buttons to avoid multi clicking
     * @param enabled True for enable, False for disabled
     */
    private void enableButtons(boolean enabled) {
        callButton.setEnabled(enabled);
        raiseButton.setEnabled(enabled);
        foldButton.setEnabled(enabled);
        betsizeSlider.enableSlider(enabled);
    }

    /**
     * Listen for a gamestate from the server and handle the gamestate
     * Special messages are sent with a prepending # and are handled differently
     * then gamestate messages
     */
    private void listenForGameState() {
        try {
            String serverMessage = "";
            serverMessage = br.readLine();
            if (serverMessage.startsWith("#")) {
                StringTokenizer messageTokenizer = new StringTokenizer(serverMessage, "||");
                String type = messageTokenizer.nextToken();
                if (type.equalsIgnoreCase("#PLAYERS")) {
                    while (messageTokenizer.hasMoreTokens()) {
                        StringTokenizer st = new StringTokenizer(messageTokenizer.nextToken(), ":");
                        if (st.nextToken().equalsIgnoreCase("PLAYER")) {
                            String playerName = st.nextToken();
                            st.nextToken();
                            int score = new Integer(st.nextToken()).intValue();
                            Player p = new Player(playerName, score);
                            players.add(p);
                        }
                    }
                    gamestate.setNumPlayers(players.size());
                } else if (type.equalsIgnoreCase("#GAMEOVER")) {
                    gameOver = true;                    
                } else if (type.equalsIgnoreCase("#SHOWDOWN")) {
                    gamestate.setShowdown(true);
                    showdownString = messageTokenizer.nextToken();
                }
            } else {
                gamestateString = serverMessage;
                gamestate.update(serverMessage);
                int currentPlayer = ((gamestate.getPlayerPosition() == 1) ? gamestate.getCurrentPlayer() : ((gamestate.getCurrentPlayer() + 1) % gamestate.getNumPlayers()));
                gamestate.setLastAction(players.get(currentPlayer).getName() + " " + gamestate.getLastAction());
                writeToLog(gamestate.getLastAction());
            }
        } catch (IOException ex) {
            logError(ex.toString());
            JOptionPane.showMessageDialog(this.getFrame(), "Server Unavailable.\n " +
                    "Please try again later", "Server Unavailable", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (NullPointerException ex) {
            logError(ex.toString());
            JOptionPane.showMessageDialog(this.getFrame(), "Server Unavailable.\n " +
                    "Please try again later", "Server Unavailable", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    /**
     * Update the GUI.  Disable the buttons, set up the button text accordingly
     * and then update the graphics
     */
    private void update() {
        enableButtons(false);
        if (gamestate != null) {
            if (gamedef.isNoLimit() && gamestate.getLastRaise() == 0) {
                callButton.setText("Check");
            } else if (gamestate.getCurrentBet() == 0) {
                callButton.setText("Check");
            } else {
                callButton.setText("Call");
            }
        }
        String raiseText = (gamestate.getRaiseFlag() ? "Raise " : "Bet ");
        String foldText = (gamestate.getRaiseFlag() ? "Fold" : callButton.getText());
        if (gamedef.isNoLimit()) {
            int minValidBet = (gamestate.getLastRaise() > gamedef.getMinBet() ? gamestate.getLastRaise() : gamedef.getMinBet());
            betsizeSlider.setMinBet(minValidBet);            
            raiseButton.setText(raiseText + betsizeSlider.getValue());
        } else {
            int betSize = gamedef.getBet(gamestate.getCurrentRound());
            raiseButton.setText(raiseText + betSize);
        }
        foldButton.setText(foldText);
        tablePanel.update(gamestate, players);
    }

    public void doShowdown() {
        StringTokenizer st = new StringTokenizer(showdownString, ":");
        while(st.hasMoreTokens()) {            
            String winnerString = st.nextToken();
            StringTokenizer innerST = new StringTokenizer(winnerString, "/");
            String winnerHand = innerST.nextToken();
            String winnerCards;
            try{
                winnerCards = innerST.nextToken();
                tablePanel.setWinningHand(winnerCards);
            } catch (NoSuchElementException ex){                
                winnerCards = "hand ended in a fold";
                tablePanel.setWinningHand("");
            }
            gamestate.setLastAction(winnerHand);
            writeToLog("Hand:" + gamestate.getCardSequence());            
            StringTokenizer winnerST = new StringTokenizer(winnerHand, " ");            
            String winnerName = winnerST.nextToken();
            winnerST.nextToken();
            writeToLog(winnerHand +": "+ winnerCards);            
            int amount = new Integer(winnerST.nextToken()).intValue();
            int winnings = gamedef.getStackSize() - Math.max(gamestate.getStackSize(0), gamestate.getStackSize(1));
            for (Player p : players) {
                if (p.getName().equalsIgnoreCase(winnerName)) {
                    int stackIndex = players.indexOf(p);
                    amount -= (gamedef.getStackSize() - gamestate.getStackSize(stackIndex));
                }
            }
            for (Player p : players) {
               if (p.getName().equalsIgnoreCase(winnerName)) {
                    p.addToScore(winnings);
                } else {
                    p.addToScore(-winnings);
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                logError(ex.toString());
            }
            update();
        }        
        gamestate.setShowdown(false);
    }

    /**
     * The GameLoop class is a thread class used to essentially handle the listening 
     * for @Gamestate messages from the server and then updating the GUI.
     */
    class GameLoop implements Runnable {
        /**
         * Listen and update while the game is not over
         */
        public void run() {
            while (!gameOver) {
                listenForGameState();
                update();
                if (gamestate.isShowdown()) {                   
                    doShowdown();                    
                }
                enableButtons(true);
            }
            tablePanel.setGameOver(gameOver);
            update();
            if(gamedef.hasSurvey()) {
                try {                    
                    tablePanel.setHasSurvey(true);
                    update();
                    Thread.sleep(8000);
                    URLTools.openURL(gamedef.getSurveyURL()+"?key="+key);
                } catch (InterruptedException ex) {
                    logError(ex.toString());
                }
            }
            
        }
    }
}
