package ginrummy;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.io.IOException;


/**
 * A class for modeling a game of Gin Rummy
 *
 * @author Todd W. Neller
 * @version 1.0

Copyright (C) 2020 Todd Neller

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Information about the GNU General Public License is available online at:
  http://www.gnu.org/licenses/
To receive a copy of the GNU General Public License, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
02111-1307, USA.

 */
public class GinRummyGame {

	/**
	 * Random number generator
	 */
	private static final Random RANDOM = new Random();

	/**
	 * Hand size (before and after turn). After draw and before discard there is one extra card.
	 */
	private static final int HAND_SIZE = 10;

	/**
	 * Whether or not to print information during game play
	 */
	private static boolean playVerbose = false;

	/**
	 * Two Gin Rummy players numbered according to their array index.
	 */
	private GinRummyPlayer[] players;

	/**
	 * Set whether or not there is to be printed output during gameplay.
	 * @param playVerbose whether or not there is to be printed output during gameplay
	 */
	public static void setPlayVerbose(boolean playVerbose) {
		GinRummyGame.playVerbose = playVerbose;
	}

	/**
	 * Create a GinRummyGame with two given players
	 * @param player0 Player 0
	 * @param player1 Player 1
	 */
	public GinRummyGame(GinRummyPlayer player0, GinRummyPlayer player1) {
		players = new GinRummyPlayer[] {player0, player1};
	}

	/**
	 * Play a game of Gin Rummy and return the winning player number 0 or 1.
	 * @return the winning player number 0 or 1
	 */
	@SuppressWarnings("unchecked")
	public int play() {
		int[] scores = new int[2];
		ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
		hands.add(new ArrayList<Card>());
		hands.add(new ArrayList<Card>());
		int startingPlayer = RANDOM.nextInt(2);

		while (scores[0] < GinRummyUtil.GOAL_SCORE && scores[1] < GinRummyUtil.GOAL_SCORE) { // while game not over
			int currentPlayer = startingPlayer;
			int opponent = (currentPlayer == 0) ? 1 : 0;

			// get shuffled deck and deal cards
			Stack<Card> deck = Card.getShuffle(RANDOM.nextInt());
			hands.get(0).clear();
			hands.get(1).clear();
			for (int i = 0; i < 2 * HAND_SIZE; i++)
				hands.get(i % 2).add(deck.pop());
			for (int i = 0; i < 2; i++) {
				Card[] handArr = new Card[HAND_SIZE];
				hands.get(i).toArray(handArr);
				players[i].startGame(i, startingPlayer, handArr);
				if (playVerbose)
					System.out.printf("Player %d is dealt %s.\n", i, hands.get(i));
			}
			if (playVerbose)
				System.out.printf("Player %d starts.\n", startingPlayer);
			Stack<Card> discards = new Stack<Card>();
			discards.push(deck.pop());
			if (playVerbose)
				System.out.printf("The initial face up card is %s.\n", discards.peek());
			Card firstFaceUpCard = discards.peek();
			int turnsTaken = 0;
			ArrayList<ArrayList<Card>> knockMelds = null;
			while (deck.size() > 2) { // while the deck has more than two cards remaining, play round
				// DRAW
				boolean drawFaceUp = false;
				Card faceUpCard = discards.peek();
				// offer draw face-up iff not 3rd turn with first face up card (decline automatically in that case)
				if (!(turnsTaken == 2 && faceUpCard == firstFaceUpCard)) { // both players declined and 1st player must draw face down
					drawFaceUp = players[currentPlayer].willDrawFaceUpCard(faceUpCard);
					if (playVerbose && !drawFaceUp && faceUpCard == firstFaceUpCard && turnsTaken < 2)
						System.out.printf("Player %d declines %s.\n", currentPlayer, firstFaceUpCard);
				}
				if (!(!drawFaceUp && turnsTaken < 2 && faceUpCard == firstFaceUpCard)) { // continue with turn if not initial declined option
					Card drawCard = drawFaceUp ? discards.pop() : deck.pop();
					for (int i = 0; i < 2; i++)
						players[i].reportDraw(currentPlayer, (i == currentPlayer || drawFaceUp) ? drawCard : null);
					if (playVerbose)
						System.out.printf("Player %d draws %s.\n", currentPlayer, drawCard);
					hands.get(currentPlayer).add(drawCard);

					// DISCARD
					Card discardCard = players[currentPlayer].getDiscard();
					if (!hands.get(currentPlayer).contains(discardCard) || discardCard == faceUpCard) {
						if (playVerbose)
							System.out.printf("Player %d discards %s illegally and forfeits.\n", currentPlayer, discardCard);
						return opponent;
					}
					hands.get(currentPlayer).remove(discardCard);
					for (int i = 0; i < 2; i++)
						players[i].reportDiscard(currentPlayer, discardCard);
					if (playVerbose)
						System.out.printf("Player %d discards %s.\n", currentPlayer, discardCard);
					discards.push(discardCard);
					if (playVerbose) {
						ArrayList<Card> unmeldedCards = (ArrayList<Card>) hands.get(currentPlayer).clone();
						ArrayList<ArrayList<ArrayList<Card>>> bestMelds = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
						if (bestMelds.isEmpty())
							System.out.printf("Player %d has %s with %d deadwood.\n", currentPlayer, unmeldedCards, GinRummyUtil.getDeadwoodPoints(unmeldedCards));
						else {
							ArrayList<ArrayList<Card>> melds = bestMelds.get(0);
							for (ArrayList<Card> meld : melds)
								for (Card card : meld)
									unmeldedCards.remove(card);
							melds.add(unmeldedCards);
							System.out.printf("Player %d has %s with %d deadwood.\n", currentPlayer, melds, GinRummyUtil.getDeadwoodPoints(unmeldedCards));
						}
					}

					// CHECK FOR KNOCK
					knockMelds = players[currentPlayer].getFinalMelds();
					if (knockMelds != null)
						break; // player knocked; end of round
				}

				turnsTaken++;
				currentPlayer = (currentPlayer == 0) ? 1 : 0;
				opponent = (currentPlayer == 0) ? 1 : 0;
			}

			if (knockMelds != null) { // round didn't end due to non-knocking and 2 cards remaining in draw pile
				// check legality of knocking meld
				long handBitstring = GinRummyUtil.cardsToBitstring(hands.get(currentPlayer));
				long unmelded = handBitstring;
				for (ArrayList<Card> meld : knockMelds) {
					long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
					if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
							|| (meldBitstring & unmelded) != meldBitstring) { // ... or meld not in hand
						if (playVerbose)
							System.out.printf("Player %d melds %s illegally and forfeits.\n", currentPlayer, knockMelds);
						return opponent;
					}
					unmelded &= ~meldBitstring; // remove successfully melded cards from
				}
				// compute knocking deadwood
				int knockingDeadwood = GinRummyUtil.getDeadwoodPoints(knockMelds, hands.get(currentPlayer));
				if (knockingDeadwood > GinRummyUtil.MAX_DEADWOOD) {
					if (playVerbose)
						System.out.printf("Player %d melds %s with greater than %d deadwood and forfeits.\n", currentPlayer, knockMelds, knockingDeadwood);
					return opponent;
				}

				ArrayList<ArrayList<Card>> meldsCopy = new ArrayList<ArrayList<Card>>();
				for (ArrayList<Card> meld : knockMelds)
					meldsCopy.add((ArrayList<Card>) meld.clone());
				for (int i = 0; i < 2; i++)
					players[i].reportFinalMelds(currentPlayer, meldsCopy);
				if (playVerbose)
					if (knockingDeadwood > 0)
						System.out.printf("Player %d melds %s with %d deadwood from %s.\n", currentPlayer, knockMelds, knockingDeadwood, GinRummyUtil.bitstringToCards(unmelded));
					else
						System.out.printf("Player %d goes gin with melds %s.\n", currentPlayer, knockMelds);

				// get opponent meld
				ArrayList<ArrayList<Card>> opponentMelds = players[opponent].getFinalMelds();
				meldsCopy = new ArrayList<ArrayList<Card>>();
				for (ArrayList<Card> meld : opponentMelds)
					meldsCopy.add((ArrayList<Card>) meld.clone());
				for (int i = 0; i < 2; i++)
					players[i].reportFinalMelds(opponent, meldsCopy);

				// check legality of opponent meld
				long opponentHandBitstring = GinRummyUtil.cardsToBitstring(hands.get(opponent));
				long opponentUnmelded = opponentHandBitstring;
				for (ArrayList<Card> meld : opponentMelds) {
					long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
					if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
							|| (meldBitstring & opponentUnmelded) != meldBitstring) { // ... or meld not in hand
						if (playVerbose)
							System.out.printf("Player %d melds %s illegally and forfeits.\n", opponent, opponentMelds);
						return currentPlayer;
					}
					opponentUnmelded &= ~meldBitstring; // remove successfully melded cards from
				}
				if (playVerbose)
					System.out.printf("Player %d melds %s.\n", opponent, opponentMelds);

				// lay off on knocking meld (if not gin)
				ArrayList<Card> unmeldedCards = GinRummyUtil.bitstringToCards(opponentUnmelded);
				if (knockingDeadwood > 0) { // knocking player didn't go gin
					boolean cardWasLaidOff;
					do { // attempt to lay each card off
						cardWasLaidOff = false;
						Card layOffCard = null;
						ArrayList<Card> layOffMeld = null;
						for (Card card : unmeldedCards) {
							for (ArrayList<Card> meld : knockMelds) {
								ArrayList<Card> newMeld = (ArrayList<Card>) meld.clone();
								newMeld.add(card);
								long newMeldBitstring = GinRummyUtil.cardsToBitstring(newMeld);
								if (GinRummyUtil.getAllMeldBitstrings().contains(newMeldBitstring)) {
									layOffCard = card;
									layOffMeld = meld;
									break;
								}
							}
							if (layOffCard != null) {
								if (playVerbose)
									System.out.printf("Player %d lays off %s on %s.\n", opponent, layOffCard, layOffMeld);
								for (int i = 0; i < 2; i++)
									players[i].reportLayoff(opponent, layOffCard, (ArrayList<Card>) layOffMeld.clone());
								unmeldedCards.remove(layOffCard);
								layOffMeld.add(layOffCard);
								cardWasLaidOff = true;
								break;
							}

						}
					} while (cardWasLaidOff);
				}
				int opponentDeadwood = 0;
				for (Card card : unmeldedCards)
					opponentDeadwood += GinRummyUtil.getDeadwoodPoints(card);
				if (playVerbose)
					System.out.printf("Player %d has %d deadwood with %s\n", opponent, opponentDeadwood, unmeldedCards);

				// compare deadwood and compute new scores
				if (knockingDeadwood == 0) { // gin round win
					scores[currentPlayer] += GinRummyUtil.GIN_BONUS + opponentDeadwood;
					if (playVerbose)
						System.out.printf("Player %d scores the gin bonus of %d plus opponent deadwood %d for %d total points.\n", currentPlayer, GinRummyUtil.GIN_BONUS, opponentDeadwood, GinRummyUtil.GIN_BONUS + opponentDeadwood);
				}
				else if (knockingDeadwood < opponentDeadwood) { // non-gin round win
					scores[currentPlayer] += opponentDeadwood - knockingDeadwood;
					if (playVerbose)
						System.out.printf("Player %d scores the deadwood difference of %d.\n", currentPlayer, opponentDeadwood - knockingDeadwood);
				}
				else { // undercut win for opponent
					scores[opponent] += GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood;
					if (playVerbose)
						System.out.printf("Player %d undercuts and scores the undercut bonus of %d plus deadwood difference of %d for %d total points.\n", opponent, GinRummyUtil.UNDERCUT_BONUS, knockingDeadwood - opponentDeadwood, GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood);
				}
				startingPlayer = (startingPlayer == 0) ? 1 : 0; // starting player alternates
			}
			else { // If the round ends due to a two card draw pile with no knocking, the round is cancelled.
				if (playVerbose)
					System.out.println("The draw pile was reduced to two cards without knocking, so the hand is cancelled.");
			}

			// report final hands
			for (int i = 0; i < 2; i++)
				for (int j = 0; j < 2; j++)
					players[i].reportFinalHand(j, (ArrayList<Card>) hands.get(j).clone());

			// score reporting
			if (playVerbose)
				System.out.printf("Player\tScore\n0\t%d\n1\t%d\n", scores[0], scores[1]);
			for (int i = 0; i < 2; i++)
				players[i].reportScores(scores.clone());
		}
		if (playVerbose)
			System.out.printf("Player %s wins.\n", scores[0] > scores[1] ? 0 : 1);
		return scores[0] >= GinRummyUtil.GOAL_SCORE ? 0 : 1;
	}
	
	//-----------------
	public static ArrayList<Double> adjustCoeff0 (ArrayList<Double> coeff0, ArrayList<Double> coeff1)
	{
		ArrayList<Double> newCoeff0 = new ArrayList<Double>();
		
		for (int i = 0; i < coeff0.size(); i ++)
		{
			newCoeff0.add( coeff0.get(i) + 0.05 * (coeff1.get(i) - coeff0.get(i)) );
		}
		
		return newCoeff0;
	}
	//----------------
	
	//Parallel Game
	public static int parallelGame(int parallelID) throws IOException{
		// Single verbose demonstration game
		GinRummyGame game_demo = new GinRummyGame(new SGP_NN_OHE_server(), new SimpleGinRummyPlayer());
		game_demo.setPlayVerbose(true);
		game_demo.play();
		
		// Multiple non-verbose games 
		GinRummyGame game = new GinRummyGame(new SGP_NN_OHE_server(), new SimpleGinRummyPlayer());
		game.setPlayVerbose(false);
		int numGames = 100;
		int numP1Wins = 0;
		long startMs = System.currentTimeMillis();
		for (int i = 0; i < numGames; i++) numP1Wins += game.play();
		long totalMs = System.currentTimeMillis() - startMs;
		System.out.printf("%d games played in %d ms.\n", numGames, totalMs);
		System.out.printf("Games Won: P0:%d, P1:%d.\n", numGames - numP1Wins, numP1Wins);
		return numGames - numP1Wins;
	}

	/**
	 * Test and demonstrate the use of the GinRummyGame class.
	 * @param args (unused)
	 * 
	 * DualInception(), SGP_NN_OHE_server(), SGP_LM5_Knock_Q(), 
	 */
	public static void main(String[] args) {
		// Single verbose demonstration game
		setPlayVerbose(true);
		new GinRummyGame(new SGP_LM5_Knock_Q(), new SimpleGinRummyPlayer()).play();
		// Multiple non-verbose games 
		setPlayVerbose(false);
		int numGames = 100;
		int numP1Wins = 0;
		
		GinRummyGame game = new GinRummyGame(new SGP_LM5_Knock_Q(), new SimpleGinRummyPlayer());
		long startMs = System.currentTimeMillis();
		for (int i = 0; i < numGames; i++) {
			numP1Wins += game.play();
		}
		long totalMs = System.currentTimeMillis() - startMs;
		System.out.printf("%d games played in %d ms.\n", numGames, totalMs);
		System.out.printf("Games Won: P0:%d, P1:%d.\n", numGames - numP1Wins, numP1Wins);
	}	
	
// SGP_LM5_Knock_EVO()
//	/**
//	 * Test and demonstrate the use of the GinRummyGame class.
//	 * @param args (unused)
//	 */
//	public static void main(String[] args) {		
//		// Single verbose demonstration game
//		setPlayVerbose(true);
//		new GinRummyGame(new SGP_LM5_Knock_EVO(), new SGP_LM5_Knock_EVO()).play();
//
//		// Multiple non-verbose games 
//		setPlayVerbose(false);
//		
////-----------------------------------------------------------------------------------------------------------
//		
////		ArrayList<Long> avgScore = new ArrayList<Long>();			
//		long pts = 0;
////		SGP_LM_DrawKnock.increaseCoeff();
////		System.out.println("Hitscore coeff before: " + SGP_LM_DrawKnock.getCoeff());
//		
//		ArrayList<ArrayList<Double>> CoefficientsSet_0 = new ArrayList<ArrayList<Double>>();
//		ArrayList<ArrayList<Double>> CoefficientsSet_1 = new ArrayList<ArrayList<Double>>();		
//		
//		Random rand = new Random();
//		
//		double STD = 0.3;
//		double threshold = 0.66;
//		
//		CoefficientsSet_0.add(new ArrayList<Double>(Arrays.asList(7.10376382,
//																 -0.9631598,
//																 13.5210358,
//																 39.9707105,
//																  0.11716917,
//																  0.2
//																  )));
//		
//		CoefficientsSet_1.add(new ArrayList<Double>(Arrays.asList(7.10376382 + rand.nextGaussian()*STD,
//																 -0.9631598  + rand.nextGaussian()*STD,
//																 13.5210358  + rand.nextGaussian()*STD,
//																 39.9707105  + rand.nextGaussian()*STD,
//																  0.11716917 + rand.nextGaussian()*STD,
//																  0.2 		 + rand.nextGaussian()*STD
//																  )));
//		
//		int nGames = 100;
//		for(int j = 0; j < nGames; j++)
//		{
//			
//			SGP_LM5_Knock_EVO player0 = new SGP_LM5_Knock_EVO();
//			player0.setCoeff(CoefficientsSet_0.get(j));
//			
//			SGP_LM5_Knock_EVO player1 = new SGP_LM5_Knock_EVO();
//			ArrayList<Double> Coeff1 = CoefficientsSet_1.get(j);
//			ArrayList<Double> mutatedCoeff1 = new ArrayList<Double>();
//			for (int i = 0; i < Coeff1.size(); i++)
//				mutatedCoeff1.add(Coeff1.get(i) + rand.nextGaussian()*STD);
//			player1.setCoeff(mutatedCoeff1);
//			
//			GinRummyGame game = new GinRummyGame(player0, player1);
//			// long startMs = System.currentTimeMillis();
//			
//			int numGames = 100;
//			int numP1Wins = 0;
//			for (int i = 0; i < numGames; i++) {
//				numP1Wins += game.play();
//			}
//			pts += numGames - numP1Wins;
//			
//			System.out.println("numP1Wins: " + numP1Wins);
//			System.out.println("CoefficientsSet_0 before: " + CoefficientsSet_0.get(j));
//			System.out.println("CoefficientsSet_1 before: " + CoefficientsSet_1.get(j));
//			
//			if (numP1Wins >= threshold * (double) numGames)
//			{
//				ArrayList<Double> newCoeff0 = adjustCoeff0(CoefficientsSet_0.get(j), CoefficientsSet_1.get(j));
//				CoefficientsSet_0.add(newCoeff0);
//				CoefficientsSet_1.add(mutatedCoeff1);
//			}
//			else
//			{
//				CoefficientsSet_0.add(CoefficientsSet_0.get(j));
//				CoefficientsSet_1.add(mutatedCoeff1);
//			}
//			System.out.println("CoefficientsSet_0 after: " + CoefficientsSet_0.get(j+1));
//			System.out.println("CoefficientsSet_1 after: " + CoefficientsSet_1.get(j+1));
//			
//			// long totalMs = System.currentTimeMillis() - startMs;
//			// System.out.printf("%d games played in %d ms.\n", numGames, totalMs);
//			System.out.printf("Games Won: P0:%d, P1:%d.\n", numGames - numP1Wins, numP1Wins);
//			
//			System.out.println();
//		}
//		
//		System.out.printf("Average point: %.4f", (double) (pts / nGames));
//		
////		avgScore.add(pts / nGames);
//		
////		if (avgScore.get(avgScore.size()-1) - avgScore.get(avgScore.size()-2) > 10)
////			SGP_LM_DrawKnock.increaseCoeff();
////		else 
////			SGP_LM_DrawKnock.decreaseCoeff();
////		System.out.println("Hitscore coeff after: " + SGP_LM_DrawKnock.getCoeff());
//	}	


// 	parallelGame()
//	public static void main(String[] args) throws IOException {
//		int numStream = 1;
//		// Parallel game
//		IntStream.range(0, numStream).parallel().forEach(i -> {
//			try  { parallelGame(i); } 
//			catch (IOException e) {e.printStackTrace(); }
//			}
//				);
//		}
}
