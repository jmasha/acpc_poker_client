package swordfish.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * The graphical Card class used to display card images.
 * A Card has a suit, a rank and an associated image
 * @author jdavidso
 */
public class Card {    

    private Integer rank;
    private String suit;
    private BufferedImage cardImage;
    private static final String IMAGEPATH_BASE = "resources/images/";

    /**
     * Default constructor, no suit, rank 0 and a null card image
     */
    public Card() {
        rank = 0;
        suit = "Unsuited";
        cardImage = null;
    }    

    /**
     * Constructor that takes the suit and rank all in one string.  The suit is the
     * first char of the string and the rank is the rest of it.
     * @param suitAndRank a String representing the suit and rank of the card
     * @throws java.io.IOException
     */      
    public Card(String suitAndRank) throws IOException {
        parseRank(suitAndRank.substring(0, 1));
        parseSuit(suitAndRank.substring(1));
        cardImage = ImageIO.read(imageLookup());
    }
    
    /**
     * Return the rank of the card Ace = 1, King = 13
     * @return the rank of the card
     */
    public Integer getRank() {
        return rank;
    }

    /**
     * Return the suit of the card
     * @return one of spades, hearts diamonds or clubs
     */
    public String getSuit() {
        return suit;
    }

    /**
     * Return the image that the card representss
     * @return an image of the proper card for the suit and rank pair
     */
    public BufferedImage getCardImage() {
        return cardImage;
    }

    /**
     * Lookup the card image from the resource file and return the URL to it's location
     * @return a URL to the location of the image in the resource file
     */
    private URL imageLookup() {
        //Get the card image
        URL imagePath = this.getClass().getResource(IMAGEPATH_BASE + suit.toLowerCase() + "-" + printRank().toLowerCase() + "-75.png");
        return imagePath;
    }

    /**
     * Return a String reprenting the Card in the form of RankSuit where Rank is 
     * 2-9,T,J,Q,K,A and suit is s,c,d,h
     * @return the String representation of the card
     */
    @Override
    public String toString() {
        return printRank() + printSuit();
    }

    /**
     * Print the first letter of the suit for the card
     * @return the first letter of the suit
     */
    private String printSuit() {
        return suit.substring(0, 1).toLowerCase();
    }

    /**
     * One of 2-9,T,J,Q,K,A for the rank
     * @return a String representing the rank of the card
     */
    private String printRank() {
        if (rank == 1) {
            return "A";
        } else if (rank < 10) {
            return rank.toString();
        } else {
            switch (rank) {
                case 10:
                    return "T";
                case 11:
                    return "J";
                case 12:
                    return "Q";
                case 13:
                    return "K";
            }
        }
        return "";
    }

    /**
     * Parse the rank of the card from the string, 
     * @param rankString a String containing the card rank
     */
    private void parseRank(String rankString) {
        if (rankString.equalsIgnoreCase("T")) {
            rank = 10;
        } else if (rankString.equalsIgnoreCase("J")) {
            rank = 11;
        } else if (rankString.equalsIgnoreCase("Q")) {
            rank = 12;
        } else if (rankString.equalsIgnoreCase("K")) {
            rank = 13;
        } else if (rankString.equalsIgnoreCase("A")) {
            rank = 1;
        } else {
            rank = new Integer(rankString);
        }
    }
    
    /**
     * Parse the Suit from the one character representations
     * @param suitString a String representing the one character suit
     */
    private void parseSuit(String suitString) {
        switch(suitString.charAt(0)) {
            case 'c':
                suit ="clubs";
                return;
            case 'd':
                suit = "diamonds";
                return;
            case 'h':
                suit = "hearts";
                return;
            case 's':
                suit = "spades";
                return;                
        }
    }

    @Override
    public boolean equals(Object arg0) {
        Card c = (Card)arg0;
        if(c.rank.intValue() == this.rank.intValue() && c.suit.equalsIgnoreCase(this.suit)) {
            return true;
        }
        return false;
    }        

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.rank != null ? this.rank.hashCode() : 0);
        hash = 47 * hash + (this.suit != null ? this.suit.hashCode() : 0);
        return hash;
    }
}
