package conquest.engine;

import conquest.game.FightMode;
import conquest.game.GameConfig;

public class EngineConfig implements Cloneable {
	
	/**
	 * Non-negative seed => use concrete seed.
	 * Negative seed => pick random seed.
	 */
	public int seed = -1;
	
	public boolean fullyObservableGame = true;
	
	public long botCommandTimeoutMillis = 2000;
	
	public int startingArmies = 5;
	public int maxGameRounds = 100;
	
	public FightMode fight = FightMode.ORIGINAL_A60_D70;
	
	public GameConfig getGameConfig() {
	    return new GameConfig(fight);
	}
	
	public String asString() {
		return seed + ";" + fullyObservableGame + ";" + botCommandTimeoutMillis + ";" + startingArmies + ";" + maxGameRounds + ";" + fight;
	}
	
	public static EngineConfig fromString(String line) {
		EngineConfig result = new EngineConfig();
		
		String[] parts = line.split(";");
		
		result.seed = Integer.parseInt(parts[0]);
		result.fullyObservableGame = Boolean.parseBoolean(parts[1]);
		result.botCommandTimeoutMillis = Long.parseLong(parts[2]);
		result.startingArmies = Integer.parseInt(parts[3]);
		result.maxGameRounds = Integer.parseInt(parts[4]);
		result.fight = FightMode.valueOf(parts[5]);

		return result;
	}

	public String getCSVHeader() {
		return "seed;fullyObservable;timeoutMillis;startingArmies;maxGameRounds;fightMode";			
	}
	
	public String getCSV() {
		return seed + ";" + fullyObservableGame + ";" + botCommandTimeoutMillis + ";" + startingArmies + ";" + maxGameRounds + ";" + fight;
	}
	
}