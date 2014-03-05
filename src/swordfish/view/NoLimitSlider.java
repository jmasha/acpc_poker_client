package swordfish.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

/**
 * A Slider class that has some buttons for betsizes and such
 * @author jdavidso
 */
public class NoLimitSlider extends JPanel
{
    
    private JSlider betsizeSlider;
    private JButton potsizeButton, halfpotButton, allinButton;
    private int stacksize = 400;
    private int minBet = 2;    
    
    public NoLimitSlider() {
        super();
        this.setOpaque(false);        
        potsizeButton = new JButton("Pot");
        halfpotButton = new JButton("1/2 Pot");
        allinButton = new JButton("All In");        
        potsizeButton.setActionCommand("potsize");
        halfpotButton.setActionCommand("halfpotsize");
        allinButton.setActionCommand("allin");
        /*
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if("potsize".equals(e.getActionCommand())) {
                    betsizeSlider.setValue(100);
                } else if ("halfpotsize".equals(e.getActionCommand())) {
                    betsizeSlider.setValue(50);
                } else if ("allin".equals(e.getActionCommand())) {
                    betsizeSlider.setValue(stacksize);
                }
            }
            
        };
        potsizeButton.addActionListener(al);
        halfpotButton.addActionListener(al);
        allinButton.addActionListener(al);*/
        betsizeSlider = new JSlider(SwingConstants.HORIZONTAL,minBet,stacksize,minBet);
        betsizeSlider.setPreferredSize(new Dimension(205, 25));
        betsizeSlider.setMinorTickSpacing((stacksize*5)/100);
        betsizeSlider.setMajorTickSpacing((stacksize*25)/100);                
        betsizeSlider.setPaintTicks(true);            
        
        JPanel buttonGroup = new JPanel() {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(205, 15);
            }
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }                        
        };
        
        buttonGroup.setLayout(new BoxLayout(buttonGroup,BoxLayout.LINE_AXIS));
        buttonGroup.add(halfpotButton);
        buttonGroup.add(potsizeButton);
        buttonGroup.add(allinButton);
        buttonGroup.setOpaque(false);
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        add(buttonGroup);
        buttonGroup.setAlignmentY(TOP_ALIGNMENT);          
        add(betsizeSlider);
        betsizeSlider.setOpaque(false);   
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent (g2);
    }
    
    public int getValue() {
        return betsizeSlider.getValue();
    }
    
    public void setValue(int value) {
        betsizeSlider.setValue(value);
    }
    
    public void setMinBet(int minBet) {
        this.minBet = minBet;
    }
    
    public JSlider getSlider() {
        return betsizeSlider;
    }
            
    public void enableSlider(Boolean enabled) {
        betsizeSlider.setEnabled(enabled);
        potsizeButton.setEnabled(enabled);
        halfpotButton.setEnabled(enabled);
        allinButton.setEnabled(enabled);
        betsizeSlider.setValue(minBet);
    }

    public void invisible() {
        betsizeSlider.setVisible(false);
        potsizeButton.setVisible(false);
        halfpotButton.setVisible(false);
        allinButton.setVisible(false);
    }        
    
    public void initSlider(int minbet, int stacksize) {
        this.minBet = minbet;
        this.stacksize = stacksize;
        betsizeSlider.setMaximum(stacksize);
        betsizeSlider.setMinimum(minbet);
        betsizeSlider.setValue(minbet);
    }
    
    public void addButtonActionListener(ActionListener al) {
        potsizeButton.addActionListener(al);
        halfpotButton.addActionListener(al);
        allinButton.addActionListener(al);
    }
}
