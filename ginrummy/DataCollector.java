import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Sang T. Truong and Hoang H. Pham
 * @version 1.0
 * 
 * Important: For convinience, our player is always player 0. 
 * In other words, we only collect the data that is visible to player 0.
 * Since the goal of the Gin Rummy is to score to 100 first, each game has several game-plays.
 * Each game play contains several game-states. 
 * The information of each game-state is collect by calling GameState object. 
 * For each game-play, we also collect these information: 
 * - The number of game-states in this game-play
 * - The starter
 * - The knocker
 * - The winner
 * - The score that is won by the winner
 * 
 * In each game-state, player 0 cannot see the hand of player 1, but player 0 can estimate player 1 hand using HandEstimator object.
 * As the game progresses, this this estimation is update dynamically.
 * 
 * With the information of player 0's hand and estimated player 1's hand, we can compute features of player 0's hand.
 * In addition, we can also update these feature as the game progresses. Note that some features are not entirely 
 * depended on player 0's hand, such as hit-score.
 */

@SuppressWarnings("unused")
public class DataCollector {
	public static final int PLAYER = 0; // Collecting data that is visible to our player - player 0
	// Game-state is a 2D array that stores
	// 1D: store game-plays
	// 2D: store game-states in a game-play
	
	private static ArrayList<ArrayList<GameState>> gameplays = new ArrayList<ArrayList<GameState>>();
	private static int currentGameplay = -1; // Zero-based index of game-play
	private static int latestStateIndex = -1; // Zero-based index of game-state
	
	private static ArrayList<Integer> winner = new ArrayList<Integer>();
	private static ArrayList<Integer> starter = new ArrayList<Integer>(); // first player to start
	private static ArrayList<Integer> knocker = new ArrayList<Integer>(); // player who knocks
	private static ArrayList<Integer> score = new ArrayList<Integer>(); // score that is won by winner
	
//	public static HandEstimator estimator = new HandEstimator();
	private static ArrayList<ArrayList<Double>> allFeatures = new ArrayList<ArrayList<Double>>();
	
	private static ArrayList<ArrayList<ArrayList<Card>>> knockerMeldsSet = new ArrayList<ArrayList<ArrayList<Card>>>();
	private static ArrayList<ArrayList<Card>> knockerDeadwoodCardsSet = new ArrayList<ArrayList<Card>>();
	private static ArrayList<Integer> knockerDeadwoodSet = new ArrayList<Integer>();
	
	private static ArrayList<ArrayList<ArrayList<Card>>> nonknockerMeldsSet = new ArrayList<ArrayList<ArrayList<Card>>>();
	private static ArrayList<ArrayList<Card>> nonknockerDeadwoodCardsSet = new ArrayList<ArrayList<Card>>();
	private static ArrayList<Integer> nonknockerDeadwoodSet = new ArrayList<Integer>();
	
	private static ArrayList<Integer> knockerMeldsCount = new ArrayList<Integer>();
	private static ArrayList<Integer> nonknockerMeldsCount = new ArrayList<Integer>();	
	
	private static ArrayList<ArrayList<Card>> knockerHandSet = new ArrayList<ArrayList<Card>>() ;
	private static ArrayList<ArrayList<Card>> nonknockerHandSet = new ArrayList<ArrayList<Card>>();
	
	private static ArrayList<Integer> knockerHitscoreSet = new ArrayList<Integer>();
	private static ArrayList<Integer> nonknockerHitscoreSet = new ArrayList<Integer>();
	
	/**
	 * Increment to the next game-play. Call everytime there is a new game-play
	 */
	public static void nextGamePlay()
	{
		currentGameplay++;
		gameplays.add(new ArrayList<GameState>());
		latestStateIndex = -1;
//		estimator = new HandEstimator();
//		estimator.init();
	}
	
	/**
	 * Add a new game-state to the current game-play
	 */
	public static void addGameState()
	{
		GameState gameState = new GameState();
		gameplays.get(currentGameplay).add(gameState);
		latestStateIndex++;
	}
	
	public static void popGamePlay()
	{
		currentGameplay--;
		gameplays.remove(gameplays.size()-1);
	}
	
	
	//-----------------------------------------------------------------
	
	public static void addStarter(int pStarter)
	{
		starter.add(pStarter);
	}
	
	public static void addKnocker(int pKnocker)
	{
		knocker.add(pKnocker);
	}
	
	public static void addWinner(int pWinner)
	{
		winner.add(pWinner);
	}
	
	public static void addScore(int pScore)
	{
		score.add(pScore);
	}
	
	public static void addKnockerMelds (ArrayList<ArrayList<Card>> knockerMelds)
	{
		knockerMeldsSet.add(knockerMelds);
	}
	
	public static void addKnockerDeadwoodCards (ArrayList<Card> knockerDeadwoodCards)
	{
		knockerDeadwoodCardsSet.add(knockerDeadwoodCards);
	}
	
	public static void addNonknockerMelds (ArrayList<ArrayList<Card>> nonknockerMelds)
	{
		nonknockerMeldsSet.add(nonknockerMelds);
	}
	
	public static void addNonknockerDeadwoodCards (ArrayList<Card> nonknockerDeadwoodCards)
	{
		nonknockerDeadwoodCardsSet.add(nonknockerDeadwoodCards);
	}
	
	public static void addKnockerDeadwood (int knockerDeadwood)
	{
		knockerDeadwoodSet.add(knockerDeadwood);
	}
	
	public static void addNonknockerDeadwood (int nonknockerDeadwood)
	{
		nonknockerDeadwoodSet.add(nonknockerDeadwood);
	}
	
	public static void addKnockerMeldsCount (int count)
	{
		knockerMeldsCount.add(count);
	}
	
	public static void addNonknockerMeldsCount (int count)
	{
		nonknockerMeldsCount.add(count);
	}
	
	@SuppressWarnings("unchecked")
	public static void addKnockerHand (ArrayList<Card> knockerHand)
	{
		knockerHandSet.add((ArrayList<Card>) knockerHand.clone());
	}
	
	@SuppressWarnings("unchecked")
	public static void addNonknockerHand (ArrayList<Card> nonknockerHand)
	{
		nonknockerHandSet.add((ArrayList<Card>) nonknockerHand.clone());
	}
	
	public static void addKnockerHitscore (int knockerHitscore)
	{
		knockerHitscoreSet.add(knockerHitscore);
	}
	
	public static void addNonknockerHitscore (int nonknockerHitscore)
	{
		nonknockerHitscoreSet.add(nonknockerHitscore);
	}
	
	//-----------------------------------------------------------------
	
	public static void addHand1(ArrayList<Card> hand1)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setHand1(hand1);
	}
	public static void addFaceUp1(Card faceUp1)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setFaceUp1(faceUp1);
	}
	
	public static void addDrawnFaceUp1(boolean drawnFaceUp1)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setDrawnFaceUp1(drawnFaceUp1);
	}	
	
	public static void addDrawn1(Card drawn1)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setDrawn1(drawn1);
	}
	
	public static void addDiscarded1(Card discarded1)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setDiscarded1(discarded1);
	}
	
//	public static void addEstimatedHand1()
//	{
//		gameplays.get(currentGameplay).get(latestStateIndex).setEstimatedHand1(estimator.getEstimatedHand());
//	}

//	public static void addProb0()
//	{
//		gameplays.get(currentGameplay).get(latestStateIndex).setProb0(estimator.prob0);
//	}
	
	public static void addHand0(ArrayList<Card> hand0)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setHand0(hand0);
	}
	
	public static void addFaceUp0(Card faceUp0)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setFaceUp0(faceUp0);
	}	
	
	public static void addWillDraw0(Card willDraw0)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setWillDraw0(willDraw0);
	}
	
	public static void addWillDiscard0(Card willDiscard0)
	{
		gameplays.get(currentGameplay).get(latestStateIndex).setWillDiscard0(willDiscard0);
	}	
	
//	public static void setKnown()
//	{
//		GameState gs = gameplays.get(currentGameplay).get(latestStateIndex);
//		estimator.setKnown(GinRummyUtil.bitstringToCards(gs.getHand0()), false);
//		
//		if (gs.getFaceUp1() != null && gs.getDiscarded1() != null)
//			estimator.reportDrawDiscard(gs.getFaceUp1(), gs.getDrawnFaceUp1(), gs.getDiscarded1());
//	}
	//-----------------------------------------------------------------
	
	/**
	 * Print all game-states in a game-play and their associated features to gamestates file and features file .
	 * @param gamestates
	 * @param features
	 * @throws IOException
	 */
	public static void print(FileWriter gamestates, FileWriter features) throws IOException
	{
		printGameStates(gamestates);
		int index = 0;
		ArrayList<ArrayList<Double>> allFeatures = new ArrayList<ArrayList<Double>>();
		for (ArrayList<GameState> gameplay: gameplays)
		{
			int count = 0; 
			for (int i = 0; i < gameplay.size(); i++)
			{
				GameState gs = gameplay.get(i);
				if (gs.getHand0() != 0 && gs.getProb0() != null && gs.getEstimatedHand1() != 0) // no value
				{
					ArrayList<Double> temp = new ArrayList<Double>();
					for (double feature: FeatureEngineer0.getFeatures(gs, 0.1f))
						temp.add(feature);
					for (double feature: FeatureEngineer1.getFeatures(gs))
						temp.add(feature);
					allFeatures.add(temp);
					count++;
				}
			}
			features.write(count + "\n");
			for (int i = index; i < index + count; i++)
				features.write(allFeatures.get(i) + "\n");
			index += count;
		}
	}
	
	public static void printGameStates(FileWriter file) throws IOException
	{
		System.out.println(gameplays.size() + " " + knocker.size() + " " + score.size());
//		file.write(gameplays.size() + "\n");
//		System.out.println("For testing: " + nonknockerDeadwoodSet);
//		System.out.println("For testing: " + gameplays.size());
		
		for (int i = 0; i < gameplays.size(); i++)
		{
			for (int j = 0; j < gameplays.get(i).size(); j++)
			{
				GameState gs = gameplays.get(i).get(j);
				
				int score0 = (winner.get(i) == 0)? score.get(i) : -score.get(i);
				int score1 = (winner.get(i) == 1)? score.get(i) : -score.get(i);
				
				ArrayList<Card> hand0 = GinRummyUtil.bitstringToCards(gs.getHand0());
				ArrayList<Card> hand1 = GinRummyUtil.bitstringToCards(gs.getHand1());
				
				if (!hand0.isEmpty() && !hand1.isEmpty())
				{
					ArrayList<ArrayList<ArrayList<Card>>> bestMeldsSet0 = GinRummyUtil.cardsToBestMeldSets(hand0);
					ArrayList<ArrayList<ArrayList<Card>>> bestMeldsSet1 = GinRummyUtil.cardsToBestMeldSets(hand1);
					
					ArrayList<ArrayList<Card>> bestMelds0 = new ArrayList<ArrayList<Card>>();
					if (!bestMeldsSet0.isEmpty()) bestMelds0 = bestMeldsSet0.get(0);
					ArrayList<ArrayList<Card>> bestMelds1 = new ArrayList<ArrayList<Card>>();
					if (!bestMeldsSet1.isEmpty()) bestMelds1 = bestMeldsSet1.get(0);
					
					file.write("GamestateNum: " + (j+1) + ", " 
							+ "Starter: " + starter.get(i) + ", " 
							+ "Knocker: " + knocker.get(i) + ", " 
							+ "Winner: " + winner.get(i) + ", "
							
							+ "Score0: " + score0 + ", "
							+ "MeldNum0: " + bestMelds0.size() + ", " 
							+ "Deadwood0: " + GinRummyUtil.getDeadwoodPoints(bestMelds0, hand0) + ", "
							+ "Hitscore0: " + FeatureEngineer0.hitScore(hand0) + ", "
							+ "Hand0: " + hand0 + ", "
							
							+ "Score1: " + score1 + ", "
							+ "MeldNum1: " + bestMelds1.size() + ", " 
							+ "Deadwood1: " + GinRummyUtil.getDeadwoodPoints(bestMelds1, hand1) + ", "
							+ "Hitscore1: " + FeatureEngineer0.hitScore(hand1) + ", "
							+ "Hand1: " + hand1
							
							+ "\n"
							);
				}
			}
			int score0 = (winner.get(i) == 0)? score.get(i) : -score.get(i);
			int score1 = (winner.get(i) == 1)? score.get(i) : -score.get(i);
			
			int deadwood0 = (knocker.get(i) == 0)? knockerDeadwoodSet.get(i) : nonknockerDeadwoodSet.get(i);
			int deadwood1 = (knocker.get(i) == 1)? knockerDeadwoodSet.get(i) : nonknockerDeadwoodSet.get(i);
			
			int hitscore0 = (knocker.get(i) == 0)? knockerHitscoreSet.get(i) : nonknockerHitscoreSet.get(i);
			int hitscore1 = (knocker.get(i) == 1)? knockerHitscoreSet.get(i) : nonknockerHitscoreSet.get(i);
			
			int meldNumber0 = (knocker.get(i) == 0) ? knockerMeldsCount.get(i) : nonknockerMeldsCount.get(i);
			int meldNumber1 = (knocker.get(i) == 1) ? knockerMeldsCount.get(i) : nonknockerMeldsCount.get(i);
			
			ArrayList<Card> hand0 = (knocker.get(i) == 0) ? knockerHandSet.get(i): nonknockerHandSet.get(i);
			ArrayList<Card> hand1 = (knocker.get(i) == 1) ? knockerHandSet.get(i): nonknockerHandSet.get(i);
			
			file.write("GamestateNum: " + (gameplays.get(i).size()+1) + ", " 
					+ "Starter: " + starter.get(i) + ", " 
					+ "Knocker: " + knocker.get(i) + ", " 
					+ "Winner: " + winner.get(i) + ", " 
					
					+ "Score0: " + score0 + ", "
					+ "MeldNum0: " + meldNumber0 + ", "
					+ "Deadwood0: " + deadwood0 + ", "
					+ "Hitscore0: " + hitscore0 + ", "
					+ "Hand0: " + hand0 + ", "
					
					+ "Score1: " + score1 + ", "
					+ "MeldNum1: " + meldNumber1 + ", "
					+ "Deadwood1: " + deadwood1 + ", "
					+ "Hitscore1: " + hitscore1 + ", "
					+ "Hand1: " + hand1 
					
					+ "\n"
					);
			
//			System.out.println(knockerDeadwoodSet.get(i));
//			for (GameState gs: gameplays.get(i))
//				file.write(gs + "\n");
		}
	}
	
	public static void save(String filename) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(gameplays);
			out.writeObject(starter);
			out.writeObject(knocker);
			out.writeObject(winner);
			out.writeObject(score);
			out.writeObject(knockerDeadwoodSet);
			out.writeObject(nonknockerDeadwoodSet);
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}