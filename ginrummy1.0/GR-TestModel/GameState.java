import java.util.ArrayList;
import java.util.Arrays;
import java.io.Serializable;

/**
 * @author Sang T. Truong and Hoang H. Pham
 * @version 1.0
 * 
 * Important: For convinience, our player is always player 0. 
 * In other words, we only collect the data that is visible to player 0.
 * The GameState class collect information of a game-state, where it is the turn of player 0.
 * There are 3 pieces of information that are collected: 
 * 
 * - What player 1 has just done:
 * 		+ The face-up card that was presented to player 1: faceUp1
 * 		+ If player 1 has just drawn face-up: drawnFaceUp1
 * 		+ The card player 1 has just drawn: drawn1
 * 		+ The card player 1 has just discarded: discarded1
 * 
 * - What is currently presenting to player 0: 
 * 		+ The hand of player 0: hand0
 * 		+ The face-up card that is presenting to player 0: faceUp0
 * 
 * - What the player 0 will do
 * 		+ The card player 0 will discard: willDraw0
 * 		+ If player 0 will willDraw0 face-up: willDiscard0
 */

public class GameState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1801083449164178536L;
	private Card faceUp1; // Face-up card that that was presented to player 1
	private boolean drawnFaceUp1; // If player 1 has just drawn face-up card
	private Card drawn1; // Card that player 1 has just drawn
	private Card discarded1; // Card that player 1 has just discarded
	private long estimatedHand1;
	private long hand1;
	
	private double[] prob0 = new double[52]; // Estimation of player 0 about the probability of a card is in player
	private long hand0; // Hand of player 0
	private Card faceUp0; // Face-up card that is presenting to player 0
	
	private Card willDraw0; // Card that player 0 will draw
	private Card willDiscard0; // Card that player 0 will discard
	
	/**
	 * Collect the real value of player's 1 hand to asset the performance of estimator
	 * @param hand1
	 */
	public void setHand1(ArrayList<Card> hand1)
	{
		this.hand1 = GinRummyUtil.cardsToBitstring(hand1);
	}
	
	/**
	 * * Set the face-up card that was presented to player 1
	 * @param faceUp1
	 */
	public void setFaceUp1(Card faceUp1)
	{
		this.faceUp1 = faceUp1;
	}
	
	/**
	 * Set if player 1 has just drawn face-up
	 * @param drawnFaceUp1
	 */
	public void setDrawnFaceUp1(boolean drawnFaceUp1)
	{
		this.drawnFaceUp1 = drawnFaceUp1;
	}
	
	/**
	 * Set the card that player 1 has just drawn
	 * @param drawn1
	 */
	public void setDrawn1(Card drawn1)
	{
		this.drawn1 = drawn1;
	}
	
	/**
	 * Set the card that player 1 has just discarded
	 * @param discarded1
	 */
	public void setDiscarded1(Card discarded1)
	{
		this.discarded1 = discarded1;
	}
	
	public void setEstimatedHand1(ArrayList<Card> estimatedHand1)
	{
		this.estimatedHand1 = GinRummyUtil.cardsToBitstring(estimatedHand1);
	}
	
	/**
	 * Set the hand of player 0
	 * @param hand0
	 */
	public void setHand0(ArrayList<Card> hand0)
	{
		this.hand0 = GinRummyUtil.cardsToBitstring(hand0);
	}
	
	/**
	 * Set the face-up card that is presenting to player 0
	 * @param faceUp0
	 */
	public void setFaceUp0(Card faceUp0)
	{
		this.faceUp0 = faceUp0;
	}
	
	/**
	 * Set the card that player 0 will draw
	 * @param willDraw0
	 */
	public void setWillDraw0(Card willDraw0)
	{
		this.willDraw0 = willDraw0;
	}
	
	/**
	 * Set the card that player 0 will discard
	 * @param willDiscard0
	 */
	public void setWillDiscard0(Card willDiscard0)
	{
		this.willDiscard0 = willDiscard0;
	}
	
	public void setProb0(double[] prob0)
	{
		if (prob0 != null)
		{
			for (int i = 0; i < 52; i++)
				this.prob0[i] = prob0[i]; // clone prob0 'object'
		}
	}
	
	//-----------------------------------------------------------------
	
	/**
	 * Get the real value of player 1's hand to asset the performance of estimator
	 * @return hand1
	 */
	public long getHand1()
	{
		return this.hand1;
	}
	
	/**
	 * Get the face-up card that was presented to player 1
	 * @return faceUp1
	 */
	public Card getFaceUp1()
	{
		return this.faceUp1;
	}
	
	/**
	 * Get if player 1 has just drawn the face-up card
	 * @return drawnFaceUp1
	 */
	public boolean getDrawnFaceUp1()
	{
		return this.drawnFaceUp1;
	}
	
	/**
	 * Get the card that player 1 has just drawn
	 * @return drawn1
	 */
	public Card getDrawn1()
	{
		return this.drawn1;
	}
	
	/**
	 * Get the card that player 1 has just discarded
	 * @return discarded1
	 */
	public Card getDiscarded1()
	{
		return this.discarded1;
	}
	
	public long getEstimatedHand1()
	{
		return this.estimatedHand1;
	}
	
	/**
	 * Get the hand of player 0
	 * @return hand0
	 */
	public long getHand0()
	{
		return this.hand0;
	}
	
	/**
	 * Get the face-up card that is presenting to player 0
	 * @return faceUp0
	 */
	public Card getFaceUp0()
	{
		return this.faceUp0;
	}
		
	/**
	 * Get the card that player 0 will draw
	 * @return willDraw0
	 */
	public Card getWillDraw0()
	{
		return this.willDraw0;
	}
	
	/**
	 * Get the card that player 0 will discard
	 * @return willDiscard0
	 */
	public Card getWillDiscard0()
	{
		return this.willDiscard0;
	}
	
	public double[] getProb0()
	{
		return this.prob0;
	}
	//-----------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "hand: " + GinRummyUtil.bitstringToCards(this.hand0) + ", "
			 + "up: " + this.faceUp0  + ", "
			 + "willDraw0: "  + this.willDraw0  + ", "
			 + "willDiscard0: " + this.willDiscard0 + ", "
			 + "opponent drawn: " + this.drawn1 + ", "
			 + "opponent discarded: " + this.discarded1 + ", "
			 + "opponent drawn faceup: " + (this.drawnFaceUp1 ? 1 : 0) + ", "
			 + "opponent faceup: " + this.faceUp1 + ", "
			 + "estimated opponent's hand: " + GinRummyUtil.bitstringToCards(this.estimatedHand1) + ", "
			 + "opponent's hand: " + GinRummyUtil.bitstringToCards(this.hand1) + ", "
			 + "estimated opponent's probabilities: " + Arrays.toString(this.prob0);
	}
}