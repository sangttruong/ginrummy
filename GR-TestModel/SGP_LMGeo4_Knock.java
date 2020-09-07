import java.util.ArrayList;
import java.util.Random;


/**
 * Implement the simple geometry relationship between card using dummy hand0
 * 
 * @author Sang T. Truong
 * @version 1.0

 */
public class SGP_LMGeo4_Knock implements GinRummyPlayer {
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
			// features.add(gamestateIndex);
			
			// Number of melds
			// features.add(bestMelds.size());	
						
			// Number of hitscore
			// int hitscore = FeatureEngineer0.hitScore(remainingCards);
			// features.add(hitscore);
			
			// Count of each rank
			// ArrayList<Integer> rankCount = FeatureEngineer0.rank(remainingCards);
			// for (Integer rank: rankCount) features.add(rank);
			
			// Additional dummy
			// int deadwood_1_10 = (deadwood > 0 && deadwood < 11)? 1 : 0;
			// features.add(deadwood_1_10);
			// int deadwood_equal0 = (deadwood == 0)? 1 : 0;
			// features.add(deadwood_equal0);
			
			// Dummy Hand0
			int[] geoRelation = FeatureEngineer0.geoRelation(remainingCards);
			for (int i: geoRelation) features.add(i);
			
			// Deadwood
			int deadwood = GinRummyUtil.getDeadwoodPoints(bestMelds, remainingCards);
			features.add(deadwood);
			
			double dwTolerate;
			if (gamestateIndex <= 4) dwTolerate = 0;
			else dwTolerate = -15;
			
			double predScore = linearRegression(features, dwTolerate);
			
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
		
	public double linearRegression (ArrayList<Integer> X, double dwTolerate)
	{	
		int u = 4;
		int s = 2;
		
		double _Intercept = 0;
		double _1A = u;
		double _2A = u-1;
		double _3A = u-2;
		double _4A = u-3;
		double _5A = u-4;
		double _6A = u-5;
		double _7A = u-6;
		double _8A = u-7;
		double _9A = u-8;
		double _10A = u-9;
		double _11A = u-10;
		double _12A = u-11;
		double _13A = u-12;
		double _1B = u;
		double _2B = u-1;
		double _3B = u-2;
		double _4B = u-3;
		double _5B = u-4;
		double _6B = u-5;
		double _7B = u-6;
		double _8B = u-7;
		double _9B = u-8;
		double _10B = u-9;
		double _11B = u-10;
		double _12B = u-11;
		double _13B = u-12;
		double _1C = u;
		double _2C = u-1;
		double _3C = u-2;
		double _4C = u-3;
		double _5C = u-4;
		double _6C = u-5;
		double _7C = u-6;
		double _8C = u-7;
		double _9C = u-8;
		double _10C = u-9;
		double _11C = u-10;
		double _12C = u-11;
		double _13C = u-12;
		double _1D = u;
		double _2D = u-1;
		double _3D = u-2;
		double _4D = u-3;
		double _5D = u-4;
		double _6D = u-5;
		double _7D = u-6;
		double _8D = u-7;
		double _9D = u-8;
		double _10D = u-9;
		double _11D = u-10;
		double _12D = u-11;
		double _13D = u-12;
		double _1A1B = s;
		double _1A1C = s;
		double _1A1D = s;
		double _1A2A = s;
		double _1B1C = s;
		double _1B1D = s;
		double _1B2B = s;
		double _1C1D = s;
		double _1C2C = s;
		double _1D2D = s;
		double _2A2B = s;
		double _2A2C = s;
		double _2A2D = s;
		double _2A3A = s;
		double _2B2C = s;
		double _2B2D = s;
		double _2B3B = s;
		double _2C2D = s;
		double _2C3C = s;
		double _2D3D = s;
		double _3A3B = s;
		double _3A3C = s;
		double _3A3D = s;
		double _3A4A = s;
		double _3B3C = s;
		double _3B3D = s;
		double _3B4B = s;
		double _3C3D = s;
		double _3C4C = s;
		double _3D4D = s;
		double _4A4B = s;
		double _4A4C = s;
		double _4A4D = s;
		double _4A5A = s;
		double _4B4C = s;
		double _4B4D = s;
		double _4B5B = s;
		double _4C4D = s;
		double _4C5C = s;
		double _4D5D = s;
		double _5A5B = s;
		double _5A5C = s;
		double _5A5D = s;
		double _5A6A = s;
		double _5B5C = s;
		double _5B5D = s;
		double _5B6B = s;
		double _5C5D = s;
		double _5C6C = s;
		double _5D6D = s;
		double _6A6B = s;
		double _6A6C = s;
		double _6A6D = s;
		double _6A7A = s;
		double _6B6C = s;
		double _6B6D = s;
		double _6B7B = s;
		double _6C6D = s;
		double _6C7C = s;
		double _6D7D = s;
		double _7A7B = s;
		double _7A7C = s;
		double _7A7D = s;
		double _7A8A = s;
		double _7B7C = s;
		double _7B7D = s;
		double _7B8B = s;
		double _7C7D = s;
		double _7C8C = s;
		double _7D8D = s;
		double _8A8B = s;
		double _8A8C = s;
		double _8A8D = s;
		double _8A9A = s;
		double _8B8C = s;
		double _8B8D = s;
		double _8B9B = s;
		double _8C8D = s;
		double _8C9C = s;
		double _8D9D = s;
		double _9A9B = s;
		double _9A9C = s;
		double _9A9D = s;
		double _9A10A = s;
		double _9B9C = s;
		double _9B9D = s;
		double _9B10B = s;
		double _9C9D = s;
		double _9C10C = s;
		double _9D10D = s;
		double _10A10B = s;
		double _10A10C = s;
		double _10A10D = s;
		double _10A11A = s;
		double _10B10C = s;
		double _10B10D = s;
		double _10B11B = s;
		double _10C10D = s;
		double _10C11C = s;
		double _10D11D = s;
		double _11A11B = s;
		double _11A11C = s;
		double _11A11D = s;
		double _11A12A = s;
		double _11B11C = s;
		double _11B11D = s;
		double _11B12B = s;
		double _11C11D = s;
		double _11C12C = s;
		double _11D12D = s;
		double _12A12B = s;
		double _12A12C = s;
		double _12A12D = s;
		double _12A13A = s;
		double _12B12C = s;
		double _12B12D = s;
		double _12B13B = s;
		double _12C12D = s;
		double _12C13C = s;
		double _12D13D = s;
		double _13A13B = s;
		double _13A13C = s;
		double _13A13D = s;
		double _13B13C = s;
		double _13B13D = s;
		double _13C13D = s;	
		
		double pred = 
				_Intercept
				+  _1A * X.get(0)
				+  _2A * X.get(1)
				+  _3A * X.get(2)
				+  _4A * X.get(3)
				+  _5A * X.get(4)
				+  _6A * X.get(5)
				+  _7A * X.get(6)
				+  _8A * X.get(7)
				+  _9A * X.get(8)
				+  _10A * X.get(9)
				+  _11A * X.get(10)
				+  _12A * X.get(11)
				+  _13A * X.get(12)
				+  _1B * X.get(13)
				+  _2B * X.get(14)
				+  _3B * X.get(15)
				+  _4B * X.get(16)
				+  _5B * X.get(17)
				+  _6B * X.get(18)
				+  _7B * X.get(19)
				+  _8B * X.get(20)
				+  _9B * X.get(21)
				+  _10B * X.get(22)
				+  _11B * X.get(23)
				+  _12B * X.get(24)
				+  _13B * X.get(25)
				+  _1C * X.get(26)
				+  _2C * X.get(27)
				+  _3C * X.get(28)
				+  _4C * X.get(29)
				+  _5C * X.get(30)
				+  _6C * X.get(31)
				+  _7C * X.get(32)
				+  _8C * X.get(33)
				+  _9C * X.get(34)
				+  _10C * X.get(35)
				+  _11C * X.get(36)
				+  _12C * X.get(37)
				+  _13C * X.get(38)
				+  _1D * X.get(39)
				+  _2D * X.get(40)
				+  _3D * X.get(41)
				+  _4D * X.get(42)
				+  _5D * X.get(43)
				+  _6D * X.get(44)
				+  _7D * X.get(45)
				+  _8D * X.get(46)
				+  _9D * X.get(47)
				+  _10D * X.get(48)
				+  _11D * X.get(49)
				+  _12D * X.get(50)
				+  _13D * X.get(51)
				+  _1A1B * X.get(52)
				+  _1A1C * X.get(53)
				+  _1A1D * X.get(54)
				+  _1A2A * X.get(55)
				+  _1B1C * X.get(56)
				+  _1B1D * X.get(57)
				+  _1B2B * X.get(58)
				+  _1C1D * X.get(59)
				+  _1C2C * X.get(60)
				+  _1D2D * X.get(61)
				+  _2A2B * X.get(62)
				+  _2A2C * X.get(63)
				+  _2A2D * X.get(64)
				+  _2A3A * X.get(65)
				+  _2B2C * X.get(66)
				+  _2B2D * X.get(67)
				+  _2B3B * X.get(68)
				+  _2C2D * X.get(69)
				+  _2C3C * X.get(70)
				+  _2D3D * X.get(71)
				+  _3A3B * X.get(72)
				+  _3A3C * X.get(73)
				+  _3A3D * X.get(74)
				+  _3A4A * X.get(75)
				+  _3B3C * X.get(76)
				+  _3B3D * X.get(77)
				+  _3B4B * X.get(78)
				+  _3C3D * X.get(79)
				+  _3C4C * X.get(80)
				+  _3D4D * X.get(81)
				+  _4A4B * X.get(82)
				+  _4A4C * X.get(83)
				+  _4A4D * X.get(84)
				+  _4A5A * X.get(85)
				+  _4B4C * X.get(86)
				+  _4B4D * X.get(87)
				+  _4B5B * X.get(88)
				+  _4C4D * X.get(89)
				+  _4C5C * X.get(90)
				+  _4D5D * X.get(91)
				+  _5A5B * X.get(92)
				+  _5A5C * X.get(93)
				+  _5A5D * X.get(94)
				+  _5A6A * X.get(95)
				+  _5B5C * X.get(96)
				+  _5B5D * X.get(97)
				+  _5B6B * X.get(98)
				+  _5C5D * X.get(99)
				+  _5C6C * X.get(100)
				+  _5D6D * X.get(101)
				+  _6A6B * X.get(102)
				+  _6A6C * X.get(103)
				+  _6A6D * X.get(104)
				+  _6A7A * X.get(105)
				+  _6B6C * X.get(106)
				+  _6B6D * X.get(107)
				+  _6B7B * X.get(108)
				+  _6C6D * X.get(109)
				+  _6C7C * X.get(110)
				+  _6D7D * X.get(111)
				+  _7A7B * X.get(112)
				+  _7A7C * X.get(113)
				+  _7A7D * X.get(114)
				+  _7A8A * X.get(115)
				+  _7B7C * X.get(116)
				+  _7B7D * X.get(117)
				+  _7B8B * X.get(118)
				+  _7C7D * X.get(119)
				+  _7C8C * X.get(120)
				+  _7D8D * X.get(121)
				+  _8A8B * X.get(122)
				+  _8A8C * X.get(123)
				+  _8A8D * X.get(124)
				+  _8A9A * X.get(125)
				+  _8B8C * X.get(126)
				+  _8B8D * X.get(127)
				+  _8B9B * X.get(128)
				+  _8C8D * X.get(129)
				+  _8C9C * X.get(130)
				+  _8D9D * X.get(131)
				+  _9A9B * X.get(132)
				+  _9A9C * X.get(133)
				+  _9A9D * X.get(134)
				+  _9A10A * X.get(135)
				+  _9B9C * X.get(136)
				+  _9B9D * X.get(137)
				+  _9B10B * X.get(138)
				+  _9C9D * X.get(139)
				+  _9C10C * X.get(140)
				+  _9D10D * X.get(141)
				+  _10A10B * X.get(142)
				+  _10A10C * X.get(143)
				+  _10A10D * X.get(144)
				+  _10A11A * X.get(145)
				+  _10B10C * X.get(146)
				+  _10B10D * X.get(147)
				+  _10B11B * X.get(148)
				+  _10C10D * X.get(149)
				+  _10C11C * X.get(150)
				+  _10D11D * X.get(151)
				+  _11A11B * X.get(152)
				+  _11A11C * X.get(153)
				+  _11A11D * X.get(154)
				+  _11A12A * X.get(155)
				+  _11B11C * X.get(156)
				+  _11B11D * X.get(157)
				+  _11B12B * X.get(158)
				+  _11C11D * X.get(159)
				+  _11C12C * X.get(160)
				+  _11D12D * X.get(161)
				+  _12A12B * X.get(162)
				+  _12A12C * X.get(163)
				+  _12A12D * X.get(164)
				+  _12A13A * X.get(165)
				+  _12B12C * X.get(166)
				+  _12B12D * X.get(167)
				+  _12B13B * X.get(168)
				+  _12C12D * X.get(169)
				+  _12C13C * X.get(170)
				+  _12D13D * X.get(171)
				+  _13A13B * X.get(172)
				+  _13A13C * X.get(173)
				+  _13A13D * X.get(174)
				+  _13B13C * X.get(175)
				+  _13B13D * X.get(176)
				+  _13C13D * X.get(177)
		
				+ dwTolerate * X.get(178);
		
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
