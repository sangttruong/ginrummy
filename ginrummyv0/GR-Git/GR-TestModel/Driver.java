import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Driver {
	public static void main(String[] args) throws IOException
	{
		ArrayList<Card> hand0 = new ArrayList<Card>();
		hand0.add(Card.getCard(4, 0));
		hand0.add(Card.getCard(5, 0));
		hand0.add(Card.getCard(6, 0));
		hand0.add(Card.getCard(8, 0));
		hand0.add(Card.getCard(12, 0));
		hand0.add(Card.getCard(2, 1));
		hand0.add(Card.getCard(5, 1));
		hand0.add(Card.getCard(11, 1));
		hand0.add(Card.getCard(4, 2));
		hand0.add(Card.getCard(5, 2));
		hand0.add(Card.getCard(6, 2));
		
		File temp = File.createTempFile("features", ".txt", new File("/Users/sangtruong_2021/Desktop"));
		FileWriter writeState = new FileWriter(temp);
		writeState.write("Hello world");
		writeState.close();
	}

}
