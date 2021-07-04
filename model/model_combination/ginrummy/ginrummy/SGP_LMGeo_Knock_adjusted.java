import java.util.ArrayList;
import java.util.Random;


/**
 * Implement the simple geometry relationship between card using dummy hand0
 * 
 * @author Sang T. Truong
 * @version 1.0

 */
public class SGP_LMGeo_Knock_adjusted implements GinRummyPlayer {
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
		double _Intercept = -16.0956294526975;
		double _1A = 3.45141167764278;
		double _2A = 0.42848997856996;
		double _3A = -1.75415976629014;
		double _4A = -4.36645701164097;
		double _5A = -5.98536730939654;
		double _6A = -6.58578003254021;
		double _7A = -6.57023739362158;
		double _8A = -6.57023739362158;
		double _9A = -6.57023739362158;
		double _10A = -6.57023739362158;
		double _11A = -6.57023739362158;
		double _12A = -6.57023739362158;
		double _13A = -6.57023739362158;
		double _1B = 3.85352365999794;
		double _2B = 1.021719104284;
		double _3B = -1.47748854836876;
		double _4B = -3.96399978097357;
		double _5B = -6.10486141563935;
		double _6B = -6.35615918022341;
		double _7B = -6.43769908322119;
		double _8B = -6.43769908322119;
		double _9B = -6.43769908322119;
		double _10B = -6.43769908322119;
		double _11B = -6.43769908322119;
		double _12B = -6.43769908322119;
		double _13B = -6.43769908322119;
		double _1C = 3.95549161753014;
		double _2C = 0.854737755750681;
		double _3C = -2.07985631689962;
		double _4C = -4.25705070713902;
		double _5C = -6.30193803601766;
		double _6C = -6.47264316449768;
		double _7C = -6.51186800277745;
		double _8C = -6.51186800277745;
		double _9C = -6.51186800277745;
		double _10C = -6.51186800277745;
		double _11C = -6.51186800277745;
		double _12C = -6.51186800277745;
		double _13C = -6.51186800277745;
		double _1D = 3.86320655867672;
		double _2D = 0.695134961026963;
		double _3D = -1.89475827179889;
		double _4D = -4.25328459884691;
		double _5D = -6.1445240777675;
		double _6D = -6.66607528523397;
		double _7D = -6.38640906966512;
		double _8D = -6.38640906966512;
		double _9D = -6.38640906966512;
		double _10D = -6.38640906966512;
		double _11D = -6.38640906966512;
		double _12D = -6.38640906966512;
		double _13D = -6.38640906966512;
		double _1A1B = 1.03706480646775;
		double _1A1C = 0.521171321053138;
		double _1A1D = 0.838237185919916;
		double _1A2A = 0.747430022401321;
		double _1B1C = 0.480319940559477;
		double _1B1D = 0.318388435589002;
		double _1B2B = 0.608696948100455;
		double _1C1D = 0.392940610600006;
		double _1C2C = 0.392749809285451;
		double _1D2D = 0.711456229581149;
		double _2A2B = 2.30853060741397;
		double _2A2C = 2.14900927909287;
		double _2A2D = 2.77204123683656;
		double _2A3A = 3.27472975515451;
		double _2B2C = 2.11175869798672;
		double _2B2D = 1.99642281501169;
		double _2B3B = 3.09602104825622;
		double _2C2D = 1.84743238186524;
		double _2C3C = 3.75926536877855;
		double _2D3D = 3.91105971032699;
		double _3A3B = 3.37272474485824;
		double _3A3C = 3.49139673171394;
		double _3A3D = 3.68584185489797;
		double _3A4A = 4.72897279245305;
		double _3B3C = 3.49635819897251;
		double _3B3D = 3.08887325222568;
		double _3B4B = 4.6681393835011;
		double _3C3D = 3.45818445772381;
		double _3C4C = 5.03633626760007;
		double _3D4D = 4.63143725846909;
		double _4A4B = 5.03035872825509;
		double _4A4C = 4.85842370116834;
		double _4A4D = 5.19167563145654;
		double _4A5A = 7.04948510141745;
		double _4B4C = 4.53392404909508;
		double _4B4D = 4.69417171434139;
		double _4B5B = 7.11672937398999;
		double _4C4D = 4.58671056306778;
		double _4C5C = 7.08493249199945;
		double _4D5D = 7.28584032689799;
		double _5A5B = 5.68565731698146;
		double _5A5C = 5.80962320834302;
		double _5A5D = 6.14994160072935;
		double _5A6A = 8.60632934389891;
		double _5B5C = 6.27877567134958;
		double _5B5D = 5.97136734380082;
		double _5B6B = 8.33225989438296;
		double _5C5D = 5.86531839262305;
		double _5C6C = 8.88772824185098;
		double _5D6D = 8.73725065948345;
		double _6A6B = 6.76572171784748;
		double _6A6C = 6.38906969064051;
		double _6A6D = 6.22790366184966;
		double _6A7A = 9.96027373013825;
		double _6B6C = 6.13437742057174;
		double _6B6D = 6.04114432912438;
		double _6B7B = 10.0689353791181;
		double _6C6D = 6.5603721235426;
		double _6C7C = 10.1637105281168;
		double _6D7D = 10.0154582393727;
		double _7A7B = 5.81975872948205;
		double _7A7C = 7.6737007803387;
		double _7A7D = 6.94227104647758;
		double _7A8A = 10.5272760242042;
		double _7B7C = 6.92078925550362;
		double _7B7D = 7.23416409071276;
		double _7B8B = 9.73948276416372;
		double _7C7D = 5.48124032482765;
		double _7C8C = 10.0687464106105;
		double _7D8D = 10.1098270252467;
		double _8A8B = 5.75851173891473;
		double _8A8C = 6.39441957222129;
		double _8A8D = 5.76475342031772;
		double _8A9A = 9.3038171143863;
		double _8B8C = 5.36619945463318;
		double _8B8D = 5.89346031026264;
		double _8B9B = 8.58799221094634;
		double _8C8D = 5.71302998906287;
		double _8C9C = 8.93951002485506;
		double _8D9D = 9.36158289569556;
		double _9A9B = 6.04146964677141;
		double _9A9C = 5.77448902462211;
		double _9A9D = 6.35899250624909;
		double _9A10A = 9.51044273269858;
		double _9B9C = 5.68379785628061;
		double _9B9D = 6.06102477103097;
		double _9B10B = 7.55162950672698;
		double _9C9D = 6.45080273299376;
		double _9C10C = 8.10459010406418;
		double _9D10D = 8.96383627036829;
		double _10A10B = 1.80048439989414;
		double _10A10C = 3.65822949346429;
		double _10A10D = 4.56882021089519;
		double _10A11A = 9.21156216109891;
		double _10B10C = 5.62242723923505;
		double _10B10D = 3.65037212074798;
		double _10B11B = 7.70498143749314;
		double _10C10D = 2.74905191588729;
		double _10C11C = 8.56948609041409;
		double _10D11D = 9.52874972188246;
		double _11A11B = 4.86264612033595;
		double _11A11C = 4.49438170516656;
		double _11A11D = 4.46576889642741;
		double _11A12A = 11.011268022626;
		double _11B11C = 3.42101488662556;
		double _11B11D = 4.47836683046058;
		double _11B12B = 11.4145583663597;
		double _11C11D = 4.378151963889;
		double _11C12C = 10.6841899331973;
		double _11D12D = 10.109405954089;
		double _12A12B = 8.87138131619855;
		double _12A12C = 4.34082680127568;
		double _12A12D = 7.10077219465599;
		double _12A13A = 8.21876804838388;
		double _12B12C = 6.9167446388985;
		double _12B12D = 4.09203503436568;
		double _12B13B = 8.76423243040886;
		double _12C12D = 8.6014401843688;
		double _12C13C = 7.20635324401244;
		double _12D13D = 8.25586301360885;
		double _13A13B = 8.00019358804904;
		double _13A13C = 5.76823660371172;
		double _13A13D = 4.93662315363621;
		double _13B13C = 4.47570620210449;
		double _13B13D = 6.58336159055664;
		double _13C13D = 7.97665871716025;		
		
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
