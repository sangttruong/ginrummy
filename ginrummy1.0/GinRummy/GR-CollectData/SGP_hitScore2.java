import java.util.ArrayList;
import java.util.Random;

/**
 * Implements a random dummy Gin Rummy player that has the following trivial, poor play policy: 
 * Ignore opponent actions and cards no longer in play.
 * Draw face up card only if it becomes part of a meld.  Draw face down card otherwise.
 * Discard a highest ranking unmelded card without regard to breaking up pairs, etc.
 * Knock as early as possible.
 * 
 * @author Sang T. Truong
 * @version 1.0
 */
public class SGP_hitScore2 implements GinRummyPlayer {
	private int playerNum;
	@SuppressWarnings("unused")
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Random random = new Random();
	private boolean opponentKnocked = false;
	Card faceUpCard, drawnCard; 
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();

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
		int minDeadwood = Integer.MAX_VALUE;
		int maxHitScore = Integer.MIN_VALUE;
		
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
//		System.out.println(">>>>>>>>> This is player's hand before discard: " + cards);
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
		
		
//		System.out.println("Unmelded cards: " + unmeldedCards);
			
		// Discard a card in unmelded card so that minimize the number of deadwood of remaining card  ------------------------------
		ArrayList<Card> unmeldedCards_minDeadwood = new ArrayList<Card>();
		for (Card card: unmeldedCards)
		{
			ArrayList<Card> remainingCards = (ArrayList<Card>) unmeldedCards.clone();
			remainingCards.remove(card);
			int deadwood = GinRummyUtil.getDeadwoodPoints(remainingCards);
			
			if (deadwood <= minDeadwood) {
				if (deadwood < minDeadwood) {
					minDeadwood = deadwood;
					unmeldedCards_minDeadwood.clear();
				}
				unmeldedCards_minDeadwood.add(card);
			}
		}
//		System.out.println("unmeldedCards_minDeadwood: " + unmeldedCards_minDeadwood);
		
		// Discard a card in unmeldedCards_minDeadwood card so that maximizes the number of hitscore of remaining card --------------------------------------------
		ArrayList<Card> unmeldedCards_minDeadwood_maxHitScore = new ArrayList<Card>();
		for (Card card: unmeldedCards_minDeadwood)
		{
			ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
			remainingCards.remove(card);
			int hitScore = FeatureEngineer0.hitScore(remainingCards);
			
			if (hitScore >= maxHitScore)
			{
				if (hitScore > maxHitScore) {
					maxHitScore = hitScore;
					unmeldedCards_minDeadwood_maxHitScore.clear();
				}
				unmeldedCards_minDeadwood_maxHitScore.add(card);
			}
		}
//		System.out.println("unmeldedCards_minDeadwood_maxHitScore: " + unmeldedCards_minDeadwood_maxHitScore);
		
		//--------------------------------------------------------------------------------------------------------------------------------------
		
		Card discard = unmeldedCards_minDeadwood_maxHitScore.get(random.nextInt(unmeldedCards_minDeadwood_maxHitScore.size()));
		
		// Prevent future repeat of draw, discard pair-----------------------------------------------------------------------------------------
		ArrayList<Card> drawDiscard = new ArrayList<Card>();
		drawDiscard.add(drawnCard);
		drawDiscard.add(discard);
		drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
		return discard;
	}

	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		// Ignore other player discards.  Remove from cards if playerNum is this player.
		if (playerNum == this.playerNum)
			cards.remove(discardedCard);
	}

	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out. 
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD))
			return null;
		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
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
