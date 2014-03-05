/*
 * RoomDialog.java
 *
 * Created on November 21, 2008, 12:19 PM
 */
package swordfish.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A dialog used to connect to the server.  Currently obsolete due to auto-connect
 * but may be re-invoked for multiple server use
 * @author  jdavidso
 */
public class SwordfishRoomDialog extends javax.swing.JDialog {

    private DefaultMutableTreeNode root;
    private DefaultMutableTreeNode[] serverList;
    private DefaultTreeModel model;
    private PrintWriter pw;
    private BufferedReader br;    
    private SwordfishView view;

    /** Creates new form RoomDialog */
    public SwordfishRoomDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        populateRoomTree();
        initComponents();
    }

    public SwordfishRoomDialog(java.awt.Frame parent, SwordfishView view, boolean modal) {
        super(parent, modal);
        this.view = view;
        initComponents();
        populateRoomTree();
    }

    private void populateRoomTree() {
        initializeServerList();
        root = new DefaultMutableTreeNode("Servers");
        for (int i = 0; i < serverList.length; i++) {
            root.add(serverList[i]);
        }
        model = new DefaultTreeModel(root);
        roomTree.setModel(model);
    }

    private void initializeServerList() {
        serverList = getServerList();
        for (int i = 0; i < serverList.length; i++) {
            populateServer(serverList[i]);
        }
    }

    private DefaultMutableTreeNode[] getServerList() {
        DefaultMutableTreeNode[] defaultServers = new DefaultMutableTreeNode[1];
        defaultServers[0] = new DefaultMutableTreeNode("129.128.184.126");
        boolean isLoaded = false;
        DefaultMutableTreeNode[] servers = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();            
            Document doc = (Document) docBuilder.parse(this.getClass().getResourceAsStream("resources/serverList.xml"));
            doc.getDocumentElement().normalize();

            NodeList xmlServerList = doc.getElementsByTagName("Server");
            servers = new DefaultMutableTreeNode[xmlServerList.getLength()];
            for (int i = 0; i < xmlServerList.getLength(); i++) {
                Node serverNode = xmlServerList.item(i);
                if (serverNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element serverElement = (Element) serverNode;
                    String name = serverElement.getElementsByTagName("Name").item(0).getChildNodes().item(0).getNodeValue().trim();
                    String ip = serverElement.getElementsByTagName("IP").item(0).getChildNodes().item(0).getNodeValue().trim();
                    String port = serverElement.getElementsByTagName("Port").item(0).getChildNodes().item(0).getNodeValue().trim();
                    servers[i] = new DefaultMutableTreeNode(ip);
                    isLoaded = true;
                }
            }
        } catch (ParserConfigurationException ex) {
            view.logError(ex.toString());
            isLoaded = false;
        } catch (IOException ex) {
            view.logError(ex.toString());
            isLoaded = false;
        } catch (SAXParseException ex) {
            view.logError("** Parsing error" + ", line " + ex.getLineNumber() + ", uri " + ex.getSystemId());
            view.logError(ex.toString());
            isLoaded = false;
        } catch (SAXException ex) {
            view.logError(ex.toString());
            isLoaded = false;
        }
        if (isLoaded) {
            return servers;
        }
        return defaultServers;
    }

    private boolean populateServer(DefaultMutableTreeNode server) {
        try {
            Socket serverConnection = new Socket(server.toString(), 9000);
            br = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
            pw = new PrintWriter(serverConnection.getOutputStream(), true);
            pw.println("LIST");
            pw.flush();
            String response = br.readLine();
            if(response.equalsIgnoreCase("No Rooms Found")) {
                server.add(new DefaultMutableTreeNode(response));
            } else {                
                StringTokenizer st = new StringTokenizer(response,"||");                
                while(st.hasMoreTokens()) {
                    String room = st.nextToken();
                    if(!isFull(room)) {
                        server.add(new DefaultMutableTreeNode(room));
                    }
                }
            }
        } catch (UnknownHostException ex) {
            view.logError(ex.toString());
            return false;
        } catch (IOException ex) {
            view.logError(ex.toString());
            return false;
        }
        return true;
    }
    
    private boolean isFull(String roomString) {
        boolean retVal;
        retVal = roomString.split(":")[5].equalsIgnoreCase("Full") ? true : false;
        return retVal;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        treePanel = new javax.swing.JPanel();
        treeScrollPane = new javax.swing.JScrollPane();
        roomTree = new javax.swing.JTree(root);
        buttonPanel = new javax.swing.JPanel();
        connectButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        treePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        treePanel.setName("treePanel"); // NOI18N
        treePanel.setLayout(new java.awt.GridLayout(1, 0));

        treeScrollPane.setName("treeScrollPane"); // NOI18N

        roomTree.setModel(null);
        roomTree.setName("roomTree"); // NOI18N
        roomTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                roomTreeMousePressed(evt);
            }
        });
        roomTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                roomTreeValueChanged(evt);
            }
        });
        treeScrollPane.setViewportView(roomTree);
        roomTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        treePanel.add(treeScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 9.0;
        getContentPane().add(treePanel, gridBagConstraints);

        buttonPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(swordfish.view.SwordfishApp.class).getContext().getActionMap(SwordfishRoomDialog.class, this);
        connectButton.setAction(actionMap.get("connect")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(swordfish.view.SwordfishApp.class).getContext().getResourceMap(SwordfishRoomDialog.class);
        connectButton.setText(resourceMap.getString("connectButton.text")); // NOI18N
        connectButton.setName("connectButton"); // NOI18N
        buttonPanel.add(connectButton, java.awt.BorderLayout.CENTER);

        refreshButton.setAction(actionMap.get("refresh")); // NOI18N
        refreshButton.setText(resourceMap.getString("refreshButton.text")); // NOI18N
        refreshButton.setName("refreshButton"); // NOI18N
        buttonPanel.add(refreshButton, java.awt.BorderLayout.PAGE_START);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void roomTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_roomTreeValueChanged
    if (roomTree.getSelectionCount() > 0) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) roomTree.getLastSelectedPathComponent();
        if (node.getChildCount() == 0 && !node.toString().equalsIgnoreCase("No Rooms Found")) {
            connectButton.setEnabled(true);
        } else {
            connectButton.setEnabled(false);
        }
    } else {
        connectButton.setEnabled(false);
    }
}//GEN-LAST:event_roomTreeValueChanged

private void roomTreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_roomTreeMousePressed
    if (evt.getClickCount() > 1) {
        connect();        
    }
}//GEN-LAST:event_roomTreeMousePressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                SwordfishRoomDialog dialog = new SwordfishRoomDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    @Action
    public void refresh() {
        populateRoomTree();       
    }

    @Action
    public void connect() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) roomTree.getLastSelectedPathComponent();
        if (node.getChildCount() == 0 && !node.toString().equalsIgnoreCase("No Rooms Found")) {
            System.out.println(node.toString());
            String[] roomSpecs = node.toString().split(":");
            //view.connectToServer(node.getParent().toString(), new Integer(roomSpecs[4]).intValue());            
            this.setVisible(false);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton connectButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JTree roomTree;
    private javax.swing.JPanel treePanel;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
}
