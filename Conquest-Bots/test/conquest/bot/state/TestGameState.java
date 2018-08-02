package conquest.bot.state;

import java.io.File;

import conquest.engine.Engine.FightMode;
import conquest.engine.RunGame;
import conquest.engine.RunGame.Config;
import conquest.engine.RunGame.GameResult;

public class TestGameState {

	public static void runInternal() {
		Config config = new Config();
		
		config.bot1Init = "internal:conquest.bot.state.TestGameStateBot";
		config.bot2Init = "internal:conquest.bot.BotStarter";
		
		config.engine.botCommandTimeoutMillis = 24*60*60*1000;
		
		config.engine.maxGameRounds = 200;
		
		config.engine.fight = FightMode.CONTINUAL_1_1_A60_D70;
		
		config.visualize = true;
		config.forceHumanVisualization = true; // prepare for hijacking bot controls
		
		config.replayLog = new File("./replay.log");
		
		RunGame run = new RunGame(config);
		GameResult result = run.go();
		
		System.exit(0);
	}

	public static void main(String[] args)
	{
		runInternal();
	}
	
}
