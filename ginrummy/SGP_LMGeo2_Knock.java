import java.util.ArrayList;
import java.util.Random;


/**
 * Implement the simple geometry relationship between card using dummy hand0
 * 
 * @author Sang T. Truong
 * @version 1.0

 */
public class SGP_LMGeo2_Knock implements GinRummyPlayer {
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
			int[] geoRelation = FeatureEngineer0.geoRelation_expanded(remainingCards);
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
		double Intercept = -17.6599062932331;
		double _1A = 3.76579316294364;
		double _2A = 0.543315049441847;
		double _3A = -2.12833204136651;
		double _4A = -5.15675901746169;
		double _5A = -6.91754288002947;
		double _6A = -7.60930319276428;
		double _7A = -7.71891327088526;
		double _8A = -5.79351348971682;
		double _9A = -5.23865730424587;
		double _10A = -1.52519069904483;
		double _11A = -2.4589787949513;
		double _12A = -2.80813226170614;
		double _13A = -4.23383655701001;
		double _1B = 4.13922191325783;
		double _2B = 1.11648413620558;
		double _3B = -1.9310053830289;
		double _4B = -4.84887420146585;
		double _5B = -6.98094143506981;
		double _6B = -7.71204635788109;
		double _7B = -7.33030531460996;
		double _8B = -5.29187174322607;
		double _9B = -4.51891889380405;
		double _10B = -0.201108348684437;
		double _11B = -0.187551752951538;
		double _12B = -4.07756305548333;
		double _13B = -4.41745849544517;
		double _1C = 4.44097603330459;
		double _2C = 0.979852027749305;
		double _3C = -2.24445554888546;
		double _4C = -4.96678049568449;
		double _5C = -7.17625369448699;
		double _6C = -7.3901652193927;
		double _7C = -7.52305412045523;
		double _8C = -5.966934159704;
		double _9C = -4.12612146111328;
		double _10C = -1.12820759134709;
		double _11C = -1.65865607246812;
		double _12C = -2.65453800313534;
		double _13C = -3.66926331227485;
		double _1D = 4.23370836345558;
		double _2D = 0.894821624025553;
		double _3D = -2.22371097210189;
		double _4D = -4.86432643345988;
		double _5D = -7.2437343246756;
		double _6D = -7.61302936360496;
		double _7D = -7.52139461306917;
		double _8D = -5.84360124415279;
		double _9D = -5.33448766494783;
		double _10D = -1.10330043648803;
		double _11D = -0.931309189085079;
		double _12D = -3.48081193429598;
		double _13D = -2.96229489705406;
		double _1A1B = 0.941178431122855;
		double _1A1C = 0.400140873864161;
		double _1A1D = 0.823236300535085;
		double _1A2A = 0.770354346997034;
		double _1A3A = 1.05398579090708;
		double _1B1C = 0.466770812170806;
		double _1B1D = 0.263827401975219;
		double _1B2B = 0.483931578039236;
		double _1B3B = 1.10901759320214;
		double _1C1D = 0.373967105858362;
		double _1C2C = 0.364481783239798;
		double _1C3C = 0.344343587906946;
		double _1D2D = 0.632675826568293;
		double _1D3D = 0.815518900611651;
		double _2A2B = 2.31953204543906;
		double _2A2C = 2.07199808835826;
		double _2A2D = 2.74779108644212;
		double _2A3A = 3.15570209716587;
		double _2A4A = 1.7400129708038;
		double _2B2C = 2.02732032749706;
		double _2B2D = 1.97728317047587;
		double _2B3B = 3.0592838600982;
		double _2B4B = 1.91929225492402;
		double _2C2D = 1.86099108244833;
		double _2C3C = 3.77803826504077;
		double _2C4C = 1.76031529171879;
		double _2D3D = 3.82094969333203;
		double _2D4D = 1.45869653516243;
		double _3A3B = 3.49734822778782;
		double _3A3C = 3.54714104952984;
		double _3A3D = 3.76513478000917;
		double _3A4A = 4.62746503623328;
		double _3A5A = 2.35459101514508;
		double _3B3C = 3.55649260830217;
		double _3B3D = 3.16533029262741;
		double _3B4B = 4.55943039210402;
		double _3B5B = 2.6001787654279;
		double _3C3D = 3.49113722753018;
		double _3C4C = 4.87758386175324;
		double _3C5C = 2.43950560982311;
		double _3D4D = 4.51829242932346;
		double _3D5D = 2.70721785764957;
		double _4A4B = 5.34298245451897;
		double _4A4C = 5.03343021331941;
		double _4A4D = 5.43760297312948;
		double _4A5A = 6.70290492365695;
		double _4A6A = 3.36114416568951;
		double _4B4C = 4.74832607878513;
		double _4B4D = 4.76798345630375;
		double _4B5B = 6.58872847947464;
		double _4B6B = 3.77850502395221;
		double _4C4D = 4.84037321765004;
		double _4C5C = 6.71108917842349;
		double _4C6C = 3.23505424288802;
		double _4D5D = 6.91347643899283;
		double _4D6D = 3.28804944534858;
		double _5A5B = 5.98334600057836;
		double _5A5C = 6.15949809716317;
		double _5A5D = 6.51740948351304;
		double _5A6A = 7.75755149358732;
		double _5A7A = 4.28392017588506;
		double _5B5C = 6.53894669519352;
		double _5B5D = 6.33898975276902;
		double _5B6B = 7.62989499243384;
		double _5B7B = 3.86727321747945;
		double _5C5D = 6.22498111282969;
		double _5C6C = 8.08682437677754;
		double _5C7C = 3.90350140226658;
		double _5D6D = 7.89955500943685;
		double _5D7D = 4.46971876692301;
		double _6A6B = 7.30967429766001;
		double _6A6C = 6.88870479873446;
		double _6A6D = 6.63280788181087;
		double _6A7A = 8.65143030824863;
		double _6A8A = 4.71409292723815;
		double _6B6C = 6.5799130601638;
		double _6B6D = 6.56133002702656;
		double _6B7B = 8.90512336906828;
		double _6B8B = 4.71319593882591;
		double _6C6D = 6.93240402801429;
		double _6C7C = 9.04084866871329;
		double _6C8C = 4.01805365796906;
		double _6D7D = 8.90509147459922;
		double _6D8D = 4.04604415679064;
		double _7A7B = 6.56492626632539;
		double _7A7C = 8.17839023394189;
		double _7A7D = 7.56897948388317;
		double _7A8A = 8.99988676605413;
		double _7A9A = 4.8870313904879;
		double _7B7C = 7.4793338565959;
		double _7B7D = 7.67375554812078;
		double _7B8B = 8.56933340789427;
		double _7B9B = 3.29973855956532;
		double _7C7D = 6.21852428496304;
		double _7C8C = 8.60152539514247;
		double _7C9C = 4.95118135643147;
		double _7D8D = 8.94101468297881;
		double _7D9D = 4.00574913380326;
		double _8A8B = 6.28695076214237;
		double _8A8C = 7.0625426912753;
		double _8A8D = 6.37533178888044;
		double _8A9A = 7.17900911097715;
		double _8A10A = 4.51676100075358;
		double _8B8C = 5.85880162540857;
		double _8B8D = 6.52323569487145;
		double _8B9B = 7.58386932449423;
		double _8B10B = 3.62753754490147;
		double _8C8D = 6.19823901028214;
		double _8C9C = 6.6575970574245;
		double _8C10C = 4.03971036552445;
		double _8D9D = 7.79892754469814;
		double _8D10D = 3.6994968808592;
		double _9A9B = 6.38462471294816;
		double _9A9C = 5.99617720383721;
		double _9A9D = 6.79090870094875;
		double _9A10A = 7.5742017392135;
		double _9A11A = 2.30784597623056;
		double _9B9C = 6.01675585041844;
		double _9B9D = 6.32012557046086;
		double _9B10B = 6.4394732716763;
		double _9B11B = 1.87085849187775;
		double _9C9D = 6.72874359246422;
		double _9C10C = 7.10964156424058;
		double _9C11C = 1.72226156757514;
		double _9D10D = 7.43208509266329;
		double _9D11D = 1.87249674297173;
		double _10A10B = 2.26604273920492;
		double _10A10C = 3.94312469795991;
		double _10A10D = 4.77198697121082;
		double _10A11A = 9.17086394706765;
		double _10A12A = 0.276402278961411;
		double _10B10C = 6.15636039389928;
		double _10B10D = 4.1722752519209;
		double _10B11B = 6.47664180199759;
		double _10B12B = 2.64414145191729;
		double _10C10D = 3.18469950209456;
		double _10C11C = 8.23710910857117;
		double _10C12C = 1.62778446845106;
		double _10D11D = 8.64669514093214;
		double _10D12D = 0.990733342711086;
		double _11A11B = 4.98767337847744;
		double _11A11C = 4.69728975904027;
		double _11A11D = 5.02159050659663;
		double _11A12A = 10.2668193867225;
		double _11A13A = 6.24792290063632;
		double _11B11C = 3.72550882111868;
		double _11B11D = 3.93781348533817;
		double _11B12B = 9.07811635600505;
		double _11B13B = 5.31109316344788;
		double _11C11D = 4.71488622011591;
		double _11C12C = 8.49205285567943;
		double _11C13C = 8.99310437196259;
		double _11D12D = 9.07790935550076;
		double _11D13D = 3.46416032612031;
		double _12A12B = 8.70615215201596;
		double _12A12C = 3.95325395705883;
		double _12A12D = 6.52896889874044;
		double _12A13A = 4.27097929201329;
		double _12B12C = 6.32557148072879;
		double _12B12D = 4.07396113201352;
		double _12B13B = 5.79362974075956;
		double _12C12D = 8.22648198805884;
		double _12C13C = 1.48826660007832;
		double _12D13D = 5.99319481040762;
		double _13A13B = 8.02018807074077;
		double _13A13C = 6.89964187612121;
		double _13A13D = 4.95769503985317;
		double _13B13C = 4.95252588744131;
		double _13B13D = 6.96722812661499;
		double _13C13D = 8.06031453654244;		
		
		double pred = 
				Intercept
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
				+  _1A3A * X.get(56)
				+  _1B1C * X.get(57)
				+  _1B1D * X.get(58)
				+  _1B2B * X.get(59)
				+  _1B3B * X.get(60)
				+  _1C1D * X.get(61)
				+  _1C2C * X.get(62)
				+  _1C3C * X.get(63)
				+  _1D2D * X.get(64)
				+  _1D3D * X.get(65)
				+  _2A2B * X.get(66)
				+  _2A2C * X.get(67)
				+  _2A2D * X.get(68)
				+  _2A3A * X.get(69)
				+  _2A4A * X.get(70)
				+  _2B2C * X.get(71)
				+  _2B2D * X.get(72)
				+  _2B3B * X.get(73)
				+  _2B4B * X.get(74)
				+  _2C2D * X.get(75)
				+  _2C3C * X.get(76)
				+  _2C4C * X.get(77)
				+  _2D3D * X.get(78)
				+  _2D4D * X.get(79)
				+  _3A3B * X.get(80)
				+  _3A3C * X.get(81)
				+  _3A3D * X.get(82)
				+  _3A4A * X.get(83)
				+  _3A5A * X.get(84)
				+  _3B3C * X.get(85)
				+  _3B3D * X.get(86)
				+  _3B4B * X.get(87)
				+  _3B5B * X.get(88)
				+  _3C3D * X.get(89)
				+  _3C4C * X.get(90)
				+  _3C5C * X.get(91)
				+  _3D4D * X.get(92)
				+  _3D5D * X.get(93)
				+  _4A4B * X.get(94)
				+  _4A4C * X.get(95)
				+  _4A4D * X.get(96)
				+  _4A5A * X.get(97)
				+  _4A6A * X.get(98)
				+  _4B4C * X.get(99)
				+  _4B4D * X.get(100)
				+  _4B5B * X.get(101)
				+  _4B6B * X.get(102)
				+  _4C4D * X.get(103)
				+  _4C5C * X.get(104)
				+  _4C6C * X.get(105)
				+  _4D5D * X.get(106)
				+  _4D6D * X.get(107)
				+  _5A5B * X.get(108)
				+  _5A5C * X.get(109)
				+  _5A5D * X.get(110)
				+  _5A6A * X.get(111)
				+  _5A7A * X.get(112)
				+  _5B5C * X.get(113)
				+  _5B5D * X.get(114)
				+  _5B6B * X.get(115)
				+  _5B7B * X.get(116)
				+  _5C5D * X.get(117)
				+  _5C6C * X.get(118)
				+  _5C7C * X.get(119)
				+  _5D6D * X.get(120)
				+  _5D7D * X.get(121)
				+  _6A6B * X.get(122)
				+  _6A6C * X.get(123)
				+  _6A6D * X.get(124)
				+  _6A7A * X.get(125)
				+  _6A8A * X.get(126)
				+  _6B6C * X.get(127)
				+  _6B6D * X.get(128)
				+  _6B7B * X.get(129)
				+  _6B8B * X.get(130)
				+  _6C6D * X.get(131)
				+  _6C7C * X.get(132)
				+  _6C8C * X.get(133)
				+  _6D7D * X.get(134)
				+  _6D8D * X.get(135)
				+  _7A7B * X.get(136)
				+  _7A7C * X.get(137)
				+  _7A7D * X.get(138)
				+  _7A8A * X.get(139)
				+  _7A9A * X.get(140)
				+  _7B7C * X.get(141)
				+  _7B7D * X.get(142)
				+  _7B8B * X.get(143)
				+  _7B9B * X.get(144)
				+  _7C7D * X.get(145)
				+  _7C8C * X.get(146)
				+  _7C9C * X.get(147)
				+  _7D8D * X.get(148)
				+  _7D9D * X.get(149)
				+  _8A8B * X.get(150)
				+  _8A8C * X.get(151)
				+  _8A8D * X.get(152)
				+  _8A9A * X.get(153)
				+  _8A10A * X.get(154)
				+  _8B8C * X.get(155)
				+  _8B8D * X.get(156)
				+  _8B9B * X.get(157)
				+  _8B10B * X.get(158)
				+  _8C8D * X.get(159)
				+  _8C9C * X.get(160)
				+  _8C10C * X.get(161)
				+  _8D9D * X.get(162)
				+  _8D10D * X.get(163)
				+  _9A9B * X.get(164)
				+  _9A9C * X.get(165)
				+  _9A9D * X.get(166)
				+  _9A10A * X.get(167)
				+  _9A11A * X.get(168)
				+  _9B9C * X.get(169)
				+  _9B9D * X.get(170)
				+  _9B10B * X.get(171)
				+  _9B11B * X.get(172)
				+  _9C9D * X.get(173)
				+  _9C10C * X.get(174)
				+  _9C11C * X.get(175)
				+  _9D10D * X.get(176)
				+  _9D11D * X.get(177)
				+  _10A10B * X.get(178)
				+  _10A10C * X.get(179)
				+  _10A10D * X.get(180)
				+  _10A11A * X.get(181)
				+  _10A12A * X.get(182)
				+  _10B10C * X.get(183)
				+  _10B10D * X.get(184)
				+  _10B11B * X.get(185)
				+  _10B12B * X.get(186)
				+  _10C10D * X.get(187)
				+  _10C11C * X.get(188)
				+  _10C12C * X.get(189)
				+  _10D11D * X.get(190)
				+  _10D12D * X.get(191)
				+  _11A11B * X.get(192)
				+  _11A11C * X.get(193)
				+  _11A11D * X.get(194)
				+  _11A12A * X.get(195)
				+  _11A13A * X.get(196)
				+  _11B11C * X.get(197)
				+  _11B11D * X.get(198)
				+  _11B12B * X.get(199)
				+  _11B13B * X.get(200)
				+  _11C11D * X.get(201)
				+  _11C12C * X.get(202)
				+  _11C13C * X.get(203)
				+  _11D12D * X.get(204)
				+  _11D13D * X.get(205)
				+  _12A12B * X.get(206)
				+  _12A12C * X.get(207)
				+  _12A12D * X.get(208)
				+  _12A13A * X.get(209)
				+  _12B12C * X.get(210)
				+  _12B12D * X.get(211)
				+  _12B13B * X.get(212)
				+  _12C12D * X.get(213)
				+  _12C13C * X.get(214)
				+  _12D13D * X.get(215)
				+  _13A13B * X.get(216)
				+  _13A13C * X.get(217)
				+  _13A13D * X.get(218)
				+  _13B13C * X.get(219)
				+  _13B13D * X.get(220)
				+  _13C13D * X.get(221)
		
				+ dwTolerate * X.get(222);
		
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
