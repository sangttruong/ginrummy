import java.io.File;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Terminal {
	public static void main(String[] args) throws InterruptedException {
		try {
			String[] cmdarray = new String[] {
					"/bin/bash",
					"-lc",
					"python Documents/GitHub/GR-Git/GR-TestModel/NN/NN1_run.py"
					};

			File dir = new File("/Users/sangtruong_2021/");

			Runtime rt = Runtime.getRuntime();
			rt.exec(cmdarray, null, dir);
			
			Process p = rt.exec(cmdarray, null, dir);
			InputStream is = p.getErrorStream();
			Reader r = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(r);
			String a = br.readLine();
			while (a != null) {
				System.out.println(a);
				a = br.readLine();
				}
			System.out.println("Done");
	      } catch (IOException e) {
	         e.printStackTrace();
	         }
		}
	}