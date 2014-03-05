package swordfish.view;

import java.util.logging.Level;
import java.util.logging.Logger;
import swordfish.model.Player;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;
import swordfish.model.Card;
import swordfish.model.Gamestate;

/**
 * The TablePanel class is used to display all the Gamestate Information to the 
 * user.  It handles the visualization of the Gamestate and shows the user all
 * the relevant information.  Hacked up for 2 player Texas Holdem
 * @author jdavidso
 */
public class TablePanel extends JPanel implements ActionListener, ChangeListener,
        WindowListener{

    private BufferedImage cardbackImage,  img;
    private Gamestate gamestate;
    private Font tableFont;
    private List<Player> players;
    private ArrayList<Card> winningHand = new ArrayList<Card>();
    private boolean gameOver, hasSurvey;
    private boolean hideStacks = false;
    private static final int FLOP_START_X = 175;
    private static final int FLOP_START_Y = 45;
    private static final int HOLE_START_X = 65;
    private static final int HOLE_START_Y = 150;
    private static final int OPP_HOLE_START_X = 65;
    private static final int OPP_HOLE_START_Y = 60;
    private static final int CARD_WIDTH = 65;
    private static final int CARD_HEIGHT = 90;
    private static final int CARD_SPACER = 10;
    private static final int POT_SIZE_X = 20;
    private static final int POT_SIZE_Y = 70;
    private static final int STACK_X = 20;
    private static final int STACK_Y = 60;
    private static final int SCORE_X = 20;
    private static final int SCORE_Y = 90;
    private static final int BUTTON_X = 85;
    private static final int BUTTON_Y = 75;
    private static final int NAME_Y = 40;
    private static final int ACTION_X = 20;
    private static final int ACTION_Y = 30;
    private static final float SMALL_FONT_SIZE = 20f;
    private static final float LARGE_FONT_SIZE = 24f;
    private static final float ALPHA = 0.5f;    


    /**
     * Default constructor sets up the cardback image, the font and a null gamestate
     */
    public TablePanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
        setFocusable(true);
        img = null;
        gamestate = null;
        gameOver = false;
        try {
            cardbackImage = ImageIO.read(this.getClass().getResource(
                    "resources/cardback.jpg"));
            tableFont = Font.createFont(Font.TRUETYPE_FONT, this.getClass().
                    getResourceAsStream("resources/AppleGaramond.ttf"));            
        } catch (IOException ex) {
        } catch (FontFormatException ex) {
        }
        //registerKeyboardAction()
    }

    /** Add a listener for window events. */
    void addWindowListener(Window w) {
        w.addWindowListener(this);
    }

    public void windowOpened(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowClosing(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowClosed(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowIconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowDeiconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowActivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowDeactivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void stateChanged(ChangeEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    

    /**
     * Draw the table and the background
     * @param g The graphics object used to paint
     * @throws java.io.IOException
     */
    private void drawTable(Graphics2D g) throws IOException {
        URL imagePath = this.getClass().getResource("resources/table.jpg");
        img = ImageIO.read(imagePath);
        g.drawImage(img, 0, 0, this.getSize().width, this.getSize().height, 0, 0,
                img.getWidth(), img.getHeight(), null);
    }

    /**
     * Draw the cards based on the current round of the gamestate
     * @param g The graphics object used to paint
     */
    private void drawCards(Graphics2D g) {
        switch (gamestate.getCurrentRound()) {
            case 0:
                drawHoleCards(g);
                break;
            case 1:
                drawHoleCards(g);
                drawFlopCards(g);
                break;
            case 2:
                drawHoleCards(g);
                drawFlopCards(g);
                drawTurnCard(g);
                break;
            case 3:
                drawHoleCards(g);
                drawFlopCards(g);
                drawTurnCard(g);
                drawRiverCard(g);
                break;
        }
    }

    /**
     * Draw the hole cards for both players.  This determines the player position 
     * and draws the cards accordingly from the gamestate.
     * @param g The graphics object used to paint
     */
    private void drawHoleCards(Graphics2D g) {
        List<Card>[] privateCards = gamestate.getPrivateCards();
        int position;
        if(privateCards == null || privateCards.length == 0) {
            return;
        }       
        if(privateCards.length == 1) {
            position = 0;
        } else {
            position = gamestate.getPlayerPosition();
        }
        try {
            img = privateCards[position].get(0).getCardImage();
            if(gamestate.isShowdown()) {
                if(!winningHand.contains(privateCards[position].get(0))) {
                    img = transparentImage(img,ALPHA);
                }   
            }
        } catch (NullPointerException ex) {
                img = cardbackImage;
        } catch (IndexOutOfBoundsException ex) {
                img = cardbackImage;
        }
        g.drawImage(img, this.getWidth() / 2 - HOLE_START_X, this.getHeight() -
                HOLE_START_Y, this.getWidth() / 2 - HOLE_START_X + CARD_WIDTH,
                this.getHeight() - HOLE_START_Y + CARD_HEIGHT, 0, 0,
                img.getWidth(), img.getHeight(), null);
        try {
            img = privateCards[position].get(1).getCardImage();
            if(gamestate.isShowdown()) {
                if(!winningHand.contains(privateCards[position].get(1))) {
                    img = transparentImage(img,ALPHA);
                }   
            }
        } catch (NullPointerException ex) {
                img = cardbackImage;
        } catch (IndexOutOfBoundsException ex) {
                img = cardbackImage;
        }
        g.drawImage(img, this.getWidth() / 2 - HOLE_START_X + CARD_WIDTH +
                    CARD_SPACER, this.getHeight() - HOLE_START_Y, this.getWidth() / 2 -
                    HOLE_START_X + 2 * CARD_WIDTH + CARD_SPACER, this.getHeight() -
                    HOLE_START_Y + CARD_HEIGHT, 0, 0, img.getWidth(), img.getHeight(),
                    null);
        
        //Opponent Hole Cards
        int i = (position + 1) % (players.size());
        while (i != position) {
            try {
                img = (!gamestate.isShowdown() ? cardbackImage : privateCards[i]
                        .get(0).getCardImage());                
                if(gamestate.isShowdown()) {
                    if(!winningHand.contains(privateCards[i].get(0))) {
                        img = transparentImage(img,ALPHA);
                    }   
                }
            } catch (NullPointerException ex) {
                    img = cardbackImage;
            } catch (IndexOutOfBoundsException ex) {
                    img = cardbackImage;
            }
            g.drawImage(img, this.getWidth() / 2 - OPP_HOLE_START_X,
                    OPP_HOLE_START_Y, this.getWidth() / 2 - OPP_HOLE_START_X +
                    CARD_WIDTH, OPP_HOLE_START_Y + CARD_HEIGHT, 0, 0,
                    img.getWidth(), img.getHeight(), null);
            try {
                img = (!gamestate.isShowdown() ? cardbackImage : privateCards[i]
                        .get(1).getCardImage());
                if(gamestate.isShowdown()) {
                    if(!winningHand.contains(privateCards[i].get(1))) {
                        img = transparentImage(img,ALPHA);
                    }   
                }
            } catch (NullPointerException ex1) {
                img = cardbackImage;
            } catch (IndexOutOfBoundsException ex) {
                img = cardbackImage;
            }
            g.drawImage(img, this.getWidth() / 2 - OPP_HOLE_START_X + CARD_WIDTH +
                    CARD_SPACER, OPP_HOLE_START_Y, this.getWidth() / 2 -
                    OPP_HOLE_START_X + 2 * CARD_WIDTH + CARD_SPACER, OPP_HOLE_START_Y +
                    CARD_HEIGHT, 0, 0, img.getWidth(), img.getHeight(), null);
            i = ++i % (players.size());
        }

        if (gamestate.isDealer()) {
            g.setColor(Color.WHITE);
            g.fillOval(this.getWidth() / 2 - BUTTON_X, this.getHeight() - BUTTON_Y, 15, 15);
            g.setColor(Color.darkGray);
            g.drawOval(this.getWidth() / 2 - BUTTON_X, this.getHeight() - BUTTON_Y, 16, 16);
        } else {
            g.setColor(Color.WHITE);
            g.fillOval(this.getWidth() / 2 - BUTTON_X, BUTTON_Y, 15, 15);
            g.setColor(Color.darkGray);
            g.drawOval(this.getWidth() / 2 - BUTTON_X, BUTTON_Y, 16, 16);
        }
    }

    /**
     * Draw the flop cards on the board
     * @param g The graphics object used to paint
     */
    private void drawFlopCards(Graphics2D g) {
        List<Card>[] publicCards = gamestate.getPublicCards();
        try {
            img = publicCards[0].get(0).getCardImage();
            if(gamestate.isShowdown()) {
                if(!winningHand.contains(publicCards[0].get(0))) {
                    img = transparentImage(img,ALPHA);
                }
            }
            g.drawImage(img, this.getWidth() / 2 - FLOP_START_X, this.getHeight() / 2 -
                    FLOP_START_Y, this.getWidth() / 2 - FLOP_START_X + CARD_WIDTH,
                    this.getHeight() / 2 - FLOP_START_Y + CARD_HEIGHT, 0, 0,
                    img.getWidth(), img.getHeight(), null);
            img = publicCards[0].get(1).getCardImage();
            if(gamestate.isShowdown()) {
                if(!winningHand.contains(publicCards[0].get(1))) {
                    img = transparentImage(img,ALPHA);
                }
            }
            g.drawImage(img, this.getWidth() / 2 - FLOP_START_X + CARD_WIDTH +
                    CARD_SPACER, this.getHeight() / 2 - FLOP_START_Y, this.getWidth() / 2 -
                    FLOP_START_X + 2 * CARD_WIDTH + CARD_SPACER, this.getHeight() / 2 -
                    FLOP_START_Y + CARD_HEIGHT, 0, 0, img.getWidth(), img.getHeight(), null);
            img = publicCards[0].get(2).getCardImage();
            if(gamestate.isShowdown()) {
                if(!winningHand.contains(publicCards[0].get(2))) {
                    img = transparentImage(img,ALPHA);
                }
            }
            g.drawImage(img, this.getWidth() / 2 - FLOP_START_X + 2 * CARD_WIDTH +
                    2 * CARD_SPACER, this.getHeight() / 2 - FLOP_START_Y,
                    this.getWidth() / 2 - FLOP_START_X + 3 * CARD_WIDTH +
                    2 * CARD_SPACER, this.getHeight() / 2 - FLOP_START_Y +
                    CARD_HEIGHT, 0, 0, img.getWidth(), img.getHeight(), null);
        } catch (NullPointerException ex) {
            System.out.println("Null flop cards???... ");
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Missing flop cards???... ");
        }
    }

    /**
     * Draw the turn card on the board
     * @param g The graphics object used to paint
     */
    private void drawTurnCard(Graphics2D g) {
        List<Card>[] publicCards = gamestate.getPublicCards();        
        try {
            img = publicCards[1].get(0).getCardImage();
            if(gamestate.isShowdown()) {
                if(!winningHand.contains(publicCards[1].get(0))) {
                    img = transparentImage(img,ALPHA);
                }
            }
            g.drawImage(img, this.getWidth() / 2 - FLOP_START_X + 3 * CARD_WIDTH +
                    3 * CARD_SPACER, this.getHeight() / 2 - FLOP_START_Y,
                    this.getWidth() / 2 - FLOP_START_X + 4 * CARD_WIDTH + 3 *
                    CARD_SPACER, this.getHeight() / 2 - FLOP_START_Y + CARD_HEIGHT,
                    0, 0, img.getWidth(), img.getHeight(), null);            
        } catch (NullPointerException ex) {
            System.out.println("Null turn card???... ");
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Missing turn card???... ");
        }
    }

    /**
     * Draw the River Card on the board
     * @param g The graphics object used to paint
     */
    private void drawRiverCard(Graphics2D g) {
        List<Card>[] publicCards = gamestate.getPublicCards();
        try {
            img = publicCards[2].get(0).getCardImage();
            if(gamestate.isShowdown()) {
                if(!winningHand.contains(publicCards[2].get(0))) {
                    img = transparentImage(img,ALPHA);
                }
            }
            g.drawImage(img, this.getWidth() / 2 - FLOP_START_X + 4 * CARD_WIDTH +
                    4 * CARD_SPACER, this.getHeight() / 2 - FLOP_START_Y,
                    this.getWidth() / 2 - FLOP_START_X + 5 * CARD_WIDTH + 4 *
                    CARD_SPACER, this.getHeight() / 2 - FLOP_START_Y + CARD_HEIGHT,
                    0, 0, img.getWidth(), img.getHeight(), null);
        } catch (NullPointerException ex) {
            System.out.println("Null river cards???... ");
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Missing river cards???... ");
        }
    }

    /**
     * Draw the stats such as the player names, score stacks, etc.
     * @param g The graphics object used to paint
     */
    private void drawStats(Graphics2D g) {
        if(players.size() == 0) {
            return;
        }
        drawNames(g);
        if(!hideStacks) {
            drawStacks(g);
        }
        drawScores(g);
        drawPot(g);
        drawCurrentAction(g);
        drawHandCount(g);
    }

    /**
     * Draw the player names
     * @param g The graphics object used to paint
     */
    private void drawNames(Graphics2D g) {        
        FontMetrics fm = g.getFontMetrics();
        g.setFont(tableFont.deriveFont(SMALL_FONT_SIZE));
        g.setColor(Color.WHITE);
        try {
            int p0StringWidth = (int)fm.getStringBounds(players.get(0).getName(),g).getWidth();
            int p1StringWidth = (int)fm.getStringBounds(players.get(1).getName(),g).getWidth();                       
            g.drawString(players.get(1).getName(), this.getWidth() / 2 - p1StringWidth / 2, this.getHeight() - NAME_Y);
            g.drawString(players.get(0).getName(), this.getWidth() / 2 - p0StringWidth / 2, NAME_Y);
        } catch (NullPointerException ex) {
            System.out.println("Null Player Names...");
        }
    }

    /**
     * Draw the player stacks
     * @param g The graphics object used to paint
     */
    private void drawStacks(Graphics2D g) {          
        int pIndex = gamestate.getPlayerPosition();
        int oppIndex = (gamestate.getPlayerPosition()+1) % gamestate.getNumPlayers();
        
        g.setColor(Color.CYAN);
        g.setFont(tableFont.deriveFont(SMALL_FONT_SIZE));
        FontMetrics fm = g.getFontMetrics();
        int p0StringWidth = fm.stringWidth("Chips: $" + gamestate.getStackSize(pIndex));
        int p1StringWidth = fm.stringWidth("Chips: $" + gamestate.getStackSize(oppIndex));
        g.drawString("Chips: $" + gamestate.getStackSize(pIndex), this.getWidth() - p0StringWidth - STACK_X, this.getHeight() - STACK_Y);
        g.drawString("Chips: $" + gamestate.getStackSize(oppIndex), this.getWidth() - p1StringWidth - STACK_X, STACK_Y);
    }

    /**
     * Draw the player scores
     * @param g The graphics object used to paint
     */
    private void drawScores(Graphics2D g) {        
        FontMetrics fm = g.getFontMetrics();
        int p0StringWidth = fm.stringWidth("Amount won: $" + players.get(0).getScore());
        int p1StringWidth = fm.stringWidth("Amount won: $" + players.get(1).getScore());
        g.setFont(tableFont.deriveFont(SMALL_FONT_SIZE));
        g.setColor((players.get(1).getScore() >= players.get(0).getScore() ? Color.GREEN : Color.RED));
        g.drawString("Amount won: $" + players.get(1).getScore(), this.getWidth() - p1StringWidth - SCORE_X, this.getHeight() - SCORE_Y);
        g.setColor((players.get(0).getScore() >= players.get(1).getScore() ? Color.GREEN : Color.RED));
        g.drawString("Amount won: $" + players.get(0).getScore(), this.getWidth() - p0StringWidth - SCORE_X, SCORE_Y);
    }

    /**
     * Draw the current potsize
     * @param g The graphics object used to paint
     */
    private void drawPot(Graphics2D g) {
        g.setFont(tableFont.deriveFont(SMALL_FONT_SIZE));
        g.setColor(Color.ORANGE);
        g.drawString("The Pot is currently: $" + gamestate.getPotSize(), POT_SIZE_X, POT_SIZE_Y);
    }

    /**
     * Draw the current hand number
     * @param g The graphics object used to paint
     */
    private void drawHandCount(Graphics2D g) {
        g.setFont(tableFont.deriveFont(SMALL_FONT_SIZE));
        g.setColor(Color.CYAN);
        g.drawString("HAND " + (gamestate.getHandNumber()+1), POT_SIZE_X, POT_SIZE_Y - 40);
    }

    /**
     * Draw the current action
     * @param g The graphics object used to paint
     */
    private void drawCurrentAction(Graphics2D g) {
        String lastAction = gamestate.getLastAction();
        g.setFont(tableFont.deriveFont(SMALL_FONT_SIZE));
        FontMetrics fm = g.getFontMetrics();
        int action_x = (int)fm.getStringBounds(lastAction, g).getWidth()/2;
        int action_y = (int)fm.getStringBounds(lastAction, g).getHeight();
        g.setColor(Color.WHITE);        
        g.drawString(lastAction, this.getWidth()/2 - action_x, this.getHeight()/2 - CARD_HEIGHT/2 - action_y);
    }

    /**
     * Draw the gameover message and the gameover stats summary
     * @param g The graphics object used to paint
     */
    private void drawGameOver(Graphics2D g) {
        String gameOverString = "GAME OVER  Thank you for playing.";
        g.setColor(Color.WHITE);
        g.setFont(tableFont.deriveFont(LARGE_FONT_SIZE));
        FontMetrics fm = g.getFontMetrics();
        int gameover_x = (int)fm.getStringBounds(gameOverString, g).getWidth()/2;
        int gameover_y = (int)fm.getStringBounds(gameOverString, g).getHeight()/2;
        Player winner = (players.get(0).getScore() > players.get(1).getScore() ? 
            players.get(0) : players.get(1));        
        gamestate.setLastAction(winner.getName()+" won the match with a score of "+winner.getScore());
        g.drawString(gameOverString, this.getWidth() / 2 - gameover_x, this.getHeight() / 2 - gameover_y);                        
        if(hasSurvey) {
            String surveyString = "You will be redirected to the post game survey in 5s as well as sent an email with the link.";
            int survey_x = (int)fm.getStringBounds(surveyString, g).getWidth()/2;
            int survey_y = (int)fm.getStringBounds(surveyString, g).getHeight()/2;
            g.drawString(surveyString, this.getWidth() / 2 - survey_x, this.getHeight() / 2 - gameover_y*2 -survey_y);                        
        }
    }
    
    /**
     * Draw the waiting message before the game starts
     * @param g The graphics object used to paint
     */
    private void drawWaiting(Graphics2D g) {
        String gameOverString = "Waiting for message from server";
        g.setColor(Color.WHITE);
        g.setFont(tableFont.deriveFont(SMALL_FONT_SIZE));
        FontMetrics fm = g.getFontMetrics();
        int gameover_x = (int)fm.getStringBounds(gameOverString, g).getWidth()/2;
        int gameover_y = (int)fm.getStringBounds(gameOverString, g).getHeight()/2;
        g.drawString(gameOverString, this.getWidth() / 2 - gameover_x, this.getHeight() / 2 - gameover_y);
    }

    /**
     * Overriding rhis method allows for the anti-aliasing of the graphics to be set
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D ig = (Graphics2D) g;
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            drawTable(ig);
        } catch (IOException ex) {
            ex.toString();
        }
        if (!gameOver && gamestate!=null && !gamestate.getLastAction().equalsIgnoreCase("")) {
            drawCards(ig);
            drawStats(ig);
        } else if (gameOver) {
            drawGameOver(ig);
            drawStats(ig);
        } else {
            drawWaiting(ig);
        }
        
    }

    /**
     * Repaint the board with the new gamestate and player list
     * @param gamestate the current @Gamestate of the game
     * @param players the current list of @Player objects
     */
    public void update(Gamestate gamestate, List<Player> players) {
        this.gamestate = gamestate;
        this.players = players;
        repaint();
    }
    
    /**
     * Set the gameOver flag to true
     * @param gameOver
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }    
    
    /**
     * Set the survey flag for post game surveys
     * @param hasSurvey
     */
    public void setHasSurvey(boolean hasSurvey) {
        this.hasSurvey = hasSurvey;
    }
    
    /**
     * Set a flag to hide the stacks
     * @param hideStacks
     */
    public void setHideStack(boolean hideStacks) {
        this.hideStacks = hideStacks;
    }
    
    /**
     * 
     * @param winningHandString
     */
    public void setWinningHand(String winningHandString) { 
        winningHand.clear();
        if(winningHandString.equalsIgnoreCase("")) {            
            return;
        }
        StringTokenizer st = new StringTokenizer(winningHandString," ");
        while(st.hasMoreTokens()) {
            Card c;
            try {
                c = new Card(st.nextToken());
                winningHand.add(c);                        
            } catch (IOException ex) {                
            }
        }
    }
    
    private BufferedImage transparentImage(BufferedImage img, float transperancy) {                             
         BufferedImage aimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TRANSLUCENT);  
         // Get the images graphics  
         Graphics2D g = aimg.createGraphics();  
         // Set the Graphics composite to Alpha  
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transperancy));  
         // Draw the LOADED img into the prepared reciver image  
         g.drawImage(img, null, 0, 0);  
         // let go of all system resources in this Graphics  
         g.dispose();  
        // Return the image  
         return aimg;  
     }
}
