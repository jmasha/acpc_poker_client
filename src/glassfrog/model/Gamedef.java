package glassfrog.model;

import swordfish.tools.XMLParser;
import java.io.IOException;
import java.io.Serializable;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A class used to define all of the properties of the game.  The properties
 * can either be specified through getters and setters, a constructor or parsed
 * in via an XML file
 * @author jdavidso
 */
public class Gamedef implements Serializable{

    private int numHands;
    private int numRounds;
    private int maxPlayers;
    private int minPlayers;
    private int minBet;
    private int maxBet;
    private int stackSize;
    private int smallBlind;
    private int[] blindStructure;
    private int[] numPrivateCards;
    private int[] numPublicCards;
    private int[] betsPerRound;
    private int[] betStructure;
    private boolean reverseBlinds;
    private boolean doylesGame;
    private boolean noLimit;
    private String surveyURL;

    /**
     * The Blind structure is an array that represents what players must pay
     * an ante or blind.  The numbers are in terms of small blinds, so for
     * Texas Holdem, with a big and small blind the array looks like [1,2].
     * For an ante game with 4 players, each paying an ante every round, it looks
     * like [1,1,1,1].
     * @return an array representing the blind or antes
     */
    public int[] getBlindStructure() {
        return blindStructure;
    }

    /**
     * Set up the blind structure.  The indices of the array corrispond to the
     * player starting with the first player left of the dealer and the values 
     * corrispond to the size of blind in multiples of the small blind
     * @param blindStructure an array representing the blinds or antes
     */
    public void setBlindStructure(int[] blindStructure) {
        this.blindStructure = blindStructure;
    }

    /**
     * Return an array representing the number of bets allowed in a round.  The 
     * array indices corrispond to the round and the values to the number of bets 
     * allowed
     * @return an array representing the bets per round
     */
    public int[] getBetsPerRound() {
        return betsPerRound;
    }

    /**
     * Set the number of bets allowed per round.  The array indices corrispond 
     * to the round and the values to the number of bets allowed
     * @param betsPerRound an array representing the bets per round
     */
    public void setBetsPerRound(int[] betsPerRound) {
        this.betsPerRound = betsPerRound;
    }

    /**
     * Get the betting structure for the game.  This is for games where bet sizes
     * are fixed per round, and we need to knwo what that system is.  For limit
     * Texas Holdem where we have a small and big bet, adn the big bet is on the 
     * turn and river, the array will look like [1,1,2,2] where these are multiples
     * of the betsize.
     * @return an array representing the bet structure of the game
     */
    public int[] getBetStructure() {
        return betStructure;
    }

    /**
     * Set the betting structure for the game.  This is for games where bet sizes
     * are fixed per round, and we need to knwo what that system is.  For limit
     * Texas Holdem where we have a small and big bet, adn the big bet is on the 
     * turn and river, the array will look like [1,1,2,2] where these are multiples
     * of the betsize.
     * @param betStructure an array representing the bet structure of the game
     */
    public void setBetStructure(int[] betStructure) {
        this.betStructure = betStructure;
    }

    /**
     * Return the maximum bet size of the game.  In No-Limit we set this to -1 
     * @return an int representation of the max bet size
     */
    public int getMaxBet() {
        return maxBet;
    }

    /**
     * Set the max bet a player is allowed to make in the game.
     * @param maxBet an int representing the max bet a player is allowed to make
     */
    public void setMaxBet(int maxBet) {
        this.maxBet = maxBet;
    }

    /**
     * Get the maximum amount of players alloed to play in this game
     * @return an int representing the the maximum amount of players 
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Set the maximum amount of players allowed in the game
     * @param maxPlayers an int representing the the maximum amount of players 
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Get the minimum amount of players allowed to play in this game
     * @return an int representing the the minimum amount of players 
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Set the minimum amount of players allowed in the game
     * @param minPlayers an int representing the the minimum amount of players 
     */
    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    /**
     * Check to see whether or not this game is Doyles Game, a game where
     * we reset the stack size after every hand and keep a score instead of 
     * overall stack achievement 
     * @return True for Doyles Game, False otherwise
     */
    public boolean isDoylesGame() {
        return doylesGame;
    }

    /**
     * Set whether or not this game is Doyles Game, a game where we reset the 
     * stack size after every hand and keep a score instead of overall stack 
     * achievement 
     * @param doylesGame True for Doyles Game, False otherwise
     */
    public void setDoylesGame(boolean doylesGame) {
        this.doylesGame = doylesGame;
    }

    /**
     * Get the minimum bet for the game
     * @return an int representing the minimum bet size for the game
     */
    public int getMinBet() {
        return minBet;
    }

    /**
     * Set the minimum bet for the game
     * @param minBet an int representing the minimum bet size for the game
     */
    public void setMinBet(int minBet) {
        this.minBet = minBet;
    }
    
    /**
     * Check to see if this is a no limit betting game
     * @return True for no limit games, false otherwise
     */
    public boolean isNoLimit() {
        return noLimit;
    }
    
    /**
     * Set whether or not this is a no limit betting game
     * @param noLimit True for no limit, false otherwise
     */
    public void setNoLimit(boolean noLimit) {
        this.noLimit = noLimit;
    }

    /**
     * Get the number of private cards each player is dealt.  The indices corrispond
     * to the round and the values corrispond to how many cards a player is dealt
     * in that round.  Texas Holdem looks like [2,0,0,0].
     * @return an array of ints representing the cards dealt to the players.
     */
    public int[] getNumPrivateCards() {
        return numPrivateCards;
    }

    /**
     * Set the number of private cards each player is dealt.  The indices corrispond
     * to the round and the values corrispond to how many cards a player is dealt
     * in that round.  Texas Holdem looks like [2,0,0,0].
     * @param numPrivateCards an array of ints representing the cards dealt to the players.
     */
    public void setNumPrivateCards(int[] numPrivateCards) {
        this.numPrivateCards = numPrivateCards;
    }

    /**
     * Get the number of puyblic cards dealt each round.  The indices corrispond
     * to the round and the values corrispond to how many cards are dealt
     * in that round.  Texas Holdem looks like [0,3,1,1].
     * @return an array of ints representing the cards dealt each round.
     */
    public int[] getNumPublicCards() {
        return numPublicCards;
    }

    /**
     * Set the number of puyblic cards dealt each round.  The indices corrispond
     * to the round and the values corrispond to how many cards are dealt
     * in that round.  Texas Holdem looks like [0,3,1,1].
     * @param numPublicCards an array of ints representing the cards dealt each round.
     */
    public void setNumPublicCards(int[] numPublicCards) {
        this.numPublicCards = numPublicCards;
    }

    /**
     * Get the number of hands to be played in the game
     * @return an int representing the number of hands to be played
     */
    public int getNumHands() {
        return numHands;
    }

    /**
     * Set the number of hands to be played in the game
     * @param numHands an int representing the number of hands to be played
     */
    public void setNumHands(int numHands) {
        this.numHands = numHands;
    }

    /**
     * Get the number of rounds to be played each hand. This should corrispond
     * to the size of the card arrays and betting arrays if being used
     * @return an int representing the number of rounds to be played each hand
     */
    public int getNumRounds() {
        return numRounds;
    }

    /**
     * Set the number of rounds to be played each hand. This should corrispond
     * to the size of the card arrays and betting arrays if being used
     * @param numRounds an int representing the number of rounds to be played each hand
     */
    public void setNumRounds(int numRounds) {
        this.numRounds = numRounds;
    }

    /**
     * Check to see whether or not reverse blinds is in effect.  This is a two 
     * player Texas Holdem trait where the blinds are reversed in heads up play
     * to retain dealer positional avantage.
     * @return True if reverse blinds are in play, false otherwise
     */
    public boolean isReverseBlinds() {
        return reverseBlinds;
    }

    /**
     * Set whether or not reverse blinds is in effect.  This is a two player 
     * Texas Holdem trait where the blinds are reversed in heads up play to retain 
     * dealer positional avantage.
     * @param reverseBlinds a boolean used to set the reverseBlinds flag
     */
    public void setReverseBlinds(boolean reverseBlinds) {
        this.reverseBlinds = reverseBlinds;
    }

    /**
     * Get the size of the small blind.
     * @return an int representing the value of the small blind
     */
    public int getSmallBlind() {
        return smallBlind;
    }

    /**
     * Set the size of the small blind.
     * @param smallBlind an int representing the value of the small blind
     */
    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
    }

    /**
     * Get the starting stack size assiged to the players if using set stack sizes
     * as in Doyles Game
     * @return an int representing the starting stack size for the players
     */
    public int getStackSize() {
        return stackSize;
    }

    /**
     * Set the starting stack size value for each player if using set stack sizes
     * as in Doyles Game
     * @param stackSize an int representing the starting stack size for the players
     */
    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }
    
    /**
     * Return the URL for the post game survey;
     * @return a String representing the URL for the post game survey;
     */
    public String getSurveyURL() {
        return surveyURL;
    }
    
    /**
     * Check to see if this game has a post game survey
     * @return Whether or not the surveyURL has been set
     */
    public boolean hasSurvey() {
        return surveyURL != null;
    }

    /**
     * Default contructor.  Set to 2 player texas holdem limit 1/2 for testing     
     */
    public Gamedef() {
        numRounds = 4;
        maxPlayers = 2;
        stackSize = 200;
        minBet = 2;
        maxBet = 4;
        smallBlind = 1;
        numPrivateCards = new int[]{2, 0, 0, 0};
        numPublicCards = new int[]{0, 3, 1, 1};
        betsPerRound = new int[]{3,4,4,4};
        betStructure = new int[]{1,1,2,2};
        blindStructure = new int[]{1, 2};
        reverseBlinds = true;
        noLimit = false;
        surveyURL = null;
    }

    /**
     * A contructor for a gamedef that takes a path to an XML file that represents
     * that gamedef
     * @param path The path to the gamedef XML file
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXParseException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public Gamedef(String path) throws ParserConfigurationException, SAXParseException, 
            SAXException, IOException {
        XMLParser parser = new XMLParser(path);
        NodeList nl = parser.parseElements("Gamedef");
        for (int i = 0; i < nl.getLength(); i++) {
            Node gdNode = nl.item(i);
            if (gdNode.getNodeType() == Node.ELEMENT_NODE) {
                numHands = parser.getIntFromNode(gdNode, "Hands");
                numRounds = parser.getIntFromNode(gdNode, "Rounds");
                minPlayers = parser.getIntFromNode(gdNode, "MinPlayers");
                maxPlayers = parser.getIntFromNode(gdNode, "MaxPlayers");
                minBet = parser.getIntFromNode(gdNode, "MinBet");
                maxBet = parser.getIntFromNode(gdNode, "MaxBet");
                stackSize = parser.getIntFromNode(gdNode, "StackSize");
                smallBlind = parser.getIntFromNode(gdNode, "SmallBlind");
                blindStructure = parser.getArrayFromNode(gdNode, "BlindStructure");
                numPrivateCards = parser.getArrayFromNode(gdNode, "PrivateCards");
                numPublicCards = parser.getArrayFromNode(gdNode, "PublicCards");
                betsPerRound = parser.getArrayFromNode(gdNode, "BetsPerRound");
                betStructure = parser.getArrayFromNode(gdNode, "BetStructure");
                reverseBlinds = parser.getBooleanFromNode(gdNode, "ReverseBlinds");
                doylesGame = parser.getBooleanFromNode(gdNode, "DoylesGame");
                noLimit = parser.getBooleanFromNode(gdNode, "NoLimit");                
                surveyURL = parser.getStringFromNode(gdNode, "SurveyURL");
            }
        }
    }

    /**
     * Get the blind / ante associated with the index using the blind structure
     * and the small blind value.
     * @param index The index from which to return the blind value
     * @return the value of the blind for the given index
     */
    public int getBlind(int index) {
        return blindStructure[index] * smallBlind;
    }

    /**
     * Get the raise value associated with the round.  This is used for games
     * where there are different betsizes for different rounds given in terms of
     * minBets
     * @param index The index from which to return the bet value
     * @return the value of the bet for the given index
     */
    public int getBet(int index) {
        return betStructure[index] * minBet;
    }
    
    /**
     * Get the gamedef as a String;
     * @return a String representing relative gamedef info
     */
    @Override
    public String toString() {
        String gdString = "GAMEDEF:";
        gdString += "Hands:"+numHands+":Rounds:"+numRounds+
                        ":MinPlayers:"+minPlayers+":MaxPlayers:"+maxPlayers+
                        ":MinBet:"+minBet+":MaxBet:"+maxBet+":StackSize:"+stackSize+
                        ":SmallBlind:"+smallBlind+":BlindStructure:"+printArray(blindStructure)+
                        ":PrivateCardArray:"+printArray(numPrivateCards)+
                        ":PublicCardArray:"+printArray(numPublicCards)+
                        ":BetsPerRound:"+printArray(betsPerRound)+
                        ":BetStructure:"+printArray(betStructure)+
                        ":ReverseBlinds:"+reverseBlinds+":DoylesGame:"+doylesGame+
                        ":NoLimit:"+noLimit+":SurveyURL:"+surveyURL;
        return gdString;        
    }
    
    /**
     * A helper function to print an int array to a string, comma delimited
     * @param array an int array
     * @return A string representation of the array in the form {1,2,3,....}
     */
    private String printArray(int[] array) {
        String arrayString = "{";
        for(int i : array) {
            arrayString += i+",";
        }
        arrayString = arrayString.substring(0, arrayString.length()-1);
        arrayString += "}";
        return arrayString;
    }
}
