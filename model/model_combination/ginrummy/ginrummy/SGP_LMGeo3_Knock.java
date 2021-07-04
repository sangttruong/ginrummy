import java.util.ArrayList;
import java.util.Random;


/**
 * Implement the simple geometry relationship between card using dummy hand0
 * 
 * @author Sang T. Truong
 * @version 1.0

 */
public class SGP_LMGeo3_Knock implements GinRummyPlayer {
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
			for (int i = 52; i < geoRelation.length; i++) features.add(geoRelation[i]);
			
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
		double Intercept = -45.3873749875449;
		double _1A1B = 5.91421402789735;
		double _1A1C = 5.44012591678434;
		double _1A1D = 5.74828092066658;
		double _1A2A = 4.10759906428318;
		double _1B1C = 5.661610814922;
		double _1B1D = 5.41554835272252;
		double _1B2B = 4.44459328440157;
		double _1C1D = 5.59803677550527;
		double _1C2C = 4.31063710007439;
		double _1D2D = 4.48880114552288;
		double _2A2B = 4.56705834333398;
		double _2A2C = 4.30577165977985;
		double _2A2D = 4.73451101203723;
		double _2A3A = 4.90476693436836;
		double _2B2C = 4.52072777941652;
		double _2B2D = 4.40737451989583;
		double _2B3B = 5.01349825273016;
		double _2C2D = 4.1219689626094;
		double _2C3C = 5.33125722980943;
		double _2D3D = 5.54130965942169;
		double _3A3B = 4.47437401293113;
		double _3A3C = 4.40869042259459;
		double _3A3D = 4.67742862247647;
		double _3A4A = 4.98506870096541;
		double _3B3C = 4.56560963624092;
		double _3B3D = 4.17753988641393;
		double _3B4B = 5.13594874767466;
		double _3C3D = 4.2412271663776;
		double _3C4C = 5.16588348999894;
		double _3D4D = 4.84563491274766;
		double _4A4B = 4.47384431345704;
		double _4A4C = 4.26300195216087;
		double _4A4D = 4.6702565280998;
		double _4A5A = 5.55912504421626;
		double _4B4C = 4.18182155960095;
		double _4B4D = 4.38082501375502;
		double _4B5B = 5.77269973365461;
		double _4C4D = 4.20158413906563;
		double _4C5C = 5.60400192806234;
		double _4D5D = 5.91061199511315;
		double _5A5B = 4.06118518850422;
		double _5A5C = 4.07518495032431;
		double _5A5D = 4.63494303278253;
		double _5A6A = 6.39809979151721;
		double _5B5C = 4.64915418954651;
		double _5B5D = 4.24195618716191;
		double _5B6B = 6.1019693986048;
		double _5C5D = 4.16379435597021;
		double _5C6C = 6.39270775823721;
		double _5D6D = 6.305684587374;
		double _6A6B = 5.03299779062801;
		double _6A6C = 4.54764602072976;
		double _6A6D = 4.10770450640352;
		double _6A7A = 7.07039933935694;
		double _6B6C = 4.32441707716637;
		double _6B6D = 4.15107214811632;
		double _6B7B = 7.4272788500417;
		double _6C6D = 4.82332855227086;
		double _6C7C = 7.41840875074571;
		double _6D7D = 7.22219747728167;
		double _7A7B = 3.87807179123993;
		double _7A7C = 5.75748415986491;
		double _7A7D = 5.01147801405368;
		double _7A8A = 8.21079594790171;
		double _7B7C = 4.92868056424819;
		double _7B7D = 5.40164997501284;
		double _7B8B = 8.00975264715002;
		double _7C7D = 3.62510301836022;
		double _7C8C = 7.55249049170058;
		double _7D8D = 7.89369441032663;
		double _8A8B = 5.14675437409774;
		double _8A8C = 5.23766978145298;
		double _8A8D = 4.77185529314075;
		double _8A9A = 7.8572091578802;
		double _8B8C = 4.45990698492839;
		double _8B8D = 5.13629566184518;
		double _8B9B = 7.88188944764251;
		double _8C8D = 4.40768620691701;
		double _8C9C = 7.86096501943539;
		double _8D9D = 7.50914358974655;
		double _9A9B = 5.25086373520917;
		double _9A9C = 4.96997917403885;
		double _9A9D = 5.21289196261031;
		double _9A10A = 9.86336210232136;
		double _9B9C = 5.39956120188726;
		double _9B9D = 4.74554580766754;
		double _9B10B = 9.33333109808534;
		double _9C9D = 5.70661379236068;
		double _9C10C = 9.46481474246378;
		double _9D10D = 9.48162673060424;
		double _10A10B = 3.91133224609843;
		double _10A10C = 5.42844015244201;
		double _10A10D = 5.42597066847831;
		double _10A11A = 10.7134598613609;
		double _10B10C = 7.96879289453911;
		double _10B10D = 6.07399535083046;
		double _10B11B = 10.848480272746;
		double _10C10D = 4.89011870514689;
		double _10C11C = 11.0070150730077;
		double _10D11D = 11.5438398980109;
		double _11A11B = 5.76104420494239;
		double _11A11C = 6.08502995105165;
		double _11A11D = 6.01980354387512;
		double _11A12A = 11.3358859171123;
		double _11B11C = 5.11691400159139;
		double _11B11D = 6.50975675910869;
		double _11B12B = 11.6558530996474;
		double _11C11D = 5.50121390099703;
		double _11C12C = 11.0203711295471;
		double _11D12D = 10.3173578070878;
		double _12A12B = 8.71247516226444;
		double _12A12C = 3.39355729470205;
		double _12A12D = 6.62031149221881;
		double _12A13A = 7.93174121801921;
		double _12B12C = 6.00816032810641;
		double _12B12D = 2.64621605889228;
		double _12B13B = 8.22648008424625;
		double _12C12D = 8.70768814397241;
		double _12C13C = 8.03467197546109;
		double _12D13D = 8.95572665478557;
		double _13A13B = 6.80807249476119;
		double _13A13C = 5.78365149183377;
		double _13A13D = 5.18833967689111;
		double _13B13C = 4.70593689733818;
		double _13B13D = 5.76337820802518;
		double _13C13D = 7.98010271336228;
		
		double pred = 
				Intercept
				+  _1A1B * X.get(0)
				+  _1A1C * X.get(1)
				+  _1A1D * X.get(2)
				+  _1A2A * X.get(3)
				+  _1B1C * X.get(4)
				+  _1B1D * X.get(5)
				+  _1B2B * X.get(6)
				+  _1C1D * X.get(7)
				+  _1C2C * X.get(8)
				+  _1D2D * X.get(9)
				+  _2A2B * X.get(10)
				+  _2A2C * X.get(11)
				+  _2A2D * X.get(12)
				+  _2A3A * X.get(13)
				+  _2B2C * X.get(14)
				+  _2B2D * X.get(15)
				+  _2B3B * X.get(16)
				+  _2C2D * X.get(17)
				+  _2C3C * X.get(18)
				+  _2D3D * X.get(19)
				+  _3A3B * X.get(20)
				+  _3A3C * X.get(21)
				+  _3A3D * X.get(22)
				+  _3A4A * X.get(23)
				+  _3B3C * X.get(24)
				+  _3B3D * X.get(25)
				+  _3B4B * X.get(26)
				+  _3C3D * X.get(27)
				+  _3C4C * X.get(28)
				+  _3D4D * X.get(29)
				+  _4A4B * X.get(30)
				+  _4A4C * X.get(31)
				+  _4A4D * X.get(32)
				+  _4A5A * X.get(33)
				+  _4B4C * X.get(34)
				+  _4B4D * X.get(35)
				+  _4B5B * X.get(36)
				+  _4C4D * X.get(37)
				+  _4C5C * X.get(38)
				+  _4D5D * X.get(39)
				+  _5A5B * X.get(40)
				+  _5A5C * X.get(41)
				+  _5A5D * X.get(42)
				+  _5A6A * X.get(43)
				+  _5B5C * X.get(44)
				+  _5B5D * X.get(45)
				+  _5B6B * X.get(46)
				+  _5C5D * X.get(47)
				+  _5C6C * X.get(48)
				+  _5D6D * X.get(49)
				+  _6A6B * X.get(50)
				+  _6A6C * X.get(51)
				+  _6A6D * X.get(52)
				+  _6A7A * X.get(53)
				+  _6B6C * X.get(54)
				+  _6B6D * X.get(55)
				+  _6B7B * X.get(56)
				+  _6C6D * X.get(57)
				+  _6C7C * X.get(58)
				+  _6D7D * X.get(59)
				+  _7A7B * X.get(60)
				+  _7A7C * X.get(61)
				+  _7A7D * X.get(62)
				+  _7A8A * X.get(63)
				+  _7B7C * X.get(64)
				+  _7B7D * X.get(65)
				+  _7B8B * X.get(66)
				+  _7C7D * X.get(67)
				+  _7C8C * X.get(68)
				+  _7D8D * X.get(69)
				+  _8A8B * X.get(70)
				+  _8A8C * X.get(71)
				+  _8A8D * X.get(72)
				+  _8A9A * X.get(73)
				+  _8B8C * X.get(74)
				+  _8B8D * X.get(75)
				+  _8B9B * X.get(76)
				+  _8C8D * X.get(77)
				+  _8C9C * X.get(78)
				+  _8D9D * X.get(79)
				+  _9A9B * X.get(80)
				+  _9A9C * X.get(81)
				+  _9A9D * X.get(82)
				+  _9A10A * X.get(83)
				+  _9B9C * X.get(84)
				+  _9B9D * X.get(85)
				+  _9B10B * X.get(86)
				+  _9C9D * X.get(87)
				+  _9C10C * X.get(88)
				+  _9D10D * X.get(89)
				+  _10A10B * X.get(90)
				+  _10A10C * X.get(91)
				+  _10A10D * X.get(92)
				+  _10A11A * X.get(93)
				+  _10B10C * X.get(94)
				+  _10B10D * X.get(95)
				+  _10B11B * X.get(96)
				+  _10C10D * X.get(97)
				+  _10C11C * X.get(98)
				+  _10D11D * X.get(99)
				+  _11A11B * X.get(100)
				+  _11A11C * X.get(101)
				+  _11A11D * X.get(102)
				+  _11A12A * X.get(103)
				+  _11B11C * X.get(104)
				+  _11B11D * X.get(105)
				+  _11B12B * X.get(106)
				+  _11C11D * X.get(107)
				+  _11C12C * X.get(108)
				+  _11D12D * X.get(109)
				+  _12A12B * X.get(110)
				+  _12A12C * X.get(111)
				+  _12A12D * X.get(112)
				+  _12A13A * X.get(113)
				+  _12B12C * X.get(114)
				+  _12B12D * X.get(115)
				+  _12B13B * X.get(116)
				+  _12C12D * X.get(117)
				+  _12C13C * X.get(118)
				+  _12D13D * X.get(119)
				+  _13A13B * X.get(120)
				+  _13A13C * X.get(121)
				+  _13A13D * X.get(122)
				+  _13B13C * X.get(123)
				+  _13B13D * X.get(124)
				+  _13C13D * X.get(125)
		
				+ dwTolerate * X.get(126);
		
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
