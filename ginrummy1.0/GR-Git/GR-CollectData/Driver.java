import java.io.File;
import java.io.FileWriter;
//import java.util.ArrayList;
import java.util.Scanner;

public class Driver {
	public static void main(String args[]) throws Exception {
		
		@SuppressWarnings("resource")
		FileWriter fw_gamestates = new FileWriter("gamestates.txt");
		
		for(int i = 0; i < 10; i++)
		{
			File f_gamestates = new File("/Users/sangtruong_2021/Desktop/DATA/gamestates_" + i + ".txt");			
			@SuppressWarnings("resource")
			Scanner sc_gamestates = new Scanner(f_gamestates);
			while (sc_gamestates.hasNextLine()) fw_gamestates.write(sc_gamestates.nextLine() + "\n");
			f_gamestates.delete();
		}
		
//		ArrayList<Card> hand0 = new ArrayList<Card>();
//		hand0.add(Card.getCard(4, 0));
//		hand0.add(Card.getCard(5, 0));
//		hand0.add(Card.getCard(6, 0));
//		hand0.add(Card.getCard(8, 0));
//		hand0.add(Card.getCard(12, 0));
//		hand0.add(Card.getCard(2, 1));
//		hand0.add(Card.getCard(5, 1));
//		hand0.add(Card.getCard(11, 1));
//		hand0.add(Card.getCard(4, 2));
//		hand0.add(Card.getCard(5, 2));
//		hand0.add(Card.getCard(6, 2));		
		}
	}