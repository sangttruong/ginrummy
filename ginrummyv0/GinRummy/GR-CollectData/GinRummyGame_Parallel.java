import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.IntStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class for modeling a game of Gin Rummy.
 * There are 3 things to modify for a new games: 
 * - Player (4 places)
 * - Number of game each stream
 * - Number of stream
 * 
 * @author Sang T. Truong and Hoang H. Pham
 * @version 2.0
 */

public class GinRummyGame_Parallel {
	private DataCollector_Parallel dataCollector = new DataCollector_Parallel();
	private final Random RANDOM = new Random();
	private final int HAND_SIZE = 10;
	private boolean playVerbose = false;
	private GinRummyPlayer[] players;
	
	public GinRummyGame_Parallel(GinRummyPlayer player0, GinRummyPlayer player1)
	{
		players = new GinRummyPlayer[] {player0, player1};
	}
	
	public void setPlayVerbose(boolean playVerbose) {
		this.playVerbose = playVerbose;
	}
	
	@SuppressWarnings("unchecked")
	public int play() {
		int[] scores = new int[2];
		ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
		hands.add(new ArrayList<Card>());
		hands.add(new ArrayList<Card>());
		int startingPlayer = RANDOM.nextInt(2);
		
		while (scores[0] < GinRummyUtil.GOAL_SCORE && scores[1] < GinRummyUtil.GOAL_SCORE) { // while game not over
			int currentPlayer = startingPlayer;
			
			// For data collector--------------------------------------------------------------------------
			dataCollector.addStarter(startingPlayer);
			//---------------------------------------------------------------------------------------------
			
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
			
			// For data collector--------------------------------------------------------------------------
			dataCollector.nextGamePlay();
			if (dataCollector.PLAYER == currentPlayer)
				dataCollector.addGameState();
			//---------------------------------------------------------------------------------------------
			
			while (deck.size() > 2) { // while the deck has more than two cards remaining, play round

				// For data collector--------------------------------------------------------------------------
				if (dataCollector.PLAYER != currentPlayer) // Player 1
				{
					dataCollector.addGameState();
					dataCollector.addHand0(hands.get(dataCollector.PLAYER));
					dataCollector.addHand1(hands.get(currentPlayer));
					dataCollector.addFaceUp1(discards.peek());
				}
				else
				{
					dataCollector.addHand0(hands.get(dataCollector.PLAYER));
					dataCollector.addFaceUp0(discards.peek());
//					dataCollector.setKnown();
//					dataCollector.addEstimatedHand1();
//					dataCollector.addProb0();
				}
				//---------------------------------------------------------------------------------------------
				
				// DRAW
				boolean drawFaceUp = false;
				Card faceUpCard = discards.peek();
				// offer draw face-up iff not 3rd turn with first face up card (decline automatically in that case) 
				if (!(turnsTaken == 3 && faceUpCard == firstFaceUpCard)) { // both players declined and 1st player must draw face down
					drawFaceUp = players[currentPlayer].willDrawFaceUpCard(faceUpCard);
					if (playVerbose && !drawFaceUp && faceUpCard == firstFaceUpCard && turnsTaken < 2)
						System.out.printf("Player %d declines %s.\n", currentPlayer, firstFaceUpCard);
				}
				if (!(!drawFaceUp && turnsTaken < 2 && faceUpCard == firstFaceUpCard)) { // continue with turn if not initial declined option
					Card drawCard = drawFaceUp ? discards.pop() : deck.pop();
					
					// For data collector--------------------------------------------------------------------------
					if (dataCollector.PLAYER != currentPlayer)
						dataCollector.addDrawnFaceUp1(drawFaceUp);
					if (dataCollector.PLAYER == currentPlayer)
						dataCollector.addWillDraw0(drawCard);
					else
					{
						if (drawFaceUp)
							dataCollector.addDrawn1(drawCard);
					}
					//---------------------------------------------------------------------------------------------
					
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
						dataCollector.popGamePlay();
						return opponent;
					}
					hands.get(currentPlayer).remove(discardCard);
					
					// For data collector--------------------------------------------------------------------------
					if (dataCollector.PLAYER == currentPlayer)
						dataCollector.addWillDiscard0(discardCard);
					else
						dataCollector.addDiscarded1(discardCard);
					//---------------------------------------------------------------------------------------------
					
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
					// For data collector--------------------------------------------------------------------------
					if (dataCollector.PLAYER != currentPlayer)
						dataCollector.addHand1(hands.get(currentPlayer));
					//---------------------------------------------------------------------------------------------
						
					// CHECK FOR KNOCK 
					knockMelds = players[currentPlayer].getFinalMelds();
					if (knockMelds != null)
					{
						// For data collector--------------------------------------------------------------------------
						dataCollector.addKnocker(currentPlayer);						
						//---------------------------------------------------------------------------------------------
						
						break; // player knocked; end of round
					}
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
						dataCollector.popGamePlay();
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
				
				// For data collector -----------------------------------------------------------------------------------
				dataCollector.addKnockerHand(hands.get(currentPlayer));
				dataCollector.addNonknockerHand(hands.get(opponent));
				
				dataCollector.addKnockerHitscore(FeatureEngineer0.hitScore(hands.get(currentPlayer)));
				dataCollector.addNonknockerHitscore(FeatureEngineer0.hitScore(hands.get(opponent)));
				//-------------------------------------------------------------------------------------------------------
				
				
				ArrayList<ArrayList<Card>> meldsCopy = new ArrayList<ArrayList<Card>>();
				for (ArrayList<Card> meld : knockMelds)
					meldsCopy.add((ArrayList<Card>) meld.clone());
				for (int i = 0; i < 2; i++) 
					players[i].reportFinalMelds(currentPlayer, meldsCopy);
				
				// For data collector -----------------------------------------------------------------------------------				
				if (knockingDeadwood > 0)
				{
					//dataCollector.addKnockerMelds(knockMelds);
					dataCollector.addKnockerDeadwood(knockingDeadwood);
					dataCollector.addKnockerMeldsCount(knockMelds.size());
					//dataCollector.addKnockerDeadwoodCards(GinRummyUtil.bitstringToCards(unmelded));
					
					
				}
				else
				{
					dataCollector.addKnockerDeadwood(0);
					dataCollector.addKnockerMeldsCount(knockMelds.size());
				}
				//-------------------------------------------------------------------------------------------------------
				
				if (playVerbose)
					if (knockingDeadwood > 0)
					{
						System.out.printf("Player %d melds %s with %d deadwood from %s.\n", currentPlayer, knockMelds, knockingDeadwood, GinRummyUtil.bitstringToCards(unmelded));
					}
					else
					{	
						System.out.printf("Player %d goes gin with melds %s.\n", currentPlayer, knockMelds);
					}
				
				// get opponent meld
				ArrayList<ArrayList<Card>> opponentMelds = players[opponent].getFinalMelds();
				for (ArrayList<Card> meld : opponentMelds)
					meldsCopy.add((ArrayList<Card>) meld.clone());
				meldsCopy = new ArrayList<ArrayList<Card>>();
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
						dataCollector.popGamePlay();
						return currentPlayer;
					}
					opponentUnmelded &= ~meldBitstring; // remove successfully melded cards from 
				}
				
				// For data collector---------------------------------------------------------------------------------
				dataCollector.addNonknockerMeldsCount(opponentMelds.size());
				//----------------------------------------------------------------------------------------------------
				
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
				
				// For data collector -------------------------------------------------------------------------
				dataCollector.addNonknockerDeadwood(opponentDeadwood);
				//---------------------------------------------------------------------------------------------
				
				if (playVerbose) System.out.printf("Player %d has %d deadwood with %s\n", opponent, opponentDeadwood, unmeldedCards);
				
				// compare deadwood and compute new scores
				if (knockingDeadwood == 0) { // gin round win

					// For data collector--------------------------------------------------------------------------
					dataCollector.addWinner(currentPlayer);					
					//---------------------------------------------------------------------------------------------
					
					scores[currentPlayer] += GinRummyUtil.GIN_BONUS + opponentDeadwood;
					
					// For data collector--------------------------------------------------------------------------
					dataCollector.addScore(GinRummyUtil.GIN_BONUS + opponentDeadwood);
					//---------------------------------------------------------------------------------------------

					if (playVerbose)
						System.out.printf("Player %d scores the gin bonus of %d plus opponent deadwood %d for %d total points.\n", currentPlayer, GinRummyUtil.GIN_BONUS, opponentDeadwood, GinRummyUtil.GIN_BONUS + opponentDeadwood); 
				}
				else if (knockingDeadwood < opponentDeadwood) { // non-gin round win

					// For data collector--------------------------------------------------------------------------
					dataCollector.addWinner(currentPlayer);
					//---------------------------------------------------------------------------------------------
					
					scores[currentPlayer] += opponentDeadwood - knockingDeadwood;
					
					// For data collector--------------------------------------------------------------------------
					dataCollector.addScore(opponentDeadwood - knockingDeadwood);
					//---------------------------------------------------------------------------------------------
					
					if (playVerbose)
						System.out.printf("Player %d scores the deadwood difference of %d.\n", currentPlayer, opponentDeadwood - knockingDeadwood); 
				}
				else { // undercut win for opponent
					
					// For data collector--------------------------------------------------------------------------
					dataCollector.addWinner(opponent);
					//---------------------------------------------------------------------------------------------
					
					scores[opponent] += GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood;
					
					// For data collector--------------------------------------------------------------------------
					dataCollector.addScore(GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood);
					//---------------------------------------------------------------------------------------------

					if (playVerbose)
						System.out.printf("Player %d undercuts and scores the undercut bonus of %d plus deadwood difference of %d for %d total points.\n", opponent, GinRummyUtil.UNDERCUT_BONUS, knockingDeadwood - opponentDeadwood, GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood); 
				}
				
				startingPlayer = (startingPlayer == 0) ? 1 : 0; // starting player alternates
			}
			else { // If the round ends due to a two card draw pile with no knocking, the round is cancelled.
				if (playVerbose) System.out.println("The draw pile was reduced to two cards without knocking, so the hand is cancelled.");
				
				// For data collector--------------------------------------------------------------------------
				// If there isn't a winner, pop the game-play
				dataCollector.popGamePlay();
				// dataCollector.addScore(-1);
				// dataCollector.addKnocker(-1);
				// dataCollector.addWinner(-1);
				// dataCollector.addKnockerDeadwood(-1);
				// dataCollector.addNonknockerDeadwood(-1);
				//---------------------------------------------------------------------------------------------
			}

			// score reporting
			if (playVerbose) 
				System.out.printf("Player\tScore\n0\t%d\n1\t%d\n", scores[0], scores[1]);
			for (int i = 0; i < 2; i++) 
				players[i].reportScores(scores.clone());
		}
		if (playVerbose)
			System.out.printf("Player %s wins.\n", scores[0] > scores[1] ? 0 : 1);

		// For data collector--------------------------------------------------------------------------
		XGBPlayer.addHistoryScore(scores[0], scores[1], scores[1] >= GinRummyUtil.GOAL_SCORE);
		
		//---------------------------------------------------------------------------------------------
		
		return scores[0] >= GinRummyUtil.GOAL_SCORE ? 0 : 1;
	}

	public DataCollector_Parallel getDataCollector()
	{
		return dataCollector;
	}
	
	public static void parallelDataCollection(int parallelID) throws IOException{
		// Single verbose demonstration game
		GinRummyGame_Parallel game_demo = new GinRummyGame_Parallel(new SGP_LM_Knock(), new SimpleGinRummyPlayer());
		game_demo.setPlayVerbose(true);
		game_demo.play();
		
		// Multiple non-verbose games 
		GinRummyGame_Parallel game = new GinRummyGame_Parallel(new SGP_LM_Knock(), new SimpleGinRummyPlayer());
		game.setPlayVerbose(false);
		int numGames = 1000;
		int numP1Wins = 0;
		long startMs = System.currentTimeMillis();
		for (int i = 0; i < numGames; i++) numP1Wins += game.play();
		long totalMs = System.currentTimeMillis() - startMs;
		System.out.printf("%d games played in %d ms.\n", numGames, totalMs);
		System.out.printf("Games Won: P0:%d, P1:%d.\n", numGames - numP1Wins, numP1Wins);

		// Print data to file
		FileWriter gamestates = new FileWriter ("/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-Data/Test_gamestates_" + parallelID + ".txt");
		// FileWriter features = new FileWriter("features_" + parallelID + ".txt");
		game.getDataCollector().print(gamestates);
		// game.getDataCollector().save("grantData_" + parallelID + ".dat");
		gamestates.close();
		// features.close();
	}
	
	public static void main(String[] args) throws IOException {
		
		int numStream = 10;
		
		// Parallel game
		IntStream.range(0, numStream).parallel().forEach(i -> {
			try  { parallelDataCollection(i); } 
			catch (IOException e) {e.printStackTrace(); }
			}
		);
		
		// Combine data from multiple different streams
		FileWriter fw_gamestates = new FileWriter("/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-Data/Test2_June19_gamestates.txt");
		fw_gamestates.write("Player 0: SimpleGinRummyPlayer.java\n" + 
							"Player 1: SimpleGinRummyPlayer.java\n" + 
							"10 of 1000 games, in ______ms each.\n" + 
							"Games Won: P0:______, P1:______.\n\n" + 
							"Data collected is the last state of player 0 right before knocking: meld, deadwood, hitscore, ranks\n" +
							"Date: July 19, 2020\n" +
							"----------------------------------------------------------------------\n\n");
		
		for(int i = 0; i < numStream; i++)
		{
			File f_gamestates = new File("/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-Data/Test_gamestates_" + i + ".txt");			
			Scanner sc_gamestates = new Scanner(f_gamestates);
			while (sc_gamestates.hasNextLine()) fw_gamestates.write(sc_gamestates.nextLine() + "\n");
			sc_gamestates.close();
			f_gamestates.delete();
		}
		fw_gamestates.close();
	}
}