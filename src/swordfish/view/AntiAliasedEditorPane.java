/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swordfish.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.FileNotFoundException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * An editor pane used to display anti-aliased text in the help file.
 * @author jdavidso
 */
public class AntiAliasedEditorPane
        extends javax.swing.JEditorPane{

    /** Creates a new instance of AliasedEditorPane */
    public AntiAliasedEditorPane() {
        super();        
    }

    /**
     * Ovverride the paintComponent method and turn on the text anti-aliasing 
     * rendering hints
     * @param g The graphics to paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        super.paintComponent(g2);
    }
    
    
    /**
     * used to follow hyperlink events in the editor pane
     * @param evt a Hyperlink Event
     */
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        HyperlinkEvent.EventType type = evt.getEventType();
        if (type == HyperlinkEvent.EventType.ENTERED) {
            System.out.println(evt.getURL().toString());
        } else if (type == HyperlinkEvent.EventType.EXITED) {
            System.out.println("Exited");
        } else if (type == HyperlinkEvent.EventType.ACTIVATED) {
            if (evt instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent evt1 = (HTMLFrameHyperlinkEvent) evt;
                HTMLDocument doc = (HTMLDocument) getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt1);
            } else {
                try {
                    setPage(evt.getURL());
                    System.out.println(evt.getURL().toString());
                } catch (FileNotFoundException fnfe) {
                    setText("Could not open file: <tt>" + evt.getURL() + "</tt>.<hr>");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
