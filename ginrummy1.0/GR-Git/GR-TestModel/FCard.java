import java.util.Collections;
import java.util.Stack;
import java.util.ArrayList;

public class FCard {

    private static String[] fixedDeck = new String[] {"TD", "7S", "AC", "TH", "TS", "7H", "4H", "QD", "3C", "8D", "JS", "5C", "KS", "6C", "9D", "6H", "6D", "9H", "AD", "2S", "JC", "AS", "8H", "4C", "2C", "5H", "KC", "2D", "8S", "QH", "5S", "6S", "JD", "4S", "JH", "9S", "8C", "3D", "KH", "9C", "AH", "KD", "4D", "5D", "2H", "7C", "QC", "TC", "3S", "7D", "QS", "3H"};
	
	/**
	 * array of abbreviated card rank names in ascending order of rank and indexed by suit index
	 */
	public static String[] rankNames = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K"};

	/**
	 * array of abbreviated card suit names indexed by suit index
	 */
	public static String[] suitNames = {"C", "H", "S", "D"}; 

	/**
	 * number of card ranks
	 */
	public static final int NUM_RANKS = rankNames.length;
	
	/**
	 * number of card suits
	 */
	public static final int NUM_SUITS = suitNames.length;
	
	/**
	 * total number of cards
	 */
	public static final int NUM_CARDS = NUM_RANKS * NUM_SUITS;
	
	
	/**
	 * Return a fixed Stack deck of Cards
	 * @param seed It doesn't do anything
	 * @return a fixed Stack deck of Cards
	 */
	static public Stack<Card> getShuffle(int seed) {
		Stack<Card> deck = new Stack<Card>();
		for (int i = 0; i < NUM_CARDS; i++)
			deck.push(Card.strCardMap.get(FCard.fixedDeck[i]));
		return deck;
	}

	public static void main(String[] args) {
        ArrayList<String> deck = new ArrayList<String>();
        
        for (String rank: rankNames)
            for (String suit: suitNames)
                deck.add(rank + suit);

        for (int i = 0; i < 10; i++)
            Collections.shuffle(deck);

        System.out.print("[");
        for (String card: deck)
            System.out.printf("\"%s\", ",card);
        System.out.println("]");
            
	}
}
