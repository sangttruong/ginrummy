import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * 
 * @author Sang T. Truong and Hoang H. Pham
 * @version 1.0
 * 
   FeatureGenerator class is used to generate all features of a given GameState object OR a set of several GameState objects. 
   
   FeatureGenerator includes 2 static overloading method: 
   - GenerateFeatures (GameState gs): Generate all features for a given game-state.
   - GenerateFeatures (String filename): Generate all features for a given set of game-states. 
   
   The 2 above functions will call FeatureEngineer0 and FeatureEngineer1 to get all features of both player 0 and player 1. 
 */

public class FeaturesGenerator {

	/**
	 * Take a game-state and return a 
	 * @param gs
	 * @return
	 */
	public static ArrayList<Double> GenerateFeatures(GameState gs, double k) {
	ArrayList<Double> features = new ArrayList<Double>();
	ArrayList<Double> features0 = new ArrayList<Double>();
	ArrayList<Double> features1 = new ArrayList<Double>();
	
	features0 = FeatureEngineer0.getFeatures(gs, k);
	features1 = FeatureEngineer1.getFeatures(gs);
	
	for (double feature: features0) features.add(feature);
	for (double feature: features1) features.add(feature);
	
	return features;
	}
	
	/**
	 * Overloading method that takes a set of game-states from many game-plays from a file
	 * @param gameplaysFile
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<Double> GenerateFeatures(String gameplaysFile, double k) {
		// Load all game-plays from file
		// -----------------------------------------------------------------------------------
		ArrayList<ArrayList<GameState>> gameplays = new ArrayList<ArrayList<GameState>>() ;
		
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(gameplaysFile));
			gameplays = (ArrayList<ArrayList<GameState>>) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + gameplaysFile);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// -----------------------------------------------------------------------------------
		ArrayList<Double> features = new ArrayList<Double>();
		for (ArrayList<GameState> gameplay: gameplays)
		{
			for (GameState gs: gameplay)
			{
				ArrayList<Double> temp_features = FeaturesGenerator.GenerateFeatures(gs, k);
				if (gs.getHand0() != 0 && gs.getProb0() != null && gs.getEstimatedHand1() != 0) {
					for(double feature: temp_features) features.add(feature);
				}
			}
		}
		return features;
	}
}