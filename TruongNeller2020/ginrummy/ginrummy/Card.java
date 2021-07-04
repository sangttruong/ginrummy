package ginrummy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;


/**
 * A class for representing standard (French) playing cards.
 * Rank numbers are 0 through 12, corresponding to possible rank names: "A", "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K".
 * Suit numbers are 0 through 3, corresponding to possible suit names: "C", "H", "S", "D".  Note that suit colors alternate.
 * The toString method for each card will be the concatenation of a rank name with a suit name.
 * 
 * It's possible to go between 4 different card representations using this class:
 * (1) Card object representation
 * (2) String representation
 * (3) single integer representation (0 - 51)
 * (4) two integer (rank, suit) representation
 * 
 * _Avoid the construction of new Card objects._  Use the Card objects already created in allCards, retrieving them via 
 * (1) static HashMap&lt;String, Card%gt; strCardMap using the method get(String),
 * (2) static Card getCard(int), or
 * (3) static Card getCard(int rank, int suit). 
 * 
 * @author Todd W. Neller
 * @version 1.0

Copyright (C) 2020 Todd Neller

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Information about the GNU General Public License is available online at:
  http://www.gnu.org/licenses/
To receive a copy of the GNU General Public License, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
02111-1307, USA.

 */
public class Card {

	/**
	 * an array of all unique Card objects
	 */
	public static Card[] allCards;
	
	/**
	 * array of abbreviated card rank names in ascending order of rank and indexed by suit index
	 */
	public static String[] rankNames = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K"};

	/**
	 * array of abbreviated card suit names indexed by suit index
	 */
	public static String[] suitNames = {"C", "H", "S", "D"}; 

	/**
	 * parallel array to suitNames indicating whether or not the corresponding suit is red
	 */
	public static boolean[] isSuitRed = {false, true, false, true}; 

	/**
	 * number of card ranks
	 */
	public static final int NUM_RANKS = rankNames.length;
	
	/**
	 * number of card suits
	 */
	public static final int NUM_SUITS = suitNames.length;
	
	/**
	 * total number of cards
	 */
	public static final int NUM_CARDS = NUM_RANKS * NUM_SUITS;
	
	/**
	 * map from String representations to Card objects
	 */
	public static HashMap<String, Card> strCardMap = new HashMap<String, Card>();
	
	/**
	 * map from String representations to Card id numbers
	 */
	public static HashMap<String, Integer> strIdMap = new HashMap<String, Integer>();
	
	/**
	 * map from Card id numbers to String representations
	 */
	public static HashMap<Integer, String> idStrMap = new HashMap<Integer, String>();

	static {
		// Create all cards and initialize static maps
		allCards = new Card[NUM_SUITS * NUM_RANKS];
		int i = 0;
		for (int suit = 0; suit < NUM_SUITS; suit++) 
			for (int rank = 0; rank < NUM_RANKS; rank++) {
				Card c = new Card(rank, suit);
				allCards[i] = c;
				strCardMap.put(c.toString(), c);
				strIdMap.put(c.toString(), c.getId());
				idStrMap.put(c.getId(), c.toString());
				i++;
			}
	}

	/**
	 * Get the Card object corresponding to the given Card id number (0 - 51)
	 * @param id Card id number (0 - 51)
	 * @return corresponding Card object
	 */
	public static Card getCard(int id) {
		return allCards[id];
	}
	
	/**
	 * Get the Card object corresponding to the given rank and suit indices
	 * @param rank rank index
	 * @param suit suit index
	 * @return corresponding Card object
	 */
	public static Card getCard(int rank, int suit) {
		return allCards[suit * NUM_RANKS + rank];
	}
	
	/**
	 * Get the Card id number corresponding to the given rank and suit indices
	 * @param rank rank index
	 * @param suit suit index
	 * @return corresponding Card id number
	 */
	public static int getId(int rank, int suit) {
		return suit * NUM_RANKS + rank;
	}

	/**
	 * Return a Stack deck of Cards corresponding to the given shuffle seed number
	 * @param seed shuffle seed number
	 * @return corresponding Stack deck of Cards
	 */
	public static Stack<Card> getShuffle(int seed) {
		Stack<Card> deck = new Stack<Card>();
		for (int i = 0; i < NUM_CARDS; i++)
			deck.push(Card.allCards[i]);
		Collections.shuffle(deck, new java.util.Random(seed));
		return deck;
	}

	/**
	 * rank index (zero-based index to rankNames)
	 */
	public final int rank;
		
	/**
	 * suit index (zero-based index to suitNames)
	 */
	public final int suit;

	/**
	 * Constructor to create a card object with the corresponding zero-based indices to rankNames and suitNames, respectively.
	 * AVOID USE IF POSSIBLE.  Use the Card objects already created in allCards, retrieving them via 
	 * (1) static HashMap&lt;String, Card&gt; strCardMap using the method get(String),
	 * (2) static Card getCard(int), or
	 * (3) static Card getCard(int rank, int suit). 
	 * @param rank rank of card (zero-based index to rankNames)
	 * @param suit suit of card (zero-based index to suitNames)
	 */
	public Card(int rank, int suit) {
		this.rank = rank;
		this.suit = suit;
	}

	/**
	 * Get rank of card (zero-based index to rankNames).
	 * @return rank of card (zero-based index to rankNames)
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Get suit of card (zero-based index to suitNames).
	 * @return suit of card (zero-based index to suitNames)
	 */
	public int getSuit() {
		return suit;
	}
	
	/**
	 * Return whether or not the card is Red.
	 * @return whether or not the card is Red
	 */
	public boolean isRed() {
		return suit % 2 == 1;
	}
	
	/**
	 * Return the Card id number.
	 * @return the Card id number
	 */
	public int getId() {
		return suit * NUM_RANKS + rank;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public java.lang.String toString() {
		return rankNames[rank] + suitNames[suit];
	}
	
	/**
	 * A test to show shuffle seed 617.
	 * @param args (not used)
	 */
	public static void main(String[] args) {
		Stack<Card> deck = getShuffle(617);
		System.out.println(deck);
		for (int i = 0; i < NUM_CARDS; i++)
			System.out.print(deck.pop() + (i % 8 == 7 ? "\n" : " "));
		System.out.println();
	}
}
