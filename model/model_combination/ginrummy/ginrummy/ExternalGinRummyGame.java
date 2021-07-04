package ginrummy;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Queue;
import java.util.stream.Collectors;


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
public class ExternalGinRummyGame {
	/**
	 */
	private static final int HAND_SIZE = 10;

	/**
	 */
	private GinRummyPlayer player;

	/**

	 */
	public ExternalGinRummyGame(GinRummyPlayer player) {
		this.player = player;
	}

	/*
	 */
	private boolean askYesNoQuestion(Scanner input, String prompt) {
		while (true) {
			System.out.printf(prompt);
			String response = input.nextLine().toUpperCase();
			switch(response) {
				case "Y": return true;
				case "N": return false;
				default:
					System.out.printf("Say again...%n");
			}
		}
	}

	/*
	 */
	private List<Card> askCardQuestion(Scanner input, String prompt, int expected) {
		while (true) {
			System.out.printf(prompt);
			String[] response = input.nextLine().toUpperCase().split("\\s+");
			List<Card> cards = Arrays.stream(response)
				.map(str -> Card.strCardMap.get(str))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
			if (cards.size() == expected) { return new ArrayList<Card>(cards); }
			else {
				System.out.printf("Say again...%n");
			}
		}
	}

	/**
	 *
	 * @return the winning player number 0 or 1
	 */
	public void play() {
		Scanner input = new Scanner(System.in);

		// request hand dealt to agent
		List<Card> hand = askCardQuestion(input, "Dealt Hand (space-deliniated pairs, e.g., 'AD 5C ...')? ", 10);

		// request starting player (0 for agent, 1 for opponent)
		int startingPlayer = askYesNoQuestion(input, "Is agent the starting player (Y/N)? ") ? 0 : 1;

		// initialize player
		player.startGame(0, startingPlayer, hand.toArray(new Card[HAND_SIZE]));
		System.out.printf("Agent is dealt %s.%n", hand);

		// starting face up card
		Queue<Card> discards = Collections.asLifoQueue(new ArrayDeque<Card>());
		discards.addAll(askCardQuestion(input, "Initial discard (pair, e.g., 'AD')? ", 1));

		final Card firstFaceUpCard = discards.element();
		System.out.printf("The initial face up card is %s.%n", firstFaceUpCard);

		// start game play
		int currentPlayer = startingPlayer;
		int opponent = (currentPlayer == 0) ? 1 : 0;

		for(int turnsTaken = 0; true; turnsTaken += 1) {
			Card faceUpCard = discards.element();

			// offer to draw face-up card, except in the situation where both
			// players have declined the first face up card
			boolean drawFaceUp = false;
			if (turnsTaken != 2 || faceUpCard != firstFaceUpCard) { //!! card equality??
				System.out.printf("Offering %s to %s.%n", faceUpCard, currentPlayer == 0 ? "Agent" : "Opponent");
				if (currentPlayer == 0) { drawFaceUp = player.willDrawFaceUpCard(faceUpCard); }
				if (currentPlayer == 1) { drawFaceUp = askYesNoQuestion(input, "Does opponent draw face up card (Y/N)? "); }

				if (!drawFaceUp && faceUpCard == firstFaceUpCard && turnsTaken < 2) {
					System.out.printf("%s declines %s.%n", currentPlayer == 0 ? "Agent" : "Opponent", firstFaceUpCard);
				}
			}

			// continue with turn assuming, the initial card was not declined,
			// using the draw face-up card decision (or forced decision)
			if (drawFaceUp || turnsTaken >= 2 || faceUpCard != firstFaceUpCard) {
				// draw the chosen card and report the results (or null, if
				// hidden) to the appropriate players
				if (currentPlayer == 0) {
					// report draw
					System.out.printf("Agent draws %s card.%n", drawFaceUp ? "face up" : "face down");
					Card drawCard = drawFaceUp ? discards.remove() : askCardQuestion(input, "Agent face down draw (pair, e.g., 'AD')? ", 1).get(0);
					player.reportDraw(currentPlayer, drawCard);

					// request the discard from the drawing agent
					Card discardCard = player.getDiscard();
					System.out.printf("Agent discards %s.%n", discardCard);
					discards.add(discardCard);

					// report discard
					player.reportDiscard(currentPlayer, discardCard);

					// check for knocks
					ArrayList<ArrayList<Card>> knockMelds = player.getFinalMelds();
					if (knockMelds != null) {
						System.out.printf("Agent knocks with melds %s.%n", knockMelds);
						break;
					}
				}
				if (currentPlayer == 1) {
					// report draw
					System.out.printf("Opponent draws %s card.%n", drawFaceUp ? "face up" : "face down");
					Card drawCard = drawFaceUp ? discards.remove() : null;
					player.reportDraw(currentPlayer, drawCard);

					// request the discard from the drawing opponent
					Card discardCard = askCardQuestion(input, "Opponent discard (pair, e.g., 'AD')? ", 1).get(0);
					discards.add(discardCard);

					// report discard
					player.reportDiscard(currentPlayer, discardCard);

					// check for knocks
					if (askYesNoQuestion(input, "Opponent knocks (Y/N)? ")) { break; }
				}
			}

			currentPlayer = (currentPlayer == 0) ? 1 : 0;
			opponent = (currentPlayer == 0) ? 1 : 0;
		}
	}


	/**
	 * Test and demonstrate the use of the GinRummyGame class.
	 * @param args (unused)
	 */
	public static void main(String[] args) {
		ExternalGinRummyGame game = new ExternalGinRummyGame(new SimpleGinRummyPlayer());
		game.play();
	}

}
