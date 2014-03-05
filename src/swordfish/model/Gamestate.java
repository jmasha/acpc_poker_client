package swordfish.model;

import glassfrog.model.Gamedef;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Class to represent the gamestate from the GUI Player perspective. It has
 * all of the attributes of the current hand being played that are relevant to the
 * player such as the dealer position, the player's position, the current pot size,
 * the current bet size, etc.
 * 
 * @author jdavidso
 */
public class Gamestate{
    
    private boolean showdown, safeExit, checkFlag, raiseFlag;
    private int playerPosition, numPlayers, potSize, currentBet, handNumber, 
            lastRaise, currentRound, currentPlayer;    
    private String lastAction;
    private ArrayList<Card>[] publicCards, privateCards;
    private Gamedef gamedef;
    private int[] stackSize;
    private String cardSequence;
    
    /**
     * Set up a gamestate using a @Gamedef send by the server
     * @param gamedef
     */
    public Gamestate(Gamedef gamedef) {
        potSize = 0;
        currentBet = 0;
        lastAction = "";
        currentPlayer = 0;
        showdown = false;   
        this.gamedef = gamedef;
        stackSize = new int[]{gamedef.getStackSize(),gamedef.getStackSize()};
    }
    
    /**
     * Return the cardSequence for the current gamestate
     * @return a String representing the current cardSequence
     */
    public String getCardSequence() {
        return cardSequence;
    }

    /**
     * Get the current bet size
     * @return an int representing the current bet size
     */
    public int getCurrentBet() {
        return currentBet;
    }    
    
    /**
     * Return the current player's index
     * @return an index of the current player
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Get the current round
     * @return an int representing the current round of the game
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Get the current hand number 
     * @return an int representing the hand number of the game
     */
    public int getHandNumber() {
        return handNumber;
    }

    /**
     * Get the last action of the game
     * @return a String representing the last action taken in the game
     */
    public String getLastAction() {
        return lastAction;
    }

    /**
     * Set the last action taken string
     * @param lastAction a String representing the last action taken
     */
    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }
    
    /**
     * Get the size of the last raise
     * @return an int representing the size of the last raise
     */
    public int getLastRaise() {
        return lastRaise;
    }
    
    /**
     * Get the stack size for the specified player
     * @param player an int representing the seat of the player to return the stacksize of
     * @return an int representing the specified player's stacksize
     */
    public int getStackSize(int player) {
        return stackSize[player]; 
    }

    /**
     * Get the number of players currently in the game
     * @return an int representing the number of players in the game
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Set the number of players in the game
     * @param numPlayers an int representing how many players are to be playing in the game
     */
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    /**
     * Return the position of the GUI Player
     * @return the current position of the GUI Player
     */
    public int getPlayerPosition() {
        return playerPosition;
    }

    /**
     * Return the current potsize
     * @return an int representing the potsize
     */
    public int getPotSize() {
        return potSize;
    }

    /**
     * Return an ArrayList of the @Card type of the GUI Player's private cards
     * @return an ArrayList of the player's private cards
     */
    public ArrayList<Card>[] getPrivateCards() {
        return privateCards;
    }    

    /**
     * Return an ArrayList of the @Card type of the public cards
     * @return an ArrayList of the public cards
     */
    public ArrayList<Card>[] getPublicCards() {
        return publicCards;
    }

    /**
     * Check to see if we are in a showdown
     * @return True if we are in a showdown, False otherwise
     */
    public boolean isShowdown() {
        return showdown;
    }    

    /**
     * Set the showdown flag
     * @param showdown A boolean representing whether or not we are in a showdown
     */
    public void setShowdown(boolean showdown) {
        this.showdown = showdown;
    }   
    
    /**
     * Checks to see if the player is the dealer
     * @return True if the player is the dealer, False otherwise
     */
    public boolean isDealer() {
        return (playerPosition == numPlayers-1);
    }
    
    /**
     * Add to the pot the amount given.
     * @param amount an int amount to add to the pot
     */
    public void addToPot(int amount) {
        potSize += amount;
    }
    
    /**
     * Subtract from the pot the amount given.  Used for hand evaluation and payout
     * @param amount The amount to subtract from the potsize
     */
    public void subtractFromPot(int amount) {
        potSize -= amount;
    }
    
    /**
     * Update all of the variables in the gamestate from a new gamestate string.
     * The gamestate string represents a gamestate and is sent from the GlassFrog
     * server.  See the sendGamestate or update Player methods in the server for 
     * more details on the format
     * @param gamestateString a String representing the gamestate from the GlassFrog Server
     */
    public void update(String gamestateString) {
        parseGameState(gamestateString);
    }
    
    /**
     * Check to see whether or not we can exit safely.  That is, whether or not
     * we have acted.  This is for quitting the client mid hand
     * @return True for a safe exit, False otherwise
     */
    public boolean isSafeExit() {
        return safeExit;
    }
    
    /**
     * Check to see if the next bet is a raise or bet
     * @return raiseFlag
     */
    public boolean getRaiseFlag() {
        return raiseFlag;
    }
    
    /**
     * Return the amount needed for a specific player to call
     * @param player The player index to check for the call amount
     * @return tha amount needed to call
     */
    public int getCallAmount(int player) {
        return getCurrentBet() - (gamedef.getStackSize() - stackSize[player]);
    }
    
    /**
     * Increment the currentPlayer counter
     */
    private void incCurrentPlayer() {
        currentPlayer = ++currentPlayer % numPlayers;
    }
    
    /**
     * Parse out the relevant information from the gamestate passed in from the
     * server.  From the string we should be able to get the following information:
     * Hand number
     * Our position
     * The betting string, which gives us the current bet, the pot size and the last action taken
     * The private and public cards for the player
     * @param gamestateString A string representing the gamestate
     */
    private void parseGameState(String gamestateString) {        
        StringTokenizer st = new StringTokenizer(gamestateString, ":");
        if (st.countTokens() < 4 || !st.nextToken().equalsIgnoreCase("MATCHSTATE")) {
            System.out.println("Invalid gamestate string");
            return;
        }
        playerPosition = new Integer(st.nextToken()).intValue();
        handNumber = new Integer(st.nextToken()).intValue();
        potSize = 0;
        safeExit = true;
        currentPlayer = 0;
        if(gamedef.isDoylesGame()) {
            stackSize = new int[]{gamedef.getStackSize(),gamedef.getStackSize()} ;
        }
        if(!gamedef.isNoLimit()) {
            postBlinds();   
        }
        if (st.countTokens() == 2) {
            String bettingSequence = st.nextToken();            
            if (gamedef.isNoLimit()) {
                parseNoLimitBettingSequence(bettingSequence);
            } else {                               
                parseLimitBettingSequence(bettingSequence);
            }
        }
        parseCardSequence(st.nextToken());        
    }

    /**
     * Post the blinds for the game, if they exist
     */
    private void postBlinds() {
        for (int i = 0; i < gamedef.getBlindStructure().length; i++) {
            incCurrentPlayer();
            addToPot(gamedef.getBlind(i));
            stackSize[currentPlayer]-=gamedef.getBlind(i);            
        }
        lastAction = "posted blinds";
        currentBet = gamedef.getSmallBlind();
        checkFlag = false;
    }

    /**
     * Parse the information out of the betting sequence token from the gameState
     * message passed in from the server.  From this we can get the current bet
     * the last action and the pot size
     *      
     * @param bettingSequence A String representing a limit betting sequence in 
     * accordance to the AAAI format
     */
    private void parseLimitBettingSequence(String bettingSequence) {                
        int index = 0, i = 0;        
        StringTokenizer st = new StringTokenizer(bettingSequence, "/");        
        while (st.hasMoreTokens()) {
            String round = st.nextToken();            
            currentBet = (index == 0 ? gamedef.getSmallBlind() : 0);            
            currentPlayer = (index == 0 ? 0 : 1);                                    
            for (char c : round.toCharArray()) {
                i++;
                incCurrentPlayer();
                switch (c) {
                    case 'b':
                        lastAction = "posted blinds";
                        checkFlag = false;
                        raiseFlag = true;
                        break;
                    case 'c':
                        lastAction = (checkFlag ? "checks" : "calls");
                        checkFlag = true;
                        raiseFlag = false;
                        addToPot(currentBet);
                        stackSize[currentPlayer] -= currentBet;                       
                        currentBet = 0;
                        break;
                    case 'r':
                        addToPot(currentBet);
                        stackSize[currentPlayer] -= currentBet;
                        currentBet = gamedef.getBet(index);
                        addToPot(currentBet);
                        stackSize[currentPlayer] -= currentBet;
                        lastAction = (raiseFlag ? "raises ": "bets ")+(currentBet);
                        checkFlag = false;
                        raiseFlag = true;                        
                        break;
                    case 'f':
                       lastAction = "folds";                                
                        return; 
                    case '/':
                        break;
                    default:
                        lastAction = "Unknown Action " + c;                        
                        break;
                }                         
            }
            index++;
        }
        currentRound = index-1;
        if(i > 1) {
            safeExit = false;
        }
    }

    /**
     * Parse the no limit betting sequence.  This will give us the current bet and
     * the pot size, as well as the last action taken;
     * 
     * @param bettingSequence A String representing a no limit betting sequence
     * in accordance to the AAAI format
     */
    private void parseNoLimitBettingSequence(String bettingSequence) {
        StringTokenizer st = new StringTokenizer(bettingSequence, "/");
        int index = 0, lastBet = 0, i = 0;
        currentBet = 0;        
        Matcher m;
        Pattern p;        
        while (st.hasMoreTokens()) {
            String round = st.nextToken();            
            p = Pattern.compile("\\d+");
            m = p.matcher(round);                        
            currentPlayer = (index == 0 ? 0 : 1);                        
            while (m.find()) {                                
                i++;
                incCurrentPlayer();
                lastBet = currentBet;
                currentBet = Integer.parseInt(round.substring(m.start(), m.end()));
                stackSize[currentPlayer] = gamedef.getStackSize()-currentBet;                
            }                        
            index++;
        }
        if(i > 3) {
            safeExit = false;
        }
        //stackSize[currentPlayer] = gamedef.getStackSize()-currentBet;
        //incCurrentPlayer();
        //stackSize[currentPlayer] = gamedef.getStackSize()-lastBet;
        potSize = lastBet + currentBet;
        lastRaise = Math.abs(currentBet - lastBet);        
        p = Pattern.compile("[brcf]");
        m = p.matcher(bettingSequence);
        while (m.find()) {
            parseLastAction(bettingSequence.substring(m.start(), m.end()));
        }
    }

    /**
     * Get the last action performed and convert it from the single char to a 
     * human readable action
     * 
     * @param action The last action as a single character string;
     */
    private void parseLastAction(String action) {
        switch (action.charAt(action.length() - 1)) {
            case 'r':
                lastAction = (raiseFlag ? "raises ": "bets ")+(lastRaise);
                checkFlag = false;
                raiseFlag = true;
                return;
            case 'c':
                lastAction = (checkFlag ? "checks" : "calls");
                checkFlag = true;
                raiseFlag = false;
                return;
            case 'f':
                lastAction = "folds";                                
                return;
            case 'b':
                lastAction = "posted blinds";
                checkFlag = false;
                raiseFlag = true;
                return;
            default:
                System.out.println("Unknown action" + action);
                lastAction = "Unknown action: " + action;
                return;
        }
    }

    /**
     * Parse the hand information out of the cardSequence token from the gamestate
     * messages passed from the server.  This will give us the public cards, the 
     * private cards and all of round information.
     * 
     * @param cardSequence a String representing a cardSequence conforming to the
     * AAAI format
     */
    private void parseCardSequence(String cardSequence) {
        this.cardSequence = cardSequence;
        StringTokenizer st = new StringTokenizer(cardSequence, "/");
        currentRound = (st.countTokens() - 1);
        publicCards = null;
        parsePrivateCards(st.nextToken());
        if (st.hasMoreTokens()) {
            publicCards = new ArrayList[currentRound];
            Pattern p = Pattern.compile("[\\d\\w]\\w");
            int i = 0;
            String cardString;
            while (st.hasMoreTokens()) {
                publicCards[i] = new ArrayList<Card>();
                cardString = st.nextToken();
                Matcher m = p.matcher(cardString);
                while (m.find()) {
                    try {
                        publicCards[i].add(new Card(cardString.substring(m.start(), m.end())));
                    } catch (IOException ex) {
                        System.err.println("Card Parse Error");
                    }
                }
                i++;
            }
        }        
    }

    /**
     * Parse out the visible private cards for each of the players
     * @param privateCardString a String representing the visible private cards
     * conforming to the AAAI format
     */
    private void parsePrivateCards(String privateCardString) {
        StringTokenizer st = new StringTokenizer(privateCardString, "|");
        privateCards = new ArrayList[st.countTokens()];
        String cards;
        int i = 0;
        while (st.hasMoreTokens()) {
            cards = st.nextToken();
            privateCards[i] = new ArrayList<Card>();
            if (cards.equalsIgnoreCase("")) {
                i++;
                continue;
            }
            Pattern p = Pattern.compile("[\\d\\w]\\w");
            Matcher m = p.matcher(cards);
            while (m.find()) {
                try {
                    privateCards[i].add(new Card(cards.substring(m.start(), m.end())));
                } catch (IOException ex) {
                    System.out.println("Card Error in Private Cards");
                }
            }
            i++;
        }       
    }
}
