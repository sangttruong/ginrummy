import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Sang T. Truong
 * @version 1.0
 */
public class SGP_NN_Knock_OHE_server implements GinRummyPlayer {
	private int playerNum;
	@SuppressWarnings("unused")
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Random random = new Random();
	private boolean opponentKnocked = false;
	Card faceUpCard, drawnCard; 
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
	private static int gamestateIndex = 1;
	int turnCount = 0;
	HandEstimator estimator = new HandEstimator();
	private int totalDiscarded = 0;
	ArrayList<Double> ratios = new ArrayList<Double>();
    
	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
		estimator.init();
		ArrayList<Card> hand = new ArrayList<Card>();
		for (Card c : cards)
			hand.add(c);
		estimator.setKnown(hand, false);
		// estimator.print();
		totalDiscarded = 0;
	}

	@Override
	public boolean willDrawFaceUpCard(Card card) {
		// Return true if card would be a part of a meld, false otherwise.
		estimator.setKnown(card, false);
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
		this.drawnCard = drawnCard;
		if (playerNum == this.playerNum) {
			cards.add(drawnCard);
			estimator.setKnown(drawnCard, false);
		}
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
		ArrayList<ArrayList<Integer>> features = new ArrayList<ArrayList<Integer>>();
		ArrayList<Card> features_cards = new ArrayList<Card>();
		ArrayList<Integer> deadwoods = new ArrayList<Integer>();
		ArrayList<int[]> dummyHands0 = new ArrayList<int[]>();		
		
		ArrayList<ArrayList<Float>> xfo_list = new ArrayList<ArrayList<Float>>();

		double dwTolerate;
		if (gamestateIndex <= 4) dwTolerate = 0;
		else dwTolerate = -15;
		
		// OHE
        double[] prob = estimator.prob;
        double[] ISRUprob = ISRU(prob);

		for (Card card: unmeldedCards)
		{	
			ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
			remainingCards.remove(card);
			ArrayList<Integer> feature = new ArrayList<Integer>();
			
			ArrayList<ArrayList<ArrayList<Card>>> bestMeldSet = GinRummyUtil.cardsToBestMeldSets(remainingCards);
			ArrayList<ArrayList<Card>> bestMelds = new ArrayList<ArrayList<Card>>();
			if (!bestMeldSet.isEmpty()) bestMelds = bestMeldSet.get(0);
			
			//52. GamestateNum
			feature.add(gamestateIndex);
	
            //53. RunNum0
            int runNumber0 = (int) FeatureEngineer0.run(remainingCards).get(0);
			feature.add(runNumber0);
                        
			//54. SetNum0
			int setNumber0 = (int) FeatureEngineer0.set(remainingCards).get(0);
			feature.add(setNumber0);
			
			//55. Deadwood0
			int deadwood = GinRummyUtil.getDeadwoodPoints(bestMelds, remainingCards);
			deadwoods.add(deadwood);
			feature.add(deadwood);			

			//56. Hitscore0
			int hitscore = FeatureEngineer0.hitScore(remainingCards); 
			feature.add(hitscore);

			// Geo-relation
			// int[] geoRelation = FeatureEngineer0.geoRelation(remainingCards);
			// for (int i: geoRelation) feature.add(i);
			
			features.add(feature);
			features_cards.add(card);

			// Dummy hand
			int[] dummyHand0 = new int[52];
            for (Card _card_: remainingCards) dummyHand0[_card_.getId()] = 1;
			dummyHands0.add(dummyHand0);

			// x, f, o
			ArrayList<Float> xfo = new ArrayList<Float>();
			for (int i: dummyHand0) xfo.add( (float) i);
			for (int i: feature) xfo.add( (float) i);
			for (double i: ISRUprob) xfo.add( (float) i);
			// System.out.println("HEREEEEEEEEEEEEEEE");
			// System.out.println(xfo);
			xfo_list.add(xfo);
		}
		
		double[] q_values = new double[features.size()];
		// double[] q_values_linear = new double[features.size()];
		
		try {
			q_values = NeuralNet(xfo_list);
			for(int i =0; i< features.size(); i++)
			{
				q_values[i] = q_values[i] + dwTolerate*deadwoods.get(i);
				// q_values_linear[i] = linearRegression(features.get(i), dwTolerate*deadwoods.get(i));
			}
			for(int i =0; i< features.size(); i++)
			{
				// System.out.println("NEURAL: " + q_values[i]);
				// System.out.println("LINEAR: " + q_values_linear[i]);
				// System.out.println("DIFFERENCE: >>>>>>>>>>>>>>>> " + (q_values[i] - q_values_linear[i]));
			}
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	
		double maxPredScore = -Double.MAX_VALUE;
		for (int i = 0; i < features.size(); i++)
		{
			q_values[i] = q_values[i] + dwTolerate;
			if (q_values[i] >= maxPredScore)
			{
				if (q_values[i] > maxPredScore) 
				{
					maxPredScore = q_values[i];
					unmeldedCards_maxPredScore.clear();
				}
				unmeldedCards_maxPredScore.add(features_cards.get(i));
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
	
	public double[] ISRU(double[] m)
	{
		double[] n = new double[m.length];
		int z = 1;
		for(int i = 0; i < m.length; i++)
		{
			if((m[i] != 0.0) && (m[i] != 1.0))
				n[i] = m[i]/Math.sqrt(z+m[i]*m[i]);
			else
				n[i] = m[i];
		}
		return n;
	}
	
	private double[] NeuralNet(ArrayList<ArrayList<Float>> xfo_list) throws IOException
	{
		double[] q_values = new double[xfo_list.size()];

		for (int i = 0; i < xfo_list.size(); i++)
		{
			// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>");
			// System.out.println(xfo_list.get(i));
		    try {
					Socket s = new Socket("127.0.0.1", 8888);
	            	PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	            	BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				
	            	// Send a number to python
	            	out.println(xfo_list.get(i));		
				
		    	// Receive a number from python
		    	String message;
	            	while ((message = in.readLine()) != null) 
	            	{
	            	    message = message.replace("[", "");
	            	    message = message.replace("]", "");
	            	    q_values[i] = Double.valueOf(message);
	                }
	
	            	s.close();
			}
		    catch (UnknownHostException e){
			System.out.println("Fail1");
			e.printStackTrace();
			}
		    catch (IOException e) {
			System.out.println("Fail2");
			e.printStackTrace();
			}
		}
		
// Brian's suggestion		
//		try
//		{
//			Socket s = new Socket("127.0.0.1", 8000);
//            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
//            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//			
//			for (int i = 0; i < features.size(); i++)
//	        {
//				// Send a number to python
//		        out.println(features.get(i));		
//					
//		        // Receive a number from python
//				String message = in.readLine();
//				System.out.println(message);
//            	message = message.replace("[", "");
//            	message = message.replace("]", "");
//            	q_values[i] = Double.valueOf(message);
//	            s.close();
//			}
//		}
//		catch (UnknownHostException e)
//		{
//			System.out.println("Fail1");
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			System.out.println("Fail2");
//			e.printStackTrace();
//		}
		
		return q_values;
	}		
	
	public double linearRegression (ArrayList<Integer> X, double dwTolerate)
	{	
		double _Intercept = -16.0358630877818;
		double _1A = 2.38391245053969;
		double _2A = -0.653307858652176;
		double _3A = -2.86258722025435;
		double _4A = -5.42978397395241;
		double _5A = -6.66407576869799;
		double _6A = -6.85842083677801;
		double _7A = -6.42506710518364;
		double _8A = -5.12940761243172;
		double _9A = -4.16788535607452;
		double _10A = 1.18239738040653;
		double _11A = -0.0512261752608658;
		double _12A = -3.06244961019984;
		double _13A = -1.59325757354407;
		double _1B = 2.75552712148632;
		double _2B = -0.197774160321768;
		double _3B = -2.71727652527155;
		double _4B = -5.16454461088601;
		double _5B = -6.92654028801247;
		double _6B = -6.97254526047306;
		double _7B = -6.35046429934668;
		double _8B = -4.77355931301672;
		double _9B = -4.5509322529839;
		double _10B = -0.663023312287754;
		double _11B = -0.321325019379449;
		double _12B = -1.34805853488352;
		double _13B = -0.201584282674902;
		double _1C = 3.03980940903901;
		double _2C = -0.0876237303335428;
		double _3C = -3.06667944612995;
		double _4C = -5.23519932120778;
		double _5C = -6.74220023163011;
		double _6C = -6.98315874843669;
		double _7C = -6.30016277345915;
		double _8C = -5.16978845528237;
		double _9C = -4.30194607372566;
		double _10C = -0.992635554317409;
		double _11C = -0.662575115259659;
		double _12C = -1.28417209678056;
		double _13C = -3.32908721532766;
		double _1D = 2.8461486188776;
		double _2D = -0.539996747293985;
		double _3D = -3.06227938770507;
		double _4D = -5.35169194309502;
		double _5D = -6.94708209905397;
		double _6D = -6.98527401991845;
		double _7D = -6.49758669593362;
		double _8D = -5.08172173599578;
		double _9D = -4.50415655490295;
		double _10D = -0.168095803812492;
		double _11D = 0.155315466430239;
		double _12D = -2.68016165973043;
		double _13D = -3.66336896469807;
		double _1A1B = 2.46347682048199;
		double _1A1C = 1.65759845276652;
		double _1A1D = 2.02766279507341;
		double _1A2A = 1.62653569663349;
		double _1B1C = 1.5970844126478;
		double _1B1D = 1.85843492607873;
		double _1B2B = 1.55603210582852;
		double _1C1D = 1.83282311185115;
		double _1C2C = 1.21135812653644;
		double _1D2D = 1.7160306903748;
		double _2A2B = 3.21613344471829;
		double _2A2C = 2.97692921101697;
		double _2A2D = 3.67767407036973;
		double _2A3A = 4.68375077954968;
		double _2B2C = 3.32993229074257;
		double _2B2D = 3.20419384162874;
		double _2B3B = 4.54725070042668;
		double _2C2D = 2.91745918371554;
		double _2C3C = 5.0001310389149;
		double _2D3D = 5.43699851942021;
		double _3A3B = 4.36525014355493;
		double _3A3C = 4.47399880904702;
		double _3A3D = 4.43085242196673;
		double _3A4A = 6.78685234214522;
		double _3B3C = 4.45914141179784;
		double _3B3D = 4.22777296476071;
		double _3B4B = 6.32938954312185;
		double _3C3D = 4.25642832330394;
		double _3C4C = 6.67349101976873;
		double _3D4D = 6.45876369506189;
		double _4A4B = 5.8567637701312;
		double _4A4C = 5.87058957675247;
		double _4A4D = 5.9814411601181;
		double _4A5A = 8.80014082143865;
		double _4B4C = 5.62447422018504;
		double _4B4D = 5.52766887942127;
		double _4B5B = 8.75439126349604;
		double _4C4D = 5.50443119650056;
		double _4C5C = 8.63331035345993;
		double _4D5D = 8.95410238530292;
		double _5A5B = 6.58411416661292;
		double _5A5C = 6.38777049027133;
		double _5A5D = 6.78007640353455;
		double _5A6A = 9.88591431631086;
		double _5B5C = 6.94833601644962;
		double _5B5D = 6.82731921782617;
		double _5B6B = 10.0380735137415;
		double _5C5D = 6.52838383759438;
		double _5C6C = 10.3731195476512;
		double _5D6D = 10.499960772825;
		double _6A6B = 6.86566701795046;
		double _6A6C = 6.86106601003616;
		double _6A6D = 6.78124658028284;
		double _6A7A = 10.855155345928;
		double _6B6C = 7.08051134355844;
		double _6B6D = 6.76408115602726;
		double _6B7B = 11.3096700721422;
		double _6C6D = 6.84470388669654;
		double _6C7C = 10.9018920299289;
		double _6D7D = 11.0111211242704;
		double _7A7B = 6.31207611599451;
		double _7A7C = 6.93127643606108;
		double _7A7D = 6.75469761537415;
		double _7A8A = 10.967707530923;
		double _7B7C = 6.88246840382261;
		double _7B7D = 6.86266875078296;
		double _7B8B = 10.6837835392169;
		double _7C7D = 6.39127288117818;
		double _7C8C = 11.1151441344715;
		double _7D8D = 11.0526295168356;
		double _8A8B = 5.66249503884954;
		double _8A8C = 6.98704729334278;
		double _8A8D = 6.09632055780102;
		double _8A9A = 9.30370494587142;
		double _8B8C = 5.37526829625471;
		double _8B8D = 6.51781823498965;
		double _8B9B = 9.5686031163186;
		double _8C8D = 5.59167063022088;
		double _8C9C = 9.80840768256261;
		double _8D9D = 9.66978407296532;
		double _9A9B = 5.43528253446452;
		double _9A9C = 6.31697095835594;
		double _9A9D = 6.28857780297025;
		double _9A10A = 7.7889484267184;
		double _9B9C = 6.30213306747358;
		double _9B9D = 6.33708580053779;
		double _9B10B = 8.89156511598309;
		double _9C9D = 5.93891698374073;
		double _9C10C = 8.47516129908212;
		double _9D10D = 7.98962572991879;
		double _10A10B = 2.623078701167;
		double _10A10C = 4.62616205495013;
		double _10A10D = 1.21783758758286;
		double _10A11A = 6.64885359600215;
		double _10B10C = 3.52081708223798;
		double _10B10D = 5.17119042694673;
		double _10B11B = 7.75162418001444;
		double _10C10D = 3.49120487273401;
		double _10C11C = 8.56880154159356;
		double _10D11D = 8.23095031667371;
		double _11A11B = 4.11612174404749;
		double _11A11C = 2.29776099514625;
		double _11A11D = 5.02991458510697;
		double _11A12A = 9.82976666436145;
		double _11B11C = 4.51810711944246;
		double _11B11D = 2.45570453478118;
		double _11B12B = 7.97621150649638;
		double _11C11D = 3.7052169395272;
		double _11C12C = 8.66027932472605;
		double _11D12D = 8.78255295704099;
		double _12A12B = 4.25651226595854;
		double _12A12C = 6.83453994786282;
		double _12A12D = 5.85634371120562;
		double _12A13A = 5.49492805441297;
		double _12B12C = 4.4734531579646;
		double _12B12D = 5.81438323723192;
		double _12B13B = 5.0600654361497;
		double _12C12D = 3.56977527149103;
		double _12C13C = 7.33717769144722;
		double _12D13D = 8.38279554056547;
		double _13A13B = -0.486362939769739;
		double _13A13C = 7.781488646998;
		double _13A13D = 7.93064762364589;
		double _13B13C = 6.65193824403704;
		double _13B13D = 7.48679923724598;
		double _13C13D = 2.89516768426351;	
		
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
		
//				+ dwTolerate * X.get(178);
				+ dwTolerate;
		
		return pred;
	}
	
	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		totalDiscarded++;
		if (playerNum == this.playerNum) {
			cards.remove(discardedCard);
	}
		else {
			estimator.reportDrawDiscard(faceUpCard, faceUpCard == drawnCard, discardedCard);
		}
		faceUpCard = discardedCard;
		// estimator.print();
	}
	
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
		
	}

	@Override
	public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
		if (playerNum != this.playerNum) { // opponent hand
			// Record est. likelihood of actual opponent hand
			int numCards = 0;
			double estProb = 1;
			for (Card card : hand) {
				numCards++;
				if (!estimator.known[card.getId()])
					estProb *= estimator.prob[card.getId()];
			}
			// Record uniform likelihood of actual opponent hand
			double uniformProb = 1;
			// Compute the number of possible cards that may be those unknown in the opponent's hand
			System.out.println("Number of opponent cards known: " + (hand.size() - estimator.numUnknownInHand));
			System.out.println("Number discarded: " + totalDiscarded);
			double numCandidates = Card.NUM_CARDS - totalDiscarded - hand.size() - (hand.size() - estimator.numUnknownInHand);
			System.out.println("Number of candidates: " + numCandidates);
			double singleCardProb = (double) estimator.numUnknownInHand / numCandidates;
			for (int i = 0; i < estimator.numUnknownInHand; i++) 
				uniformProb *= singleCardProb;

			System.out.println(">>>> est. " + estProb + " unif. " + uniformProb + " ratio " + (estProb / uniformProb));
			ratios.add((estProb / uniformProb));
			System.out.println(ratios);
			double sum = 0;
			for (double ratio : ratios)
				sum += ratio;
			System.out.println("Average ratio: " + sum / ratios.size());
		}
	}
	
}

