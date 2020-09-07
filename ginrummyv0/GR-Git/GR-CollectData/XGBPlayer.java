import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("unused")
public class XGBPlayer implements GinRummyPlayer {
	private int playerNum;
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Random random = new Random();
	private boolean opponentKnocked = false;
	Card faceUpCard, drawnCard, discardedCard; // thêm vào discardedCard để ghi lại nước đi của đối thủ
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
	private int chosenMeldsIndex = 0;
	
	private static final int Q = 20, R = 90, S = 20;
	
	private static ArrayList<ArrayList<Integer>> winScoreHistory = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<ArrayList<Integer>> loseScoreHistory = new ArrayList<ArrayList<Integer>>();
	
//	private HandEstimator estimator;
	private boolean opponentDrawnFaceUp;
	
	private int[] scores = new int[2]; // cumulative scores
	
	public static void addHistoryScore(int scoreP0, int scoreP1, boolean p1Win)
	{
		if (p1Win)
			winScoreHistory.add(new ArrayList<Integer>(Arrays.asList(scoreP1, scoreP0)));
		else
			loseScoreHistory.add(new ArrayList<Integer>(Arrays.asList(scoreP1, scoreP0)));
	}
	
	public static void printHistoryScore()
	{
		System.out.println(winScoreHistory);
		System.out.println(loseScoreHistory);
	}

	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
		
		discardedCard = null;
		opponentDrawnFaceUp = false;
		
//		estimator = new HandEstimator();
//		estimator.init();
//		estimator.setKnown(this.cards, false);
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
		else
		{
//			if (this.discardedCard == null)
//				this.discardedCard = drawnCard; // edge case, because if opponent starts the game first, xgb-player doesn't have visibility to discards.peek(). THIS IS NOT CHEATING, FOR THE SAKE OF FAIRNESS IN THE GAME
			opponentDrawnFaceUp = (drawnCard == this.discardedCard);
		}
	}
		
	// sau khi getDiscard, nhớ gán lại cho discardedCard là lá vừa discard để cập nhật cho game sau của đối thủ.
	@SuppressWarnings("unchecked")
	@Override
	public Card getDiscard() {
//		ArrayList<Card> estOppHitScoreCards = estimator.getEstimatedHitScoreCards();
//		
//		// Generate a array list of cards that is considered to be discarded and is not in estOppHitScoreCards
//		ArrayList<Card> safelyCandidateCards = new ArrayList<Card>();
//		
//		for (Card card: cards) {
//			if (!estOppHitScoreCards.contains(card))
//				safelyCandidateCards.add(card);				
//		}
//		if (safelyCandidateCards.isEmpty())
//			safelyCandidateCards = cards;
//		
//		// Generate a map of all potential discards with their probabilities of win
//		HashMap<Card, Double> allProbWin = new HashMap<Card, Double>();
//		
//		ArrayList<Card> safelyDiscardableCards = new ArrayList<Card>();
//		
//		long bestMeldsBitstring = 0;
//		ArrayList<ArrayList<ArrayList<Card>>> bestMeldsSets = GinRummyUtil.cardsToBestMeldSets(cards);
//		ArrayList<ArrayList<Card>> chosenbestMeldsSet = new ArrayList<ArrayList<Card>>();
//		if (!bestMeldsSets.isEmpty()) // meld exist 
//		{
//			chosenbestMeldsSet = bestMeldsSets.get(0);
//			for (ArrayList<Card> meld: chosenbestMeldsSet)
//				bestMeldsBitstring |= GinRummyUtil.cardsToBitstring(meld);
//		}
//		
//		for (Card card: safelyCandidateCards) {
//			
//			// Cannot draw and discard face up card.
//			if (card.getId() == drawnCard.getId() && drawnCard.getId() == faceUpCard.getId()) continue;
//			
//			// Disallow repeat of draw and discard.
//			ArrayList<Card> drawDiscard = new ArrayList<Card>();
//			drawDiscard.add(drawnCard);
//			drawDiscard.add(card);
//			if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
//				continue;
//			
//			// Discardable card
//			ArrayList<Card> temp = new ArrayList<Card>(Arrays.asList(card));
//			long tempBitstring = GinRummyUtil.cardsToBitstring(temp);
//			
//			// Bitwise AND so not discard cards in meld
//			if ((tempBitstring & bestMeldsBitstring) == 0)
//			{
//				// Create a potential hand (after discarding a card) from current 11-cards-hand
//				ArrayList<Card> potentialHand = (ArrayList<Card>) cards.clone();
//				potentialHand.remove(card);
//				
//				// Get all features
//				ArrayList<Double> features1 = FeatureEngineer0.getFeatures(potentialHand, estimator, 0.1f);
//				
//				// Convert features arrayList to features array for Gradient Boosting input
//				double[] features2 = new double[features1.size()]; 
//		        for (int i =0; i < features1.size(); i++)
//		            features2[i] = features1.get(i);
//				
//		        // Generate probability of win
//		        double probWin = GradientBoosting.predict(features2)[1];
//		        allProbWin.put(card, probWin);
//			}	
//		}
//		// If there is non-meld cards: 
//		if (!allProbWin.isEmpty()) {
//			// Generate a list of non-meld cards that have maximum probability of win
//		    double maxProb = Collections.max(allProbWin.values());
//			for(HashMap.Entry<Card, Double> entry : allProbWin.entrySet()) {
//			    Card key = entry.getKey();
//			    Double value = entry.getValue();		    
//			    if (value == maxProb) { 
//			    	safelyDiscardableCards.add(key); 
//			    }
//			}
//		}
//		
//		// If there isn't non-meld cards: 
//		else 
//		{
//			for (ArrayList<Card> meld: chosenbestMeldsSet) 
//			{
//				if (meld.size() > 3)
//					safelyDiscardableCards.add(meld.get(meld.size()-1));	
//			}
//		}
//		// Remove the largest card in the safelyDiscardableCards set
//		int minRankInCandidate = 14;
//		int index = 0;
//		
//		for (int i = 0; i < safelyDiscardableCards.size(); i++) {
//			if (minRankInCandidate > safelyDiscardableCards.get(i).getRank()) {
//				minRankInCandidate = safelyDiscardableCards.get(i).getRank();
//				index = i;
//			}
//		}
//		
//		// Among all potentials discards that all have max probability of win, choose a random one.
//		Card discard = safelyDiscardableCards.get(index);
//		return discard;
		return Card.getCard(1);
	}

	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		// Ignore other player discards.  Remove from cards if playerNum is this player.
		if (playerNum == this.playerNum)
		{
			cards.remove(discardedCard);
			this.discardedCard = discardedCard; // save for opponent's faceup
		}
		else
		{
			if (this.discardedCard != null)
//				estimator.reportDrawDiscard(this.discardedCard, opponentDrawnFaceUp, discardedCard);
		}
	}

	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		
		// Check if deadwood of maximal meld is low enough to go out. 
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD))
			return null;
//		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
		if (opponentKnocked)
			return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
		if (bestMeldSets.isEmpty())
			return new ArrayList<ArrayList<Card>>();
		
		// there is a chance to knock with found melds
		if (Math.abs(this.scores[1 - this.playerNum] - this.scores[this.playerNum]) > Q && this.scores[1 - this.playerNum] > R) // play more offensively...
		{
			ArrayList<ArrayList<ArrayList<Card>>> estimatedOpponentMeldSets = new ArrayList<ArrayList<ArrayList<Card>>>();
//			int estimatedOpponentDeadwoodPoints = GinRummyUtil.getDeadwoodPoints(estimatedOpponentMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : estimatedOpponentMeldSets.get(0), estimator.getEstimatedHand());
			if (estimatedOpponentDeadwoodPoints > S)
				return null; // not knock
			else
				return bestMeldSets.get(random.nextInt(bestMeldSets.size())); // knock
		}
		else // else, play more defensively (a.k.a knock ASAP)
			return bestMeldSets.get(random.nextInt(bestMeldSets.size()));
	}

	@Override
	public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
		// Melds ignored by simple player, but could affect which melds to make for complex player.
		if (playerNum != this.playerNum)
			opponentKnocked = true;
	}

	@Override
	public void reportScores(int[] scores) {
		this.scores[this.playerNum] = scores[this.playerNum];
		this.scores[1 - this.playerNum] = scores[1 - this.playerNum];
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
