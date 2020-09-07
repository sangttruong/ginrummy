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
public class HandEstimator {
	private static final int HAND_SIZE = 10;
	private static final int MIN_VISITS = 50;
	private static final double EPS = .1 / MIN_VISITS; // to make non-events rare but not impossible
	int[] rankCounts = new int[Card.NUM_RANKS];
	int[][][][] heldVisits = new int[2][2][Card.NUM_RANKS][Card.NUM_RANKS]; // indexed by drawFaceUp (0/1), discardSuited (0/1), 
	int[][][][][][] heldCounts = new int[2][2][Card.NUM_RANKS][Card.NUM_RANKS][3][Card.NUM_RANKS]; // indexed by drawFaceUp (0/1), discardSuited (0/1), rank face up, rank discard, suited (0=unsuited with either, 1=suited with face-up, 2=suited with discard), rank 
	final int DRAW_FACE_UP = 1, DRAW_FACE_DOWN = 0, UNSUITED = 0, SUITED_WITH_FACE_UP = 1, SUITED_WITH_DISCARD = 2;
	int faceDownDrawCount = 0;
	int immediateDiscardCount = 0;
	
	boolean[] known = new boolean[Card.NUM_CARDS];
	double[] prob = new double[Card.NUM_CARDS];
	int numUnknownInHand = HAND_SIZE;
	public ArrayList<Card> estimatedHand;
	
//	public HandEstimator(GinRummyDataCollector collector) {
//		rankCounts = collector.rankCounts;
//		heldVisits = collector.heldVisits;
//		heldCounts = collector.heldCounts;
//		faceDownDrawCount = collector.faceDownDrawCount;
//		immediateDiscardCount = collector.immediateDiscardCount;
//	}
	
	public HandEstimator(String filename) {
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

	public HandEstimator() {
		this("handEst1-10000.dat");
	}
	
	public void init() {
		known = new boolean[Card.NUM_CARDS];
		prob = new double[Card.NUM_CARDS];
		int numUnknown = 0;
		for (int i = 0; i < prob.length; i++)
			if (!known[i])
				numUnknown++;
		for (int i = 0; i < prob.length; i++)
			prob[i] = (double) numUnknownInHand / numUnknown;
		
		estimatedHand = getEstimatedHand();
	}
	
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

	public void print() {
		System.out.print("Rank");
		for (int i = 0; i < Card.NUM_RANKS; i++)
			System.out.print("\t" + Card.rankNames[i]);
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (i % Card.NUM_RANKS == 0)
				System.out.printf("\n%s", Card.suitNames[i / Card.NUM_RANKS]);
			System.out.print("\t");
			if (known[i])
				System.out.print(prob[i] == 1 ? "*TRUE*" : "FALSE");
			else
				System.out.printf("%.4f", prob[i]);
		}
		System.out.println();
	}
	
	public void setKnown(Card card, boolean held) {
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.add(card);
		setKnown(cards, held);
	}
	
	public void setKnown(ArrayList<Card> cards, boolean held) {
		for (Card card : cards) {
			known[card.getId()] = true;
			prob[card.getId()] = held ? 1 : 0;
		}
		renormalize();
	}

	public ArrayList<Card> getEstimatedHand()
	{
		ArrayList<Card> hand = new ArrayList<Card>();
		ArrayList<ArrayList<Double>> temp = new ArrayList<ArrayList<Double>>();
		
		for (int id = 0; id < 52; id++)
			if (prob[id] == 1)
				hand.add(Card.strCardMap.get(Card.idStrMap.get(id)));
			else if (!known[id])
				temp.add(new ArrayList<Double>(Arrays.asList(prob[id], (double) id)));
		
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
					for (int id: uniqueID)
						hand.add(Card.strCardMap.get(Card.idStrMap.get(id)));
					break;
				}
			}
		}
		estimatedHand = hand;
		return hand;
	}
	
	public void reportDrawDiscard(Card faceUpCard, boolean faceUpDrawn, Card discardCard) {
		// System.out.printf("*** %s %s %s\n", faceUpCard, faceUpDrawn, discardCard);
		known[faceUpCard.getId()] = true;
		prob[faceUpCard.getId()] = faceUpDrawn ? 1 : 0;
		known[discardCard.getId()] = true;
		prob[discardCard.getId()] = 0;
		int faceUpRank = faceUpCard.rank;
		int drawFaceUpIndex = faceUpDrawn ? 1 : 0;
		int discardRank = discardCard.rank;
		int discardSuitedIndex = (discardCard.suit == faceUpCard.suit) ? 1 : 0;
		if (heldVisits[drawFaceUpIndex][discardSuitedIndex][faceUpRank][discardRank] >= MIN_VISITS) {
			for (int i = 0; i < Card.NUM_CARDS; i++) {
				if (known[i]) continue;
				Card c = Card.getCard(i);
				int suitedIndex = UNSUITED;
				if (c.suit == faceUpCard.suit)
					suitedIndex = SUITED_WITH_FACE_UP;
				else if (c.suit == discardCard.suit)
					suitedIndex = SUITED_WITH_DISCARD;
				prob[i] *= heldCounts[drawFaceUpIndex][discardSuitedIndex][faceUpRank][discardRank][suitedIndex][c.rank];
				if (suitedIndex == UNSUITED)
					prob[i] /= (discardSuitedIndex == 1) ? 3 : 2;
				prob[i] /= rankCounts[c.rank];
			}
		}
		renormalize();
	}
	
	public void renormalize() {
		numUnknownInHand = HAND_SIZE;
		double sumProb = 0;
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (!known[i])
				sumProb += prob[i];
 			else if (prob[i] == 1) {
				numUnknownInHand--;
			}
		}
		double normalizeFactor = numUnknownInHand / sumProb;
		for (int i = 0; i < Card.NUM_CARDS; i++)
			if (!known[i])
				prob[i] *= normalizeFactor;
	}
	
	public void test() {
		init();
		print();
	}

	public static void main(String[] args) {
		new HandEstimator().test();
	}

}
