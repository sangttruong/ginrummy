import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Sang T. Truong and Hoang H. Pham
 * @version 2.0

 Among 52 cards, there are cards that player 0 knows their locations.
 They can be in one of these 3 places: 
 - Player 0 's hand
 - Face-up pile
 - Player 1 's hand if player 1 has drawn that card face-up
 
 There are also cards that player 0 doesn't know their locations. 
 They can be in either:
 - Player 1's hand if player 1's has drawn that card face-down
 - Face-down pile
 
 From a player 0 point of view, the known and unknown character of all cards 
 are present in an array of boolean. As always, everything that belongs to player 0 has
 suffix 0 at the end: known0.
 
 Given a card that is unknown, player 0 can estimate the probability of player 1
 having that card in player 1's hand. As always, everything that belongs to player 0 has
 suffix 0 at the end: prob0. 
 
 Obviously:
 - If player 0 has card i, the probability of player 1 has i, prob0[i], is 0.
 - If player 0 saw that
 	+ Player 1 has drawn card i face-up
 	+ Player 1 hasn't discarded card i
   the probability of player 1 having card i is 1.
   
 Whenever player 1 execute a transaction (i.e. draw and discard), player 1 releases some
 information about player 1's hand. By carefully observe that WHAT cards player 1 has drawn and
 has discarded and HOW they were transacted, player 0 can estimate what cards player 1 is having.

 For this HandEstimator class, a transaction is characterized by 4 properties: 
 - If player 1 has drawn face-up
 - If player 1 has discarded a card that was suited with the face-up card (that was presented to
   player 1 at player 1's turn)
 - The rank of the face-up card that was presented to player 1 at his turn
 - The rank of the card that has just been discarded by player 1's at his turn
 Information about the frequency of a particular transaction is stored in a 4D array: heldTransaction.
 In total, there are 2*2*13*13 = 676 types of transaction.
 
 Each card in 52 cards is further associated with a transaction: Given face-up card f that was presented
 to player 1, discard card d that was discarded by player 1 in his turn, for card c in 52 cards:
 - If c isn't suited with both f and d: 0
 - If c is only suited with f: 1
 - If c is only suited with d: 2
 
 The rank of c is also use to associate c with transaction t. 
 
 Given the fact that playr 1 has card c, the frequency of player 1 execuates transaction t where there is 
 a certain suit-association between c and t is store in a 6D array: heldCounts. This frequency can be use 
 to compute the probability of transaction T given the fact that player 1 has card c: P(T|C)
 
 Using P(T|C) and prior probability of player 1 has card C P(C), we can compute the relative likelihood of
 player 1 has card C given he executes a certain transaction: P(C|T) using Bayes theorem: 
 
 										P(C|T) ~ P(T|C) * P(C)
 */

public class HandEstimator_ver2 {
	private static final int HAND_SIZE = 10;
	private static final int MIN_VISITS = 50;
	private static final double EPS = .1 / MIN_VISITS; // to make non-events rare but not impossible
	final int DRAW_FACE_UP = 1, DRAW_FACE_DOWN = 0,
			  UNSUITED = 0, SUITED_WITH_FACE_UP = 1, SUITED_WITH_DISCARD = 2;
	int[][][][] heldVisits = new int[2][2][Card.NUM_RANKS][Card.NUM_RANKS];
	// indexed by drawFaceUp (0/1), discardSuited (0/1), rank of face-up, rank of discard
	int[][][][][][] heldCounts = new int[2][2][Card.NUM_RANKS][Card.NUM_RANKS][3][Card.NUM_RANKS];
	// indexed by drawFaceUp (0/1), discardSuited (0/1), rank face up, rank discard, 
	// suited (0=unsuited with either, 1=suited with face-up, 2=suited with discard), rank 

	int[] rankCounts = new int[Card.NUM_RANKS];
	int faceDownDrawCount = 0;
	int immediateDiscardCount = 0;
	
	boolean[] known0 = new boolean[Card.NUM_CARDS];
	public double[] prob0 = new double[Card.NUM_CARDS];
	int numUnknownInHand = HAND_SIZE;
	ArrayList<Card> estHand1;
	private int Q = 10;
	
	/**
	 * Constructor of the estimator that take a set (file) of simulated data as input
	 * @param filename which holds the simulated data
	 */
	public HandEstimator_ver2(String filename) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			rankCounts = (int[]) in.readObject();
			heldVisits = (int[][][][]) in.readObject();
			heldCounts = (int[][][][][][]) in.readObject();
			faceDownDrawCount = in.readInt();
			immediateDiscardCount = in.readInt();
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * By default, the HandEstimator constructor take a set of 10000 simulated game
	 */
	public HandEstimator_ver2() {
		this("handEstimator/handEst1-10000.dat");
	}
	
	/**
	 * Initially, player 0 does not know for sure which cards are in player 1 hand for sure.
	 * In other words, there are 0 known card and there are 10 unknown cards. 
	 * Thus, the probability of each card in 52 cards to be in player 1 hand is 10/52
	 */
	public void init() {
		known0 = new boolean[Card.NUM_CARDS];
		prob0 = new double[Card.NUM_CARDS];
		int numUnknown = 0;
		for (int i = 0; i < prob0.length; i++)
			if (!known0[i]) numUnknown++;
		for (int i = 0; i < prob0.length; i++)
			prob0[i] = (double) numUnknownInHand / numUnknown;
		getEstimatedHand();
	}
	 
	/**
	 * Save simulated data to a file
	 * @param filename
	 */
	public void save(String filename) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(rankCounts);
			out.writeObject(heldVisits);
			out.writeObject(heldCounts);
			out.writeInt(faceDownDrawCount);
			out.writeInt(immediateDiscardCount);
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print out the probability each in 52 cards to be in player 1's hand.
	 */
	public void print() {		
		double sum = 0;
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (!known0[i]) sum = sum + prob0[i];	
		}
		
		System.out.print("Rank");
		for (int i = 0; i < Card.NUM_RANKS; i++)
			System.out.print("\t" + Card.rankNames[i]);
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (i % Card.NUM_RANKS == 0)
				System.out.printf("\n%s", Card.suitNames[i / Card.NUM_RANKS]);
			System.out.print("\t");	
			
			if (known0[i]) System.out.print(prob0[i] == 1 ? "*TRUE*" : "FALSE");
			else System.out.printf("%.4f", prob0[i]/sum);	
		}		
	}
	
	/**
	 * As the game progressing, player 0 knows more about where each of 52 cards is. Everytime player 0
	 * knows a card location, he needs to update the known0 array. Note: The fact that player 0 knows 
	 * where a card is is different than knowing if it is in player 1's hand. He might know that the card
	 * is in the face-up pile. 
	 * @param card
	 * @param held
	 */
	public void setKnown(Card card, boolean held) {
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.add(card);
		setKnown(cards, held);
	}

	/**
	 * Set a set of cards in opponent hand to known0. In other words, opponent does hold all of these cards
	 * @param cards
	 * @param held
	 */
	public void setKnown(ArrayList<Card> cards, boolean held) {
		for (Card card : cards) {
			known0[card.getId()] = true;
			prob0[card.getId()] = held ? 1 : 0;
		}
		renormalize();
		computeProb();
	}
	
	/**
	 * Set number Q used for retrieving estimated opponent's hand
	 * @param Q
	 * */
	public void setQ(int Q)
	{
		this.Q = Q;
	}
	
	/**
	 * Update the probability (belief) base on 3 new evidents using Bayes theorem:
	 * - What was the faceup card to the opponent
	 * - If the opponent has just drawn faceup
	 * - What card the opponent has just discarded
	 * @param faceUpCard
	 * @param faceUpDrawn
	 * @param discardCard
	 */
	public void reportDrawDiscard(Card faceUp1, boolean drawnFaceUp1, Card discarded1) {
		known0[faceUp1.getId()] = true;
		prob0[faceUp1.getId()] = drawnFaceUp1 ? 1 : 0;
		known0[discarded1.getId()] = true;
		prob0[discarded1.getId()] = 0;
		int faceUpRank = faceUp1.rank;
		int drawFaceUpIndex = drawnFaceUp1 ? 1 : 0;
		int discardRank = discarded1.rank;
		int discardSuitedIndex = (discarded1.suit == faceUp1.suit) ? 1 : 0;
		if (heldVisits[drawFaceUpIndex][discardSuitedIndex][faceUpRank][discardRank] >= MIN_VISITS) {
			for (int i = 0; i < Card.NUM_CARDS; i++) {
				if (known0[i]) continue;
				Card c = Card.getCard(i);
				int suitedIndex = UNSUITED;
				if (c.suit == faceUp1.suit)
					suitedIndex = SUITED_WITH_FACE_UP;
				else if (c.suit == discarded1.suit)
					suitedIndex = SUITED_WITH_DISCARD;
				prob0[i] *= heldCounts[drawFaceUpIndex][discardSuitedIndex][faceUpRank][discardRank][suitedIndex][c.rank];
				if (suitedIndex == UNSUITED)
					prob0[i] /= (discardSuitedIndex == 1) ? 3 : 2;
				prob0[i] /= rankCounts[c.rank];
			}
		}
		renormalize();
		computeProb();
	}
	
	/**
	 * Estimate opponent hand with the given probability of each card in the opponent hand.
	 * If there are n known0 cards in opponent hands, we need to estimate 10-n cards. 
	 * List A = 10-n highest probability in from a list of the current update probability of each card in opponent hand.
	 * From list A, if we can map b cards whose probability of being in opponent hand is in list A, where 10-n < b < q,
	 * we are comfortable to say that these b cards are likely to be in opponent hand. 
	 * @param q
	 * @return
	 */
	public ArrayList<Card> getEstimatedHand()
	{
		ArrayList<Card> hand = new ArrayList<Card>();
		ArrayList<ArrayList<Double>> temp = new ArrayList<ArrayList<Double>>();
		
		for (int id = 0; id < 52; id++)
			if (prob0[id] == 1)
				hand.add(Card.strCardMap.get(Card.idStrMap.get(id)));
			else if (!known0[id])
				temp.add(new ArrayList<Double>(Arrays.asList(prob0[id], (double) id)));
		
		int theRest = 10 - hand.size(); // the left number of unknown cards 
		Collections.sort(temp, new Comparator<ArrayList<Double>>()
				{
					@Override
					public int compare(ArrayList<Double> a, ArrayList<Double> b) {
						if (a.get(0) < b.get(0)) 
							return 1;
						else if (a.get(0) > b.get(0))
							return -1;
						return 0;
					}
				});
		
		ArrayList<Integer> uniqueID = new ArrayList<Integer>();
		for (int i = 0; i < temp.size(); i++)
		{
			if (i > 0 && temp.get(i).get(0) != temp.get(i-1).get(0))
			{
				uniqueID.add((int)Math.floor(temp.get(i-1).get(1)));
				theRest--;
				if (theRest == 0)
				{
					if (uniqueID.size() <= Q)
						for (int id: uniqueID)
							hand.add(Card.strCardMap.get(Card.idStrMap.get(id)));
					break;
				}
			}
		}
		estHand1 = hand;
		return hand;
	}
	
	/**
	 * Given an estimated hand of the opponent, compute the estimated hit-score cards of opponent
	 * @return the estimated list of opponent's hit-score card
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Card> getEstimatedHitScoreCards()
	{
//		System.out.println("Estimated Opponent's Hand: " + this.estHand1);
		boolean[] c = new boolean[52];
		for (Card card: estHand1)
			c[card.getId()] = true;
		
		ArrayList<Card> hitScoreCards = new ArrayList<Card>();
		
		for (int id = 0; id < 52; id++)
			if (!c[id])
			{
				ArrayList<Card> unmeldedCards = (ArrayList<Card>) estHand1.clone();
				ArrayList<ArrayList<ArrayList<Card>>> meldsSets = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
				if (meldsSets.size() != 0)
				{
					ArrayList<ArrayList<Card>> melds = meldsSets.get(0);
					for (ArrayList<Card> meld : melds)
						for (Card card : meld)
							unmeldedCards.remove(card);
					
				}
				int countPreviousUnmeldedCards = unmeldedCards.size();
				
				Card newCard = Card.strCardMap.get(Card.idStrMap.get(id));
				
				unmeldedCards = (ArrayList<Card>) estHand1.clone();
				unmeldedCards.add(newCard);
				meldsSets = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
				if (meldsSets.size() != 0)
				{
					ArrayList<ArrayList<Card>> melds = meldsSets.get(0);
					for (ArrayList<Card> meld : melds)
						for (Card card : meld)
							unmeldedCards.remove(card);
				}
				
				if (countPreviousUnmeldedCards >= unmeldedCards.size())
					hitScoreCards.add(newCard);
			}
		
		return hitScoreCards; // number of hitscore card chosen by prob0
	}
	
	/**
	 * Normalize the hand given the new number of unknown card in hand
	 */
	public void renormalize() {
		numUnknownInHand = HAND_SIZE;
		double sumProb = 0;
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (!known0[i])
				sumProb += prob0[i];
 			else if (prob0[i] == 1) {
				numUnknownInHand--;
			}
		}
		double normalizeFactor = numUnknownInHand / sumProb;
		for (int i = 0; i < Card.NUM_CARDS; i++)
			if (!known0[i])
				prob0[i] *= normalizeFactor;
	}
	
	/**
	 * Force the relative likelyhood between 0 and 1 by compute dividing by total relative likelihood. 
	 * This is probably wrong, as the sum prob need to be 10. 
	 */
	public void computeProb()
	{
		double sum = 0;
		for (int i = 0; i < Card.NUM_CARDS; i++)
			if (!known0[i])
				sum = sum + prob0[i];
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (!known0[i])
				prob0[i] = prob0[i]/sum;
		}
	}
	
	/**
	 * Test by initialize the probability of every card being in the opponent hand without any further information. 
	 * Here probability is 10/52
	 */
	public void test() {
		init();
		print();
	}

	public static void main(String[] args) {
		new HandEstimator_ver2().test();
	}
}