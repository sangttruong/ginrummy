package ginrummy;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.OpenOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Constants and utilities for Gin Rummy.  Meld checking makes use of bitstring representations
 * where a single long value represents a set of cards.  Each card has an id number i in the range 0-51, and the
 * presence (1) or absense (0) of that card is represented at bit i (the 2^i place in binary).
 * This allows fast set difference/intersection/equivalence/etc. operations with bitwise operators.
 *
 * Gin Rummy Rules: https://www.pagat.com/rummy/ginrummy.html
 * Adopted variant: North American scoring (25 point bonus for undercut, 25 point bonus for going gin)
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

@SuppressWarnings("unchecked")
public class GinRummyUtil {
	/**
	 * Goal score
	 */
	public static final int GOAL_SCORE = 100;
	/**
	 * Bonus for melding all cards before knocking
	 */
	public static final int GIN_BONUS = 25;
	/**
	 * Bonus for undercutting (having less than or equal the deadwood of knocking opponent)
	 */
	public static final int UNDERCUT_BONUS = 25;
	/**
	 * Maximum deadwood points permitted for knocking
	 */
	public static final int MAX_DEADWOOD = 10;
	/**
	 * Deadwood points indexed by card rank
	 */
	private static final int[] DEADWOOD_POINTS = new int[Card.NUM_RANKS];
	/**
	 * Card bitstrings indexed by card id number
	 */
	private static long[] cardBitstrings = new long[Card.NUM_CARDS];
	/**
	 * List of lists of meld bitstrings.  Melds appearing after melds in lists are supersets, so the
	 * first meld not made in a list makes further checking in that list unnnecessary.
	 */
	private static ArrayList<ArrayList<Long>> meldBitstrings;
	/**
	 * Map from meld bitstrings to corresponding lists of cards
	 */
	private static HashMap<Long, ArrayList<Card>> meldBitstringToCardsMap;

	static {
		// initialize DEADWOOD_POINTS
		for (int rank = 0; rank < Card.NUM_RANKS; rank++)
			DEADWOOD_POINTS[rank] = Math.min(rank + 1, 10);

		// initialize cardBitStrings
		long bitstring = 1L;
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			cardBitstrings[i] = bitstring;
			bitstring <<= 1;
		}

		// build list of lists of meld bitstring where each subsequent meld bitstring in the list is a superset of previous meld bitstrings
		meldBitstrings = new ArrayList<ArrayList<Long>>();
		meldBitstringToCardsMap = new HashMap<Long, ArrayList<Card>>();

		// build run meld lists
		for (int suit = 0; suit < Card.NUM_SUITS; suit++) {
			for (int runRankStart = 0; runRankStart < Card.NUM_RANKS - 2; runRankStart++) {
				ArrayList<Long> bitstringList = new ArrayList<Long>();
				ArrayList<Card> cards = new ArrayList<Card>();
				Card c = Card.getCard(runRankStart, suit);
				cards.add(c);
				long meldBitstring = cardBitstrings[c.getId()];
				c = Card.getCard(runRankStart + 1, suit);
				cards.add(c);
				meldBitstring |= cardBitstrings[c.getId()];
				for (int rank = runRankStart + 2; rank < Card.NUM_RANKS; rank++) {
					c = Card.getCard(rank, suit);
					cards.add(c);
					meldBitstring |= cardBitstrings[c.getId()];
					bitstringList.add(meldBitstring);
					meldBitstringToCardsMap.put(meldBitstring, (ArrayList<Card>) cards.clone());
				}
				meldBitstrings.add(bitstringList);
			}
		}

		// build set meld lists
		for (int rank = 0; rank < Card.NUM_RANKS; rank++) {
			ArrayList<Card> cards = new ArrayList<Card>();
			for (int suit = 0; suit < Card.NUM_SUITS; suit++)
				cards.add(Card.getCard(rank,  suit));
			for (int suit = 0; suit <= Card.NUM_SUITS; suit++) {
				ArrayList<Card> cardSet = (ArrayList<Card>) cards.clone();
				if (suit < Card.NUM_SUITS)
					cardSet.remove(Card.getCard(rank,  suit));
				ArrayList<Long> bitstringList = new ArrayList<Long>();
				long meldBitstring = 0L;
				for (Card card : cardSet)
					meldBitstring |= cardBitstrings[card.getId()];
				bitstringList.add(meldBitstring);
				meldBitstringToCardsMap.put(meldBitstring, cardSet);
				meldBitstrings.add(bitstringList);
			}
		}
	}

	/**
	 * Given card set bitstring, return the corresponding list of cards
	 * @param bitstring card set bitstring
	 * @return the corresponding list of cards
	 */
	public static ArrayList<Card> bitstringToCards(Long bitstring) {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (bitstring % 2 == 1)
				cards.add(Card.allCards[i]);
			bitstring /= 2;
		}
		return cards;
	}

	/**
	 * Given a list of cards, return the corresponding card set bitstring
	 * @param cards a list of cards
	 * @return the corresponding card set bitstring
	 */
	public static long cardsToBitstring(ArrayList<Card> cards) {
		long bitstring = 0L;
		for (Card card : cards)
			bitstring |= cardBitstrings[card.getId()];
		return bitstring;
	}

	/**
	 * Given a list of cards, return a list of all meld bitstrings that apply to that list of cards
	 * @param cards a list of cards
	 * @return a list of all meld bitstrings that apply to that list of cards
	 */
	public static ArrayList<Long> cardsToAllMeldBitstrings(ArrayList<Card> cards) {
		ArrayList<Long> bitstringList = new ArrayList<Long>();
		long cardsBitstring = cardsToBitstring(cards);
		for (ArrayList<Long> meldBitstringList : meldBitstrings)
			for (long meldBitstring : meldBitstringList)
				if ((meldBitstring & cardsBitstring) == meldBitstring)
					bitstringList.add(meldBitstring);
				else
					break;
		return bitstringList;
	}

	/**
	 * Given a list of cards, return a list of all lists of card melds that apply to that list of cards
	 * @param cards a list of cards
	 * @return a list of all lists of card melds that apply to that list of cards
	 */
	public static ArrayList<ArrayList<Card>> cardsToAllMelds(ArrayList<Card> cards) {
		ArrayList<ArrayList<Card>> meldList = new ArrayList<ArrayList<Card>>();
		for (long meldBitstring : cardsToAllMeldBitstrings(cards))
			meldList.add(bitstringToCards(meldBitstring));
		return meldList;
	}

	/**
	 * Given a list of cards, return a list of all card melds lists to which another meld cannot be added.
	 * This corresponds to all ways one may maximally meld, although this doesn't imply minimum deadwood/cards in the sets of melds.
	 * @param cards a list of cards
	 * @return a list of all card melds lists to which another meld cannot be added
	 */
	public static ArrayList<ArrayList<ArrayList<Card>>> cardsToAllMaximalMeldSets(ArrayList<Card> cards) {
		ArrayList<ArrayList<ArrayList<Card>>> maximalMeldSets = new ArrayList<ArrayList<ArrayList<Card>>>();
		ArrayList<Long> meldBitstrings = cardsToAllMeldBitstrings(cards);
		HashSet<HashSet<Integer>> closed = new HashSet<HashSet<Integer>>();
		Queue<HashSet<Integer>> queue = new LinkedList<HashSet<Integer>>();
		HashSet<Integer> allIndices = new HashSet<Integer>();
		for (int i = 0; i < meldBitstrings.size(); i++) {
			HashSet<Integer> meldIndexSet = new HashSet<Integer>();
			meldIndexSet.add(i);
			allIndices.add(i);
			queue.add(meldIndexSet);
		}
		while (!queue.isEmpty()) {
			HashSet<Integer> meldIndexSet = queue.poll();
//			System.out.println(meldSet);
			if (closed.contains(meldIndexSet))
				continue;
			long meldSetBitstring = 0L;
			for (int meldIndex : meldIndexSet)
				meldSetBitstring |= meldBitstrings.get(meldIndex);
			closed.add(meldIndexSet);
			boolean isMaximal = true;
			for (int i = 0; i < meldBitstrings.size(); i++) {
				if (meldIndexSet.contains(i))
					continue;
				long meldBitstring = meldBitstrings.get(i);
				if ((meldSetBitstring & meldBitstring) == 0) { // meld has no overlap with melds in set
					isMaximal = false;
					HashSet<Integer> newMeldIndexSet = (HashSet<Integer>) meldIndexSet.clone();
					newMeldIndexSet.add(i);
					queue.add(newMeldIndexSet);
				}
			}
			if (isMaximal) {
				ArrayList<ArrayList<Card>> cardSets = new ArrayList<ArrayList<Card>>();
				for (int meldIndex : meldIndexSet) {
					long meldBitstring  = meldBitstrings.get(meldIndex);
					cardSets.add(bitstringToCards(meldBitstring));
				}
				maximalMeldSets.add(cardSets);
			}
		}
		return maximalMeldSets;
	}

	/**
	 * Given a list of card melds and a hand of cards, return the unmelded deadwood points for that hand
	 * @param melds a list of card melds
	 * @param hand hand of cards
	 * @return the unmelded deadwood points for that hand
	 */
	public static int getDeadwoodPoints(ArrayList<ArrayList<Card>> melds, ArrayList<Card> hand) {
		HashSet<Card> melded = new HashSet<Card>();
		for (ArrayList<Card> meld : melds)
			for (Card card : meld)
				melded.add(card);
		int deadwoodPoints = 0;
		for (Card card : hand)
			if (!melded.contains(card))
				deadwoodPoints += DEADWOOD_POINTS[card.rank];
		return deadwoodPoints;
	}

	/**
	 * Return the deadwood points for an individual given card.
	 * @param card given card
	 * @return the deadwood points for an individual given card
	 */
	public static int getDeadwoodPoints(Card card) {
		return DEADWOOD_POINTS[card.rank];
	}

	/**
	 * Return the deadwood points for a list of given cards.
	 * @param cards list of given cards
	 * @return the deadwood points for a list of given cards
	 */
	public static int getDeadwoodPoints(ArrayList<Card> cards) {
		int deadwood = 0;
		for (Card card : cards)
			deadwood += DEADWOOD_POINTS[card.rank];
		return deadwood;
	}

	/**
	 * Returns a list of list of melds that all leave a minimal deadwood count.
	 * @param cards
	 * @return a list of list of melds that all leave a minimal deadwood count
	 */
	// Note: This is actually a "weighted maximum coverage problem". See https://en.wikipedia.org/wiki/Maximum_coverage_problem
	public static ArrayList<ArrayList<ArrayList<Card>>> cardsToBestMeldSets(ArrayList<Card> cards) {
		int minDeadwoodPoints = Integer.MAX_VALUE;
		ArrayList<ArrayList<ArrayList<Card>>> maximalMeldSets = cardsToAllMaximalMeldSets(cards);
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = new ArrayList<ArrayList<ArrayList<Card>>>();
		for (ArrayList<ArrayList<Card>> melds : maximalMeldSets) {
			int deadwoodPoints = getDeadwoodPoints(melds, cards);
			if (deadwoodPoints <= minDeadwoodPoints) {
				if (deadwoodPoints < minDeadwoodPoints) {
					minDeadwoodPoints = deadwoodPoints;
					bestMeldSets.clear();
				}
				bestMeldSets.add(melds);
			}
		}
		return bestMeldSets;
	}

	/**
	 * Return all meld bitstrings.
	 * @return all meld bitstrings
	 */
	public static Set<Long> getAllMeldBitstrings() {
		return meldBitstringToCardsMap.keySet();
	}

	/**
	 * Represents a class resource which is rooted at the directory associated
	 * with a specific class loader.
	 */
	public static class FileResource {
		private final Path path;

		/**
		 * Generate a path resource which is rooted at the directory associated
		 * with the specified class' class loader with the specified file name.
		 * That name may specify any relative path (using unix conventions); the
		 * file handing code will convert the generated paths to the correct
		 * syntax for the target system.
		 *
		 * @param cls the class from which to obtain the class loader directory
		 * @param name a file specification relative to the class loader
		 */
		public FileResource(Class cls, String name) {
			try {
				this.path = Paths.get(cls.getResource("/").toURI()).resolve(name);
			}
			catch(URISyntaxException exception) {
				/* I can not imagine a situation wherein the URL provided by
				 * the core java method Class<>.getResource(String) would not
				 * be strictly compliant with the URL syntax specification of
				 * RFC2396. Therefore we circumvent the checked exception
				 * URISyntaxException and replace it with an error condition.
				 */
				throw new Error("For some reason the class loader's URL is not formatted strictly according to to RFC2396 and cannot be converted to a URI", exception);
			}
		}

		/**
		 * Query the existance of this file resource.
		 */
		 public boolean exists() {
			 return Files.exists(this.path);
		 }

		/**
		 * Open a new InputStream to the resource. This input stream can be
		 * used in other file parsing classes such as Scanner to facilitate
		 * file reading.
		 *
		 * @param options options for opening the file (see StandardOpenOption)
		 */
		public InputStream asInputStream(OpenOption... options) throws IOException {
			return Files.newInputStream(this.path, options);
		}

		/**
		 * Open a new OutputStream to the resource. This output stream can be
		 * used in other file parsing classes such as PrintWriter to facilitate
		 * file writing.
		 *
		 * @param options options for opening the file (see StandardOpenOption)
		 */
		public OutputStream asOutputStream(OpenOption... options) throws IOException {
			return Files.newOutputStream(this.path, options);
		}
	}

	/**
	 * Test GinRummyUtils for a given list of cards specified in the first line.
	 * @param args (unused)
	 */
	public static void main(String[] args) {
		String cardNames = "AD AS AH AC 2C 3C 4C 4H 4D 4S"; // adding these (impossible in Gin Rummy) causes great combinatorial complexity: 3S 5S 6S 7S 7D 7C 7H 8H 9H TH TC TS TD 9D JD QD KD KS KH KC";
//		String cardNames = "AC AH AS 2C 2H 2S 3C 3H 3S KD";
//		String cardNames = "AC AH AS 2C 2H 2S 3C 3H 3S 4H";

		String[] cardNameArr = cardNames.split(" ");
		ArrayList<Card> cards = new ArrayList<Card>();
		for (String cardName : cardNameArr)
			cards.add(Card.strCardMap.get(cardName));
		System.out.println("Hand: " + cards);
		System.out.println("Bitstring representation as long: " + cardsToBitstring(cards));
		System.out.println("All melds:");
		for (ArrayList<Card> meld : cardsToAllMelds(cards))
			System.out.println(meld);
		System.out.println("Maximal meld sets:");
		for (ArrayList<ArrayList<Card>> meldSet : cardsToAllMaximalMeldSets(cards))
			System.out.println(meldSet);
		System.out.println("Best meld sets:");
		for (ArrayList<ArrayList<Card>> meldSet : cardsToBestMeldSets(cards))
			System.out.println(getDeadwoodPoints(meldSet, cards) + ":" + meldSet);
	}

}
