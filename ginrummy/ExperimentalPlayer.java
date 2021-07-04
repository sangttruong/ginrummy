import java.util.ArrayList;
import java.util.Random;

// import org.graalvm.compiler.debug.CSVUtil.Escape;

public class ExperimentalPlayer implements GinRummyPlayer {
	private int playerNum;
	@SuppressWarnings("unused")
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Random random = new Random();
	private boolean opponentKnocked = false;
	Card faceUpCard, drawnCard; 
    ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();

    boolean dwMode;
    int countdown;
    int turnCount;

    // @SuppressWarnings("unchecked")
	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
        drawDiscardBitstrings.clear();


        turnCount = 0;
        dwMode = false;
        countdown = 0;

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(this.cards);
        if (bestMeldSets.isEmpty())
            return;
        if (bestMeldSets.get(0).size() >= 2)
        {
            dwMode = true;
            countdown = 0;
        }
        else if (bestMeldSets.get(0).size() == 1)
            countdown = 2;
        else
            countdown = 3;
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
	
//  @SuppressWarnings("unchecked")
//	@Override
//	public boolean willDrawFaceUpCard(Card cardFaceUp) {
//		// Return true if card would be a part of a meld, false otherwise.
//        this.faceUpCard = cardFaceUp;
//        turnCount++;
//
//		ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
//		newCards.add(cardFaceUp);
//		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
//			if (meld.contains(cardFaceUp))
//				return true;
//         if (turnCount > 10)
//         {
//             // System.out.println("=>>");
//             ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(this.cards);
//             ArrayList<Card> unmeldedCards = (ArrayList<Card>) this.cards.clone();
//             if (bestMeldSets.size() != 0)
//             {
//                 ArrayList<ArrayList<Card>> melds = bestMeldSets.get(0);
//                 for (ArrayList<Card> meld: melds)
//                     for (Card card: meld)
//                         unmeldedCards.remove(card);
//             }
//             boolean[] c = new boolean[52];
//             for (Card card: unmeldedCards)
//                 c[card.getId()] = true;
//             for (int rank = 12; rank >= 0; rank--)
//                 for (int suit = 0; suit < 4; suit++)
//                     if (c[rank + suit * 13] && rank / 2 >= cardFaceUp.getId())
//                         return true;
//         }
//        return false;
//	}

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
        
		// Disallow discard melded cards and repeat of (draw, discard) pair
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(this.cards);
        ArrayList<Card> unmeldedCards = (ArrayList<Card>) this.cards.clone();
        if (bestMeldSets.size() != 0)
        {
            ArrayList<ArrayList<Card>> melds = bestMeldSets.get(0);
            for (ArrayList<Card> meld: melds)
                for (Card card: meld)
                    unmeldedCards.remove(card);
            if (melds.size() >= 2 && countdown != 0)
                countdown = 0;
        }

        ArrayList<Card> validCards = new ArrayList<Card>();
        for (Card card : unmeldedCards) 
        {
            // Cannot draw and discard face up card.
            if (card == drawnCard && drawnCard == faceUpCard)
                continue;
            // Disallow repeat of draw and discard.
            ArrayList<Card> drawDiscard = new ArrayList<Card>();
            drawDiscard.add(drawnCard);
            drawDiscard.add(card);
            if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
                continue;
            
            validCards.add(card);
        }
        
        
        boolean[] c = new boolean[52];
        for (Card card: validCards) c[card.getId()] = true;
        
        if (countdown != 0) // Still leave time for J Q K
        {
            countdown--;
            for (int rank = 7; rank >= 2; rank--) // Only considering rank of 3 to 8
                for (int suit = 0; suit < 4; suit++)
                    if (c[rank + suit * 13])
                    {
                        boolean noNeighbor = true;
                        for (int i = 1; i <= 2; i++)
                            if (c[rank + suit * 13 + i] || 
                            	c[rank + suit * 13 - i] ||
                            	c[rank + ((suit + i) % 4) * 13] || 
                            	c[rank + ((suit - i + 4) % 4) * 13] ) 
                            {
                            	noNeighbor = false;
                                break; // No longer consider this card since it has neighbor
                            }
                        if (noNeighbor) return Card.getCard(rank + suit * 13); // Discard the first encounter card that doesn't have neighbor
                    }
            
            // If there isn't any card that has no neighbor, discard the largest card
            for (int rank = 9; rank >= 0; rank--)
                for (int suit = 0; suit < 4; suit++)
                    if (c[rank + suit * 13])
                        return Card.getCard(rank + suit * 13);
        }

        dwMode = true; // No longer have time for J Q K to find their melds
        for (int rank = 12; rank >= 0; rank--)
        	for (int suit = 0; suit < 4; suit++)
        		if (c[rank + suit * 13])
        			return Card.getCard(rank + suit * 13);
    
        // If all 11 cards are in melds, discard a card from a meld that have size > 3
        Card discard = null;
        for (ArrayList<ArrayList<Card>> melds: bestMeldSets)
            if (discard == null)
                for (ArrayList<Card> meld: melds)
                    if (meld.size() > 3)
                    {
                        discard = meld.get(meld.size() - 1);
                        break;
                    }
        return discard;            
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
