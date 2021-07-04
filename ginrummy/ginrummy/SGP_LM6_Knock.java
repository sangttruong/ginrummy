import java.util.ArrayList;
import java.util.Random;


/**
 * 
 * @author Sang T. Truong
 * @version 1.0

 */
public class SGP_LM6_Knock implements GinRummyPlayer {
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
    
	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
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

	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		// Ignore other player draws.  Add to cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			cards.add(drawnCard);
			this.drawnCard = drawnCard;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Card getDiscard() {
		// Discard a random card (not just drawn face up) leaving minimal deadwood points.
		double maxPredScore = -Double.MAX_VALUE;
		
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
			for (Card card: eligibleDiscardCards_1) 
			{
				ArrayList<ArrayList<Card>> bestMelds = bestMeldSets.get(0); 
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
		
		for (Card card: unmeldedCards)
		{
			ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
			remainingCards.remove(card);
			ArrayList<Integer> features = new ArrayList<Integer>();
			
			ArrayList<ArrayList<ArrayList<Card>>> bestMeldSet = GinRummyUtil.cardsToBestMeldSets(remainingCards);
			
			ArrayList<ArrayList<Card>> bestMelds = new ArrayList<ArrayList<Card>>();
			if (!bestMeldSet.isEmpty()) bestMelds = bestMeldSet.get(0);
		
			
			// Game-state index
			features.add(gamestateIndex);
			
			// Number of melds
			features.add(bestMelds.size());
			
			// Deadwood
			int deadwood = GinRummyUtil.getDeadwoodPoints(bestMelds, remainingCards);
			features.add(deadwood);	
						
			// Number of hitscore
			int hitscore = FeatureEngineer0.hitScore(remainingCards);
			features.add(hitscore);
			
			// Count of each rank
			ArrayList<Integer> rankCount = FeatureEngineer0.rank(remainingCards);
			for (Integer rank: rankCount) features.add(rank);
			
			// Additional dummy
			int deadwood_1_10 = (deadwood > 0 && deadwood < 11)? 1 : 0;
			features.add(deadwood_1_10);
			int deadwood_equal0 = (deadwood == 0)? 1 : 0;
			features.add(deadwood_equal0);
			
			double predScore = linearRegression(features);
			
//			System.out.println(">>>>>>>>>>>>>> Feature: " + features);
//			System.out.println(">>>>>>>>>>>>>> Discard card: " + card);
//			System.out.println(">>>>>>>>>>>>>> Score: " + predScore);
			
			if (predScore >= maxPredScore)
			{
				if (predScore > maxPredScore) {
					maxPredScore = predScore;
					unmeldedCards_maxPredScore.clear();
				}
				unmeldedCards_maxPredScore.add(card);
			}
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
		
	public double linearRegression (ArrayList<Integer> X)
	{	
		double Intercept        =  1.071207;
		double GamestateNum     = -0.119109;
		double MeldNum0         =  1.007642;
		double Deadwood0        = -1.060207;
		double Hitscore0        = -0.158657;
		double A                =  0.710397;
		double Two              =  0.399233;
		double Three            =  0.468102;
		double Four             =  0.806911;
		double Five             =  0.889163;
		double Six              =  0.859303;
		double Seven            =  0.843583;
		double Eight            =  0.934078;
		double Nine             =  0.948112;
		double Ten              =  1.026966;
		double J                =  0.928460;
		double Q                =  0.977607;
		double K                =  0.920155;
		double Deadwood0_1_10   = 12.458583;
		double Deadwood0_0      = 19.369405;
		
		double pred = Intercept
				    + X.get(0) * GamestateNum
				    + X.get(1) * MeldNum0
				    + X.get(2) * Deadwood0
				    + X.get(3) * Hitscore0
				    + X.get(4) * A
				    + X.get(5) * Two
				    + X.get(6) * Three
				    + X.get(7) * Four
				    + X.get(8) * Five
				    + X.get(9) * Six
				    + X.get(10) * Seven
				    + X.get(11) * Eight
				    + X.get(12) * Nine
				    + X.get(13) * Ten
				    + X.get(14) * J
				    + X.get(15) * Q
				    + X.get(16) * K
				    + X.get(17) * Deadwood0_1_10
				    + X.get(18) * Deadwood0_0;
		
		return pred;
	}
	
	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		// Ignore other player discards.  Remove from cards if playerNum is this player.
		if (playerNum == this.playerNum)
			cards.remove(discardedCard);
	}
	
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
        		if (unmeldedCards.size() <= 2 && turnCount > 22)
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
		// Ignored by simple player, but could affect strategy of more complex player.
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
