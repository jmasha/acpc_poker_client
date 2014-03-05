package swordfish.model;

/**
 * The Player class represents the players in the game.  They are given attribures
 * such as thier current bet, score, how much they have commited to the pot, thier
 * seat, hand rank, stack size.  They also keep track of the actions of the player, 
 * such as if the player has acted in a particular round or if they have folded in
 * a hand
 * 
 * Note: As of Sept 2009, this class is a shell and needs to be re-implemented with the
 * GUI for better integration
 * @author jdavidso
 */
public class Player {

    private String name;    
    private int score;
    
    /**
     * A Player in the GUI Class.  The constructor here takes in the name,
     * stack, score and seat of the player.  Score was added in order to reload players
     * on disconnect.
     * @param name A String representing the name of the player
     * @param score an int representing the score of the player
     */
    public Player(String name, int score) {
        this.name = name;
        this.score = score;
    }

    /**
     * Get the current score of the player
     * @return an int representing the player's score
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Get the name of the player
     * @return a @String representing the player's name
     */
    public String getName(){
        return name;
    }
    
    /**
     * Add to the players score
     * @param score an int representing the amount to add to the score of the player
     * can be negative
     */
    public void addToScore(int score) {
        this.score += score;
    }
}
