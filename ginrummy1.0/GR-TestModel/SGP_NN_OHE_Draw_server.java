import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.net.Socket;
import java.net.UnknownHostException;

public class SGP_NN_OHE_Draw_server implements GinRummyPlayer {
	private int playerNum;
	@SuppressWarnings("unused")
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Random random = new Random();
	private boolean opponentKnocked = false;
	Card faceUpCard, drawnCard; 
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
	private int gamestateIndex = 1;
	int turnCount = 0;
	HandEstimator estimator = new HandEstimator();
	private int totalDiscarded = 0;
	ArrayList<Double> ratios = new ArrayList<Double>();
    
	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards) this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
		estimator.init();
		ArrayList<Card> hand = new ArrayList<Card>();
		for (Card c : cards) hand.add(c);
		estimator.setKnown(hand, false);
//		estimator.print();
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
			if (meld.contains(card)) return true;
		
// 		if (card.getRank() < 3) return true; 
//		Card discard = getDiscard(newCards);
//		
//		Random r = new Random();		
//		if (discard == card) return false;
//		else {
//			if (r.nextDouble() < 0.5) return true;
//			else return false;
//		}
		
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
		// Disallow repeat of draw and discard.
		ArrayList<Card> eligibleDiscardCards_1 = new ArrayList<Card>();
		for (Card card : cards) {
			if (card == drawnCard && drawnCard == faceUpCard) continue;
			ArrayList<Card> drawDiscard = new ArrayList<Card>();
			drawDiscard.add(drawnCard);
			drawDiscard.add(card);
			if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard))) continue;
			eligibleDiscardCards_1.add(card);
		}
		
		// Prevent breaking melds
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		
		ArrayList<Card> unmeldedCards = new ArrayList<Card>(); // This is the set of card being consider discard
		if (bestMeldSets.isEmpty()) unmeldedCards = (ArrayList<Card>) cards.clone();
		else {
			for (Card card: eligibleDiscardCards_1) {
				ArrayList<ArrayList<Card>> bestMelds = bestMeldSets.get(0); 
				boolean check = false;
				for (ArrayList<Card> meld: bestMelds)
					if(meld.contains(card)) {
						check = true;
						break;
					}
				if (!check) unmeldedCards.add(card);
			}
		}
		
		// If all 11 cards are in melds, they are all in unmelded cards
		if (unmeldedCards.size() == 0) unmeldedCards = (ArrayList<Card>) cards.clone();
			
		// Discard based on model
		ArrayList<ArrayList<Integer>> features = new ArrayList<ArrayList<Integer>>();
		ArrayList<Card> features_cards = new ArrayList<Card>();
		ArrayList<Integer> deadwoods = new ArrayList<Integer>();
		ArrayList<int[]> dummyHands0 = new ArrayList<int[]>();		
		
		ArrayList<ArrayList<Float>> xfo_list = new ArrayList<ArrayList<Float>>();
		
		// OHE
        double[] ISRUprob = ISRU(estimator.prob);

		for (Card card: unmeldedCards){	
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
			xfo_list.add(xfo);
		}
		
		double[] q_values = new double[features.size()];
		try { q_values = NeuralNet(xfo_list); } 
		catch (IOException e) { e.printStackTrace(); }
	
//		estimator.print();
//		System.out.println(estimator.getEstimatedHand());
//		for (int i = 0; i < q_values.length; i++)
//			System.out.println(q_values[i] 
//								+ " : "
//								+ features_cards.get(i) 
//								+ " : "
//								+ joint_likelihood(estimator.prob, features_cards.get(i).getId() ));
		
		double maxPredScore = -Double.MAX_VALUE;
		ArrayList<Card> unmeldedCards_maxPredScore = new ArrayList<Card>();
		for (int i = 0; i < features.size(); i++) {
			if (q_values[i] >= maxPredScore) {
				if (q_values[i] > maxPredScore) {
					maxPredScore = q_values[i];
					unmeldedCards_maxPredScore.clear();
				}
				unmeldedCards_maxPredScore.add(features_cards.get(i));
			}
		}
		
		Card discard;
		if (maxPredScore < -10)
			discard = unmeldedCards_maxPredScore.get(random.nextInt(unmeldedCards_maxPredScore.size()));
		else {
			ArrayList<Card> positiveHands = new ArrayList<Card>();
			for (int i = 0; i < features.size(); i++) {
				if (q_values[i] > -10) {
					positiveHands.add(features_cards.get(i));
				}
			}
			
			double jointLikelihood[] = new double[positiveHands.size()];
			for (int i = 0; i < positiveHands.size(); i++) {
				jointLikelihood[i] = joint_likelihood(estimator.prob, features_cards.get(i).getId());
			}			
			
			double minJointLikelihood = Double.MAX_VALUE;
			ArrayList<Card> minJointLikelihoodCards = new ArrayList<Card>();
			for (int i = 0; i < positiveHands.size(); i++) {
				if (jointLikelihood[i] <= minJointLikelihood) {
					if (jointLikelihood[i] < minJointLikelihood) {
						minJointLikelihood = jointLikelihood[i];
						minJointLikelihoodCards.clear();
					}
					minJointLikelihoodCards.add(positiveHands.get(i));
				}
			}
			
			discard = minJointLikelihoodCards.get(random.nextInt(minJointLikelihoodCards.size()));
		}
			
		
		// Prevent future repeat of draw, discard pair
		ArrayList<Card> drawDiscard = new ArrayList<Card>();
		drawDiscard.add(drawnCard);
		drawDiscard.add(discard);
		drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
		gamestateIndex++;
		return discard;
	}
	
	public static double joint_likelihood(double[] e, int n) {
		double s = 0;
	    //Horizontal calculate
	    //Rank A column
	    if (n == 0 || n == 13 || n == 26 || n == 39) {
	      s += e[n + 1] * e[n + 2];
	    }

	    //Rank K column
	    else if (n == 12 || n == 25 || n == 38 || n == 51) {
	      s += e[n - 1] * e[n - 2];
	    }

	    //Rank 2 column
	    else if (n == 1 || n == 14 || n == 27 || n == 40) {
	      s += e[n-1]*e[n+1];
	      s += e[n+1]*e[n+2];
	    }

	    //Rank Q column
	    else if (n == 11 || n == 24 || n == 37 || n == 50) {
	      s += e[n-1]*e[n+1];
	      s += e[n-1]*e[n-2];
	    }

	    //Other Rank column
	    else {
	      s += e[n-1]*e[n-2];
	      s += e[n-1]*e[n+1];
	      s += e[n+1]*e[n+2];
	    }

	    //Vertical calculate
	    double[] verti = new double[3];
	    int prev = n - 13;
	    int next = n + 13;
	    int t = 0;
	    while (prev > 0 && t < verti.length) {
	      verti[t] = e[prev];
	      prev -= 13;
	      t += 1;
	    }
	    while (next < 52 && t < verti.length) {
	      verti[t] = e[next];
	      next += 13;
	      t += 1;
	    }
	    for(int i = 0; i < verti.length; i++) {
	      for(int j = i+1; j < verti.length; j++) {
	        s += verti[i]*verti[j];
	      }
	    }
	    return s;
	}
	
	public double[] ISRU(double[] m) {
		double[] n = new double[m.length];
		int z = 1;
		for(int i = 0; i < m.length; i++) {
			if((m[i] != 0.0) && (m[i] != 1.0)) n[i] = m[i]/Math.sqrt(z+m[i]*m[i]);
			else n[i] = m[i];
		}
		return n;
	}
	
	private double[] NeuralNet(ArrayList<ArrayList<Float>> xfo_list) throws IOException {
		double[] q_values = new double[xfo_list.size()];

		for (int i = 0; i < xfo_list.size(); i++) {
		    try {
				Socket s = new Socket("127.0.0.1", 8888);
	            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				
	            // Send a number to python
	            out.println(xfo_list.get(i));		
				
		    	// Receive a number from python
		    	String message;
	            while ((message = in.readLine()) != null) {
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
		return q_values;
	}		
	
	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		totalDiscarded++;
		if (playerNum == this.playerNum) cards.remove(discardedCard);
		else estimator.reportDrawDiscard(faceUpCard, faceUpCard == drawnCard, discardedCard);
		faceUpCard = discardedCard;
//		estimator.print();
	}
	
	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out. 
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD) )
			return null;
		gamestateIndex = 1;
		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
	}

//    @SuppressWarnings("unchecked")
//	@Override
//	public ArrayList<ArrayList<Card>> getFinalMelds() {
//
//		// Check if deadwood of maximal meld is low enough to go out. 
//		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
//		
//		ArrayList<ArrayList<Card>> bestMelds = (bestMeldSets.isEmpty())?
//				new ArrayList<ArrayList<Card>>() : bestMeldSets.get(0);
//		
//        ArrayList<Card> unmeldedCards = (ArrayList<Card>) this.cards.clone();
//        if (bestMeldSets.size() != 0)
//        {
//            ArrayList<ArrayList<Card>> melds = bestMeldSets.get(0);
//            for (ArrayList<Card> meld: melds)
//                for (Card card: meld)
//                    unmeldedCards.remove(card);
//        }
//        if (opponentKnocked)
//        {
//            if (bestMeldSets.size() == 0) return new ArrayList<ArrayList<Card>>();
//            else return bestMeldSets.get(random.nextInt(bestMeldSets.size()));
//        }
//        else
//        {
//        	int deadwoodPoints = GinRummyUtil.getDeadwoodPoints(bestMelds, cards);
//        	if (bestMeldSets.isEmpty() || deadwoodPoints  > GinRummyUtil.MAX_DEADWOOD) return null; // Unable to knock
//        	else if (deadwoodPoints >= 1)
//        	{
//        		if (unmeldedCards.size() <= 2 && turnCount > 22)
//        			return bestMeldSets.get(random.nextInt(bestMeldSets.size())); //Dangerous zone
//        		else return null; //Choose not to knock
//        	}
//        	else return bestMeldSets.get(random.nextInt(bestMeldSets.size()));
//        }
//	}	
	
	
	

	@Override
	public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
		if (playerNum != this.playerNum) opponentKnocked = true;
	}

	@Override
	public void reportScores(int[] scores) {}

	@Override
	public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {}

	@Override
	public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
		if (playerNum != this.playerNum) { // opponent hand
			// Record est. likelihood of actual opponent hand
			int numCards = 0;
			double estProb = 1;
			for (Card card : hand) {
				numCards++;
				if (!estimator.known[card.getId()]) estProb *= estimator.prob[card.getId()];
			}
			// Record uniform likelihood of actual opponent hand
			double uniformProb = 1;
			// Compute the number of possible cards that may be those unknown in the opponent's hand
			System.out.println("Number of opponent cards known: " + (hand.size() - estimator.numUnknownInHand));
			System.out.println("Number discarded: " + totalDiscarded);
			double numCandidates = Card.NUM_CARDS - totalDiscarded - hand.size() - (hand.size() - estimator.numUnknownInHand);
			System.out.println("Number of candidates: " + numCandidates);
			double singleCardProb = (double) estimator.numUnknownInHand / numCandidates;
			for (int i = 0; i < estimator.numUnknownInHand; i++)  uniformProb *= singleCardProb;

			System.out.println(">>>> est. " + estProb + " unif. " + uniformProb + " ratio " + (estProb / uniformProb));
			ratios.add((estProb / uniformProb));
			System.out.println(ratios);
			double sum = 0;
			for (double ratio : ratios) sum += ratio;
			System.out.println("Average ratio: " + sum / ratios.size());
		}
	}	
}