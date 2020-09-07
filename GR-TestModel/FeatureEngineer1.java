import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * @author Sang T.Truong and Hoang H. Pham 
 * @version 1.0
 * 
FeatureEngineering1 is designed to collect feature of the estimated hand of player 1 that is
possibly known by player 0. In other words, these features are generated based on the estimated 
hand of player 1. These features are used by player 0 to improve performance. 
 
Features of player 1 are:
- Run
- Set
- Deadwood

 *
 */
public class FeatureEngineer1 {
	
	private static final int[] SCORE = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};
	
	public static ArrayList<Double> getFeatures(GameState gs)
	{
		ArrayList<Card> cards = GinRummyUtil.bitstringToCards(gs.getEstimatedHand1());
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
		
		features.add(deadWood);
		
		return features;
	} 
	
	/**
	 * Count the number of sets in a given hand
	 * @param an Array List of Card that is needed to be evaluated and an a choose matrix.
	 * @return number of set, the score of the set, and the choose matrix. 
	 */
	public static ArrayList<Object> set(ArrayList<Card> cards, boolean[][] choose)
	{
		boolean[][] c = convertTo2DBooleanArray(cards);
		int count = 0;
		int set_score = 0;
		
		for (int j = 0; j < 13; j++)
		{
			ArrayList<ArrayList<Integer>> l = new ArrayList<ArrayList<Integer>>(); // list of "tuples"
			int s = 0;
			for (int i = 0; i < 4; i++)
			{
				if (c[i][j] && !choose[i][j])
				{
					s++;
					l.add(new ArrayList<Integer>(Arrays.asList(i, j)));
				}
			}
			if (s >= 3)
			{
				count++;
				for (ArrayList<Integer> coordinate: l)
					choose[coordinate.get(0)][coordinate.get(1)] = true;
				set_score += s * SCORE[j];
			}
		}
		
		return new ArrayList<Object>(Arrays.asList(count, set_score, choose));
	}
	
	/**
	 * Count the number of run in a given hand
	 * @param an Array List of Card that is needed to be evaluated and an a choose matrix.
	 * @return number of run, the score of the run, and the choose matrix. 
	 */
	static ArrayList<Object> run(ArrayList<Card> cards, boolean[][] choose)
	{
		boolean[][] c = convertTo2DBooleanArray(cards);
		int count = 0;
		int run_score = 0;
		
		for (int i = 0; i < 4; i++)
		{
			ArrayList<ArrayList<Integer>> l = new ArrayList<ArrayList<Integer>>();
			int s = 0;
			for (int j = 0; j < 13; j++)
			{
				if (c[i][j] && !choose[i][j])
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
							choose[coordinate.get(0)][coordinate.get(1)] = true;
							run_score += SCORE[coordinate.get(1)];
						}
					}
					s = 0;
					l.clear();
				}
			}
		}
		
		return new ArrayList<Object>(Arrays.asList(count, run_score, choose));
	}
	
	static boolean[][] convertTo2DBooleanArray(ArrayList<Card> cards)
	{
		boolean[][] c = new boolean[4][13];
		for (Card card: cards)
			c[card.getSuit()][card.getRank()] = true;
		return c;
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
