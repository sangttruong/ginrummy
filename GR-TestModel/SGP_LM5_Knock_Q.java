import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * 
 * @author Sang T. Truong
 * @version 1.0

 */
public class SGP_LM5_Knock_Q implements GinRummyPlayer {
	private int playerNum;
	@SuppressWarnings("unused")
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Random random = new Random(26);
	private boolean opponentKnocked = false;
	Card faceUpCard, drawnCard; 
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
	private static int gamestateIndex = 1;
	
    int turnCount = 0;
//    private final int START_TURNCOUNT = 12;
//    private final int INIT_RATIO = 2;
    
    private double intercept = 7.10376382;
	
	private double deadwood = -0.9631598;
	private double deadwood_1_10 = 13.5210358;
	private double deadwood_equal0 = 39.9707105;
	
	private double meld = 0.11716917;
//	private double hitscore = -0.2636346;
	private static double hitscore = 0.2;

	private int[] scores;
	private ArrayList<ArrayList<Integer>> featuresHistory;
	
	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();

		if (this.scores == null || this.scores[0] >= 100 || this.scores[1] >= 100)
			this.scores = new int[2];
		featuresHistory  = new ArrayList<ArrayList<Integer>>();
		featuresHistory.add(getFeature(this.cards));
	}

	@Override
	public boolean willDrawFaceUpCard(Card card) {
		// Return true if card would be a part of a meld, false otherwise.
		this.faceUpCard = card;
		@SuppressWarnings("unchecked")
		ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
		newCards.add(card);
		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
			if (meld.contains(card))
				return true;
		return false;
	}
	
//    @SuppressWarnings("unchecked")
//	@Override
//	public boolean willDrawFaceUpCard(Card cardFaceUp) {
//		// Return true if card would be a part of a meld, false otherwise.
//        this.faceUpCard = cardFaceUp;
//        turnCount++;
//        if (turnCount > START_TURNCOUNT)
//        {
//            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(this.cards);
//            ArrayList<Card> unmeldedCards = (ArrayList<Card>) this.cards.clone();
//            if (bestMeldSets.size() != 0)
//            {
//                ArrayList<ArrayList<Card>> melds = bestMeldSets.get(0);
//                for (ArrayList<Card> meld: melds)
//                    for (Card card: meld)
//                        unmeldedCards.remove(card);
//            }
//            boolean[] c = new boolean[52];
//            for (Card card: unmeldedCards)
//                c[card.getId()] = true;
//            for (int rank = 12; rank >= 0; rank--)
//                for (int suit = 0; suit < 4; suit++)
//                    if (c[rank + suit * 13] && rank >= cardFaceUp.getId() / INIT_RATIO)
//                        return true;
//        }
//
//		ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
//		newCards.add(cardFaceUp);
//		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
//			if (meld.contains(cardFaceUp))
//				return true;
//		return false;
//	}

	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		// Ignore other player draws.  Add to cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			cards.add(drawnCard);
			this.drawnCard = drawnCard;
		}
	}

	/**
	 * Get 5 features to feed to NN
	 * @param cards
	 * @return 5 features to feed to NN
	 */
	private ArrayList<Integer> getFeature(ArrayList<Card> cards)
	{
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSet = GinRummyUtil.cardsToBestMeldSets(cards);
		ArrayList<Integer> feature = new ArrayList<Integer>();
		ArrayList<ArrayList<Card>> bestMelds = new ArrayList<ArrayList<Card>>();
		if (!bestMeldSet.isEmpty()) bestMelds = bestMeldSet.get(0);
		
		// Deadwood, Deadwood_greater10, Deadwood_equal0
		int deadwood = GinRummyUtil.getDeadwoodPoints(bestMelds, cards);
		feature.add(deadwood);			
		
		int deadwood_1_10 = (deadwood > 0 && deadwood < 11)? 1 : 0;
		feature.add(deadwood_1_10);
		
		int deadwood_equal0 = (deadwood == 0)? 1 : 0;
		feature.add(deadwood_equal0);
		
		// Number of melds
		feature.add(bestMelds.size());
		
		// Number of hitscore
		int hitscore = FeatureEngineer0.hitScore(cards);
		feature.add(hitscore);
		
		// Game-state index
		// feature.add(gamestateIndex);

		return feature;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Card getDiscard() {
		// Discard a random card (not just drawn face up) leaving minimal deadwood points.
		
		ArrayList<Card> eligibleDiscardCards_1 = new ArrayList<Card>();
		
		// Eligibility conditions set by naive game that I don't want to break----------------------------------------------------
		for (Card card : cards) {
			// Cannot draw and discard face up card.
			if (card == drawnCard && drawnCard == faceUpCard)
				continue;
			// Disallow repeat of draw and discard.
			ArrayList<Card> drawDiscard = new ArrayList<Card>();
			drawDiscard.add(drawnCard);
			drawDiscard.add(card);
			if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
				continue;
			
			eligibleDiscardCards_1.add(card);
		}
		
		// Prevent breaking melds------------------------------------------------------------------------------------------------
		// System.out.println(">>>>>>>>> This is player's hand before discard: " + cards);
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		
		ArrayList<Card> unmeldedCards = new ArrayList<Card>(); // This is the set of card being consider discard
		if (bestMeldSets.isEmpty())
			unmeldedCards = (ArrayList<Card>) cards.clone();
		else 
		{
			ArrayList<ArrayList<Card>> bestMelds = bestMeldSets.get(0); 
			for (Card card: eligibleDiscardCards_1) 
			{
				boolean check = false;
				for (ArrayList<Card> meld: bestMelds)
					if(meld.contains(card))
					{
						check = true;
						break;
					}
				if (!check) unmeldedCards.add(card);
			}
		}
		
		// If all 11 cards are in melds, they are all in unmelded cards
		if (unmeldedCards.size() == 0)
			unmeldedCards = (ArrayList<Card>) cards.clone();
		
		
		// System.out.println("Unmelded cards: " + unmeldedCards);
		// Discard based on model ------------------------------------------------------------------------------------------------
		ArrayList<Card> unmeldedCards_maxPredScore = new ArrayList<Card>();
		ArrayList<ArrayList<Integer>> features = new ArrayList<ArrayList<Integer>>();
		ArrayList<Card> features_cards = new ArrayList<Card>();
		
		for (Card card: unmeldedCards)
		{
			ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
			remainingCards.remove(card);
			ArrayList<Integer> feature = getFeature(remainingCards);
			
			features.add(feature);
			features_cards.add(card);
			
			// double predScore = linearRegression_2(features);
		
//			System.out.println(">>>>>>>>>>>>>> Feature: " + features);
//			System.out.println(">>>>>>>>>>>>>> Discard card: " + card);
//			System.out.println(">>>>>>>>>>>>>> Score: " + predScore);
		}
		double[] q_values = new double[features.size()];
		try {
			q_values = getQ(features);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// for (int i = 0; i < features.size(); i++)
		// 	System.out.println("=====>>>>> " + features_cards.get(i) + " : " + q_values[i]);
		double maxPredScore = -Double.MAX_VALUE;
		for (int i = 0; i < features.size(); i++)
			if (q_values[i] >= maxPredScore)
			{
				if (q_values[i] > maxPredScore) 
				{
					maxPredScore = q_values[i];
					unmeldedCards_maxPredScore.clear();
				}
				unmeldedCards_maxPredScore.add(features_cards.get(i));
			}
		//--------------------------------------------------------------------------------------------------------------------------------------
		Card discard = unmeldedCards_maxPredScore.get(random.nextInt(unmeldedCards_maxPredScore.size()));
		
		// Prevent future repeat of draw, discard pair-----------------------------------------------------------------------------------------
		ArrayList<Card> drawDiscard = new ArrayList<Card>();
		drawDiscard.add(drawnCard);
		drawDiscard.add(discard);
		drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
		gamestateIndex++;
		return discard;
	}

	private double[] getQ(ArrayList<ArrayList<Integer>> features) throws IOException
	{
		FileWriter writeState = new FileWriter("/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-TestModel/Model/features.txt");
		for (ArrayList<Integer> feature: features)
		{
			for (int value: feature)
				writeState.write(String.valueOf(value) + " ");
			writeState.write('\n');
		}
		writeState.close();

		double[] q_values = new double[features.size()];
		try 
		{
			String[] cmdarray = new String[] {
					"/bin/bash",
					"-lc",
					"python3 Documents/GitHub/GR-Git/GR-TestModel/Model/run.py"
					};

			File dir = new File("/Users/hoangpham");

			// rt.exec(cmdarray, null, dir);
			
			Runtime.getRuntime().exec(cmdarray, null, dir).waitFor();
		}
		catch (Exception e)
		{
			// something
		}
		File q_value_file = new File("Model/q_value.txt");
		Scanner sc = new Scanner(q_value_file);
		for (int i = 0; i < features.size() && sc.hasNextLine(); i++)
			q_values[i] = Double.valueOf(sc.nextLine());
		sc.close();
		return q_values;
	}
	
	public double linearRegression (ArrayList<Integer> X)
	{		
		double intercept = 4.81730395;
		
		double deadwood = -0.959333438;
		double deadwood_1_10 = 13.58686275;
		double deadwood_equal0 = 39.9556314;
		
		double meld = 0.593976935;
		
		double pred = intercept
				    + X.get(0) * deadwood
				    + X.get(1) * deadwood_1_10
				    + X.get(2) * deadwood_equal0
				    + X.get(3) * meld;
		
		return pred;
	}
	
	public static void increaseCoeff()
	{
		hitscore = hitscore + 0.05;
	}
	
	public static void decreaseCoeff()
	{
		hitscore = hitscore - 0.05;
	}
	
	public static double getCoeff()
	{
		return hitscore;
	}
	
	public void setCoeff(double[] coeffs)
	{
		intercept = coeffs[0];
		
		deadwood = coeffs[1];
		deadwood_1_10 = coeffs[2];
		deadwood_equal0 = coeffs[3];
		
		meld = coeffs[4];
	}
	
	public double linearRegression_2 (ArrayList<Integer> X)
	{				
		double pred = intercept
				    + X.get(0) * deadwood
				    + X.get(1) * deadwood_1_10
				    + X.get(2) * deadwood_equal0
				    + X.get(3) * meld
					+ X.get(4) * hitscore;
		
		return pred;
	}
	
	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		// Ignore other player discards.  Remove from cards if playerNum is this player.
		if (playerNum == this.playerNum)
			cards.remove(discardedCard);
		
		featuresHistory.add(getFeature(this.cards));
	}

//	@Override
//	public ArrayList<ArrayList<Card>> getFinalMelds() {
//		// Check if deadwood of maximal meld is low enough to go out. 
//		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
//		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD))
//			return null;
//		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
//	}
	
   @SuppressWarnings("unchecked")
	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {

		// Check if deadwood of maximal meld is low enough to go out. 
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		
		ArrayList<ArrayList<Card>> bestMelds = (bestMeldSets.isEmpty())?
				new ArrayList<ArrayList<Card>>() : bestMeldSets.get(0);
		
        ArrayList<Card> unmeldedCards = (ArrayList<Card>) this.cards.clone();
        if (bestMeldSets.size() != 0)
        {
            ArrayList<ArrayList<Card>> melds = bestMeldSets.get(0);
            for (ArrayList<Card> meld: melds)
                for (Card card: meld)
                    unmeldedCards.remove(card);
        }
        if (opponentKnocked)
        {
            if (bestMeldSets.size() == 0) return new ArrayList<ArrayList<Card>>();
            else return bestMeldSets.get(random.nextInt(bestMeldSets.size()));
        }
        else
        {
        	int deadwoodPoints = GinRummyUtil.getDeadwoodPoints(bestMelds, cards);
        	if (bestMeldSets.isEmpty() || deadwoodPoints  > GinRummyUtil.MAX_DEADWOOD) return null; // Unable to knock
        	else if (deadwoodPoints >= 1)
        	{
        		if (unmeldedCards.size() <= 2 && turnCount > 20)
        			return bestMeldSets.get(random.nextInt(bestMeldSets.size())); //Dangerous zone
        		else return null; //Choose not to knock
        	}
        	else return bestMeldSets.get(random.nextInt(bestMeldSets.size()));
        }
	}

	@Override
	public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
		// Melds ignored by simple player, but could affect which melds to make for complex player.
		if (playerNum != this.playerNum)
			opponentKnocked = true;
	}

	@Override
	public void reportScores(int[] scores) {
		int score = 0;
		if (scores[this.playerNum] != this.scores[this.playerNum])
		{
			score = scores[this.playerNum] - this.scores[this.playerNum];
			this.scores[this.playerNum] = scores[this.playerNum];
		}
		else
		{
			score = -(scores[1 - this.playerNum] - this.scores[1 - this.playerNum]);
			this.scores[1 - this.playerNum] = scores[1 - this.playerNum];
		}
		
		FileWriter features;
		try
		{
			features = new FileWriter("Documents/GitHub/GR-Git/GR-TestModel/Model/features.txt");
			for (ArrayList<Integer> feature: featuresHistory)
			{
				for (int value: feature)
					features.write(String.valueOf(value) + " ");
				features.write('\n');
			}
			features.write(String.valueOf(score));
			features.close();
		}
		catch (IOException e)
		{
			// something
		}

		try 
		{
			String[] cmdarray = new String[] {
					"/bin/bash",
					"-lc",
					"python3 Documents/GitHub/GR-Git/GR-TestModel/Model/update.py"
					};

			File dir = new File("/Users/sangtruong_2021");

			// rt.exec(cmdarray, null, dir);
			
			Runtime.getRuntime().exec(cmdarray, null, dir).waitFor();
		}
		catch (Exception e)
		{
			// something
		}
		
	}

	@Override
	public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {
		// Ignored by simple player, but could affect strategy of more complex player.
		
	}

	@Override
	public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
		// Ignored by simple player, but could affect strategy of more complex player.		
	}
	
}
