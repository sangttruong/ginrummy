import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Sockets
{	
	public static void main(String[] args)
	{
		try {
			Socket s = new Socket("127.0.0.1", 8000);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			// Send a number to python
			ArrayList<ArrayList<Integer>> a = new ArrayList<ArrayList<Integer>>();
			
			ArrayList<Integer> b = new ArrayList<Integer>();
			b.add(1);
			b.add(2);
			a.add(b);
			a.add(b);
            out.println(a);		
			System.out.println("Input: " + a);
			
			// Receive a number from python
			String message;
            while ((message = in.readLine()) != null)
            {
            	System.out.println("Result: " + message);
            	message = message.replace("[", "");
            	message = message.replace("]", "");
            	String str[] = message.split(",");
            	ArrayList<String> result = (ArrayList<String>) Arrays.asList(str);
            	
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
}
