package conquest.engine;

import java.io.File;

import conquest.game.GameConfig;

public class Config implements Cloneable {
	
	public String gameId = "GAME";
	
	/**
	 * Used by ENGINE as PLAYER IDENTIFIER of the player 1
	 * BETTER NOT TO ALTER AT ALL...
	 */
	public String playerId1 = "PLR1";
	/**
	 * Used by ENGINE as PLAYER IDENTIFIER of the player 2
	 * BETTER NOT TO ALTER AT ALL...
	 */
	public String playerId2 = "PLR2";
	
	/**
	 * Human-readable name of player 1 to display during visualization or to report into CSV.
	 */
	public String player1Name = "Bot1";
	/**
	 * Human-readable name of player 2 to display during visualization or to report into CSV.
	 */
	public String player2Name = "Bot2";
	
	public String bot1Init;
	public String bot2Init;
	
    public long botCommandTimeoutMillis = 2000;
	
	public boolean visualize = true;
	
	public Boolean visualizeContinual = null;
	
	public Integer visualizeContinualFrameTimeMillis = null;
	
	public boolean logToConsole = true;
	
	public File replayLog = null;
	
	public GameConfig game = new GameConfig();
	
	public String asString() {
		return gameId + ";" + playerId1 + ";" + playerId2 + ";" + player1Name + ";" + player2Name + ";" +
		       botCommandTimeoutMillis + ";" +
	           visualize + ";" + visualizeContinual + ";" + visualizeContinualFrameTimeMillis + ";" +
			   logToConsole + ";" + game.asString();
	}
	
	@Override
	public Config clone() {
		Config result = fromString(asString());
		
		result.replayLog = replayLog;
		result.bot1Init = bot1Init;
		result.bot2Init = bot2Init;
		
		return result;
	}
	
	public String getCSVHeader() {
		return "ID;PlayerName1;PlayerName2;timeoutMillis;" + game.getCSVHeader();
	}
	
	public String getCSV() {
		return gameId + ";" + player1Name + ";" + player2Name + ";" +
	           botCommandTimeoutMillis + ";" + game.getCSV();
	}
	
	public static Config fromString(String line) {
		
		String[] parts = line.split(";");
		
		Config result = new Config();

		result.gameId = parts[0];
		result.playerId1 = parts[1];
		result.playerId2 = parts[2];
		result.player1Name = parts[3];
		result.player2Name = parts[4];
		result.botCommandTimeoutMillis = Integer.parseInt(parts[5]);
		result.visualize = Boolean.parseBoolean(parts[6]);
		result.visualizeContinual = (parts[7].toLowerCase().equals("null") ? null : Boolean.parseBoolean(parts[7]));
		result.visualizeContinualFrameTimeMillis = (parts[8].toLowerCase().equals("null") ? null : Integer.parseInt(parts[8]));
		result.logToConsole = Boolean.parseBoolean(parts[9]);
		
		int engineConfigStart = 0;
		for (int i = 0; i < 10; ++i) {
			engineConfigStart = line.indexOf(";", engineConfigStart);
			++engineConfigStart;
		}
		
		result.game = GameConfig.fromString(line.substring(engineConfigStart));
		
		return result;
	}
	
}