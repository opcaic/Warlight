package conquest.engine;

import conquest.Conquest;

/**
 * Class that can be used to quickly run some replay manually.  
 * 
 * @author Jimmy
 */
public class RunReplay {

	public static void main(String[] args) {
		// Configure with with: "path/to/your/replay.log";
		String replayFile = "./replay.log";
		
		Conquest.startReplay(replayFile);
	}
	
}
