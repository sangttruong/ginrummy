import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/** 
 * @author Sang T. Truong and Hoang H. Pham
 * @version 1.0

FeatureEngineer0 is used to collect all feature that belong to player 0.
These features are:
- Run (highest priority)
- Set 
- TwoSet
- TwoRun
- ThreeRun
- Triangle
- Hit-score
- Deadwood 

Functions in this class include counter and getter. 
 * 
 */

public class FeatureEngineer0 {
	private static final int[] SCORE = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};
	
	public static ArrayList<Double> getFeatures(GameState gs, double k) {
		ArrayList<Card> cards = GinRummyUtil.bitstringToCards(gs.getHand0());
		double[] prob0 = gs.getProb0();
		
		ArrayList<Double> features = new ArrayList<Double>();
		ArrayList<Object> temp; 
		boolean[][] choose = new boolean[4][13];
		double totalScore = 0;
		for (Card card: cards)
			totalScore += (double) SCORE[card.getRank()];
		
		double deadWood = totalScore;
		temp = run(cards, choose);
		features.add((double)(Integer) temp.get(0)); // countRun
		deadWood -= (double)(Integer) temp.get(1); // scoreRun
		
		temp = set(cards, choose);
		features.add((double)(Integer) temp.get(0)); // countSet
		deadWood -= (double)(Integer) temp.get(1); // scoreSet
		
		temp = twoSet(cards, choose);
		choose =  clone((boolean[][]) temp.get(2)); // choose array after passing through run and set
		features.add((double)(Integer) temp.get(0));
		
		choose = clone((boolean[][]) temp.get(2)); // choose array after passing through run and set
		features.add((double) twoRun(cards, choose));

		choose = clone((boolean[][]) temp.get(2)); // choose array after passing through run and set
		features.add((double) threeRun(cards, choose));
		
		choose = clone((boolean[][]) temp.get(2)); // choose array after passing through run and set
		features.add((double) triangle(cards, choose));
		
		features.add((double) hitScore(cards, prob0, k));
		
		features.add(deadWood);
		
		return features;
	} 
	
	/**
	 * Count the number of sets in a given hand
	 * @param an Array List of Card that is needed to be evaluated and an a chosen matrix.
	 * @return number of set, the SCORE of the set, and the chosen matrix. 
	 */
	public static ArrayList<Object> set(ArrayList<Card> hand0, boolean[][] chosen)
	{
		boolean[][] c = convertTo2DBooleanArray(hand0);
		int count = 0;
		int set_score = 0;
		
		for (int j = 0; j < 13; j++)
		{
			ArrayList<ArrayList<Integer>> l = new ArrayList<ArrayList<Integer>>(); // list of "tuples"
			int s = 0;
			for (int i = 0; i < 4; i++)
			{
				if (c[i][j] && !chosen[i][j])
				{
					s++;
					l.add(new ArrayList<Integer>(Arrays.asList(i, j)));
				}
			}
			if (s >= 3)
			{
				count++;
				for (ArrayList<Integer> coordinate: l)
					chosen[coordinate.get(0)][coordinate.get(1)] = true;
				set_score += s * SCORE[j];
			}
		}
		
		return new ArrayList<Object>(Arrays.asList(count, set_score, chosen));
	}
	
	/**
	 * Overloading method to count number of set
	 * @param hand0
	 * @return
	 */
	public static ArrayList<Object> set(ArrayList<Card> hand0)
	{
		return set(hand0, new boolean[4][13]);		
	}
	
	/**
	 * Count the number of run in a given hand
	 * @param an Array List of Card that is needed to be evaluated and an a chosen matrix.
	 * @return number of run, the SCORE of the run, and the chosen matrix. 
	 */
	static ArrayList<Object> run(ArrayList<Card> hand0, boolean[][] chosen)
	{
		boolean[][] c = convertTo2DBooleanArray(hand0);
		int count = 0;
		int run_score = 0;
		ArrayList<ArrayList<Integer>> l = new ArrayList<ArrayList<Integer>>();
		int s = 0;	
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 13; j++)
			{
				if (c[i][j] && !chosen[i][j])
				{
					s++;
					l.add(new ArrayList<Integer>(Arrays.asList(i, j)));
				}
				else 
				{
					if (s >= 3)
					{
						count++;
						for (ArrayList<Integer> coordinate: l)
						{
							chosen[coordinate.get(0)][coordinate.get(1)] = true;
							run_score += SCORE[coordinate.get(1)];
						}
					}
					s = 0;
					l.clear();
				}
			}
		}
		if (s >= 3)
		{
			count++;
			for (ArrayList<Integer> coordinate: l)
			{
				chosen[coordinate.get(0)][coordinate.get(1)] = true;
				run_score += SCORE[coordinate.get(1)];
			}
		}	
		return new ArrayList<Object>(Arrays.asList(count, run_score, chosen));
	}
	
	/**
	 * Overloading method to count number of run
	 * @param hand0
	 * @return
	 */
	static ArrayList<Object> run(ArrayList<Card> hand0)
	{
		return run(hand0, new boolean[4][13]);
	}
	
	/**
	 * Two-set
	 * @param hand0
	 * @param chosen
	 * @return Object including number of two-sets and an array list of card 
	 * that construct those two-sets.
	 */
	static ArrayList<Object> twoSet(ArrayList<Card> hand0, boolean[][] chosen)
	{
		boolean[][] c = convertTo2DBooleanArray(hand0);
		int count = 0;
		ArrayList<Card> twoSet = new ArrayList<Card>();
		
		for (int j = 0; j < 13; j++)
		{
			ArrayList<ArrayList<Integer>> l = new ArrayList<ArrayList<Integer>>();
			int s = 0;
			for (int i = 0; i < 4; i++)
				if (c[i][j] && !chosen[i][j])
				{
					s++;
					l.add(new ArrayList<Integer>(Arrays.asList(i, j)));
				}
			if (s == 2)
			{
				count++;
				for (ArrayList<Integer> coordinate: l)
				{
					chosen[coordinate.get(0)][coordinate.get(1)] = true;
					twoSet.add(Card.getCard(coordinate.get(1), coordinate.get(0))); // getCard(rank, suit)
				}
			}
		}
		return new ArrayList<Object>(Arrays.asList(count, twoSet, chosen));
	}
	
	static int twoRun(ArrayList<Card> hand0, boolean[][] chosen)
	{
		boolean[][] c = convertTo2DBooleanArray(hand0);
		int count = 0;
		for (int j = 0; j < 13; j++)
		{
			for (int i = 0; i < 4; i++)
				if (c[i][j] && j < 12 && c[i][j+1] && !chosen[i][j] && !chosen[i][j+1])
				{
					count++;
					chosen[i][j] = true;
					chosen[i][j+1] = true;
				}
				else if (c[i][j] && j < 11 && c[i][j+2] && !chosen[i][j] && !chosen[i][j+2])
				{
					count++;
					chosen[i][j] = true;
					chosen[i][j+2] = true;
				}
		}
		return count;
	}
	
	static int threeRun(ArrayList<Card> hand0, boolean[][] chosen)
	{
		boolean[][] c = convertTo2DBooleanArray(hand0);
		int count = 0;
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 13; j++)
				if (c[i][j] && j < 9 && c[i][j+2] && c[i][j+4] && !chosen[i][j] && !chosen[i][j+2] && !chosen[i][j+4])
				{
					count++;
					chosen[i][j] = true;
					chosen[i][j+2] = true;
					chosen[i][j+4] = true;
				}
		}
		return count;
	}
	
	public static int triangle(ArrayList<Card> hand0, boolean[][] chosen)
	{
		boolean[][] c = convertTo2DBooleanArray(hand0);
		int count = 0;
		
		for (int j = 0; j < 12; j++)
		{
			int h = 0, s1 = 0, s2 = 0;
			for (int i = 0; i < 4; i++)
			{
				int u = (c[i][j]   && !chosen[i][j])   ? 1 : 0;
				int v = (c[i][j+1] && !chosen[i][j+1]) ? 1 : 0;
				h += u * v;
				s1 += u;
				s2 += v;
			}
			count += ((s1 >= 2 | s2 >= 2) ? 1 : 0) & h;
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public static int hitScore(ArrayList<Card> hand0, double[] prob0, double k)
	{
		int count = 0;
		boolean[] c = new boolean[52];
		for (Card card: hand0)
			c[card.getId()] = true;
		
		ArrayList<Card> hitScoreCards = new ArrayList<Card>();
		
		for (int id = 0; id < 52; id++)
			if (!c[id] && 0.0 < prob0[id] && prob0[id] < 1.0)
			{
				ArrayList<Card> unmeldedCards = (ArrayList<Card>) hand0.clone();
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
				
				unmeldedCards = (ArrayList<Card>) hand0.clone();
				unmeldedCards.add(newCard);
				meldsSets = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
				if (meldsSets.size() != 0)
				{
					ArrayList<ArrayList<Card>> melds = meldsSets.get(0);
					for (ArrayList<Card> meld : melds)
						for (Card card : meld)
							unmeldedCards.remove(card);
				}
				if (countPreviousUnmeldedCards >= unmeldedCards.size()) hitScoreCards.add(newCard);
			}
		for (Card card: hitScoreCards)
			if (prob0[card.getId()] <= k) count++;
		return count; // number of hitscore card chosen by prob
	}
	
	/**
	 * Overloading hitScore
	 * @param hand0
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static int hitScore(ArrayList<Card> hand0)
	{
		boolean[] c = new boolean[52];
		for (Card card: hand0)
			c[card.getId()] = true;
		
		ArrayList<Card> hitScoreCards = new ArrayList<Card>();
		
		for (int id = 0; id < 52; id++)
			if (!c[id])
			{	
				// Count the number of unmeld cards before adding a new card
				ArrayList<Card> unmeldedCards = (ArrayList<Card>) hand0.clone();
				ArrayList<ArrayList<ArrayList<Card>>> meldsSets = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
				if (meldsSets.size() != 0)
				{
					ArrayList<ArrayList<Card>> melds = meldsSets.get(0);
					for (ArrayList<Card> meld : melds)
						for (Card card : meld)
							unmeldedCards.remove(card);
				}
				int countUnmeldedCards_before = unmeldedCards.size();
				
				// Adding new card 
				Card newCard = Card.strCardMap.get(Card.idStrMap.get(id));
				unmeldedCards = (ArrayList<Card>) hand0.clone();
				unmeldedCards.add(newCard);
				
				// Count the number of unmeld cards after adding a new card
				meldsSets = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
				if (meldsSets.size() != 0)
				{
					ArrayList<ArrayList<Card>> melds = meldsSets.get(0);
					for (ArrayList<Card> meld : melds)
						for (Card card : meld)
							unmeldedCards.remove(card);
				}
				int countUnmeldedCards_after = unmeldedCards.size();
				
				// If adding a new card:
				// - make the number of unmeld card the same as before: the new card improves a meld.
				// - reduce the number of unmeld card: the new card forms a new meld.
				if (countUnmeldedCards_before >= countUnmeldedCards_after)
					hitScoreCards.add(newCard);
			}
		
		int count = hitScoreCards.size();
		return count;
	}
	
	/**
	 * Count number of card at each rank
	 * @param hand0
	 * @return
	 */
	public static ArrayList<Integer> rank(ArrayList<Card> hand0)
	{
		ArrayList<Integer> rankCount = new ArrayList<Integer>();
		
		boolean[][] c = convertTo2DBooleanArray(hand0);
		for (int j = 0; j < 13; j++)
		{
			int count = 0;
			for (int i = 0; i < 4; i++)
			{
				if(c[i][j] == true) count++;
			}
			rankCount.add(count);
		}
		return rankCount;
		
	}
	
	public static int[] geoRelation (ArrayList<Card> hand0)
	{
		int[] dummyHand0 = new int[52];
		for (Card card: hand0) dummyHand0[card.getId()] = 1;
		
		String[] temp;
		temp = new String[] {"A", "B", "C", "D"};
		ArrayList<String> column = new ArrayList<>(126);
		int t;
		
		for(int j = 0; j < temp.length; j++)
			for(int i = 1; i < 14; i++) column.add(Integer.toString(i) + temp[j]);
		
		for(int j = 1; j < 14; j++)
		{
			for(int i = 0; i < temp.length; i++)
			{
				t = i+1;
				while(t < temp.length)
				{
					column.add(Integer.toString(j) + temp[i] + Integer.toString(j)+temp[t]);
					t += 1;
				}
				if (j < 13) column.add(Integer.toString(j) + temp[i] + Integer.toString(j+1)+temp[i]);
			}
		}
		
		//Instantiate a hashmap
		HashMap<String, Integer> m = new HashMap<>();
		
		for(int i = 0; i < column.size(); i++ )
		{
			if (i < dummyHand0.length) m.put(column.get(i), dummyHand0[i]);
			else m.put(column.get(i), 0);
		}
		
		for(int i = 0; i < column.size(); i++ )
		{
			if (column.get(i).length() >= 4)
			{
				String a;
				String b;
				if (column.get(i).length() == 4)
				{
					a = column.get(i).substring(0, 2);
					b = column.get(i).substring(2, 4);
				}
				else if (column.get(i).length() == 5)
				{
					a = column.get(i).substring(0, 2);
					b = column.get(i).substring(2, 5);
				}
				else
				{
					a = column.get(i).substring(0, 3);
					b = column.get(i).substring(3, 6);
				}
				m.replace(column.get(i), m.get(a) * m.get(b));
			}
		}
		
		int[] geoRelation = new int[178];
		for(int i = 0; i < column.size(); i ++) geoRelation[i] = m.get(column.get(i));
		
		return geoRelation;
	}
	
	public static int[] geoRelation_expanded (ArrayList<Card> hand0)
	{
		int[] dummyHand0 = new int[52];
		for (Card card: hand0) dummyHand0[card.getId()] = 1;
		
		String[] temp;
		temp = new String[] {"A", "B", "C", "D"};
		ArrayList<String> column = new ArrayList<>(126);
		int t;
		
		for(int j = 0; j < temp.length; j++)
			for(int i = 1; i < 14; i++) column.add(Integer.toString(i) + temp[j]);
		
		for(int j = 1; j < 14; j++)
		{
			for(int i = 0; i < temp.length; i++)
			{
				t = i+1;
				while(t < temp.length)
				{
					column.add(Integer.toString(j) + temp[i] + Integer.toString(j)+temp[t]);
					t += 1;
				}
				if (j < 13) column.add(Integer.toString(j) + temp[i] + Integer.toString(j+1)+temp[i]);
				if (j < 12) column.add(Integer.toString(j) + temp[i] + Integer.toString(j+2)+temp[i]);
			}
		}
		
		//Instantiate a hashmap
		HashMap<String, Integer> m = new HashMap<>();
		
		for(int i = 0; i < column.size(); i++ )
		{
			if (i < dummyHand0.length) m.put(column.get(i), dummyHand0[i]);
			else m.put(column.get(i), 0);
		}
		
		for(int i = 0; i < column.size(); i++ )
		{
			if (column.get(i).length() >= 4)
			{
				String a;
				String b;
				if (column.get(i).length() == 4)
				{
					a = column.get(i).substring(0, 2);
					b = column.get(i).substring(2, 4);
				}
				else if (column.get(i).length() == 5)
				{
					a = column.get(i).substring(0, 2);
					b = column.get(i).substring(2, 5);
				}
				else
				{
					a = column.get(i).substring(0, 3);
					b = column.get(i).substring(3, 6);
				}
				m.replace(column.get(i), m.get(a) * m.get(b));
			}
		}
		
		int[] geoRelation = new int[222];
		for(int i = 0; i < column.size(); i ++) geoRelation[i] = m.get(column.get(i));
		
		return geoRelation;
		
	}	

	static boolean[][] convertTo2DBooleanArray(ArrayList<Card> hand0)
	{
		boolean[][] c = new boolean[4][13];
		for (Card card: hand0)
			c[card.getSuit()][card.getRank()] = true;
		return c;
	}
	
	private static boolean[][] clone(boolean[][] a)
	{
		boolean[][] res = new boolean[4][13];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 13; j++)
				res[i][j] = a[i][j];
		return res;
	}
	
	static void print(boolean[][] a)
	{
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 13; j++)
				System.out.printf("%d ", a[i][j] ? 1 : 0);
			System.out.println();
		}
	}
}