package conquest.bot.playground;

import java.io.File;
import java.util.*;

import conquest.bot.BotParser;
import conquest.bot.state.*;
import conquest.bot.state.GameState.RegionState;
import conquest.engine.Engine.FightMode;
import conquest.engine.RunGame;
import conquest.engine.RunGame.Config;
import conquest.game.world.Region;
import conquest.view.GUI;

/**
 */
public class MyBot extends GameBot
{
	Random rand = new Random();
	
	@Override
	public void setGUI(GUI gui) {
	}
	
	// Code your bot here.
	
	//
	// This is a dummy implemementation that moves randomly.
	//
	
	// Return 6 starting regions in order of preference.
	
	@Override
	public List<ChooseCommand> chooseRegions(List<Region> choosable, long timeout) {
		List<Region> l = new ArrayList<Region>(choosable);	// make a copy
		Collections.shuffle(l);  // shuffle it randomly
		
		List<ChooseCommand> ret = new ArrayList<ChooseCommand>();
		for (int i = 0 ; i < 6 ; ++i)
			ret.add(new ChooseCommand(l.get(i)));
		return ret;
	}

	// Decide where to place armies this turn.
	// state.me.placeArmies is the number of armies available to place.
	
	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
		List<Region> mine = new ArrayList<Region>(state.me.regions.keySet());
		int numRegions = mine.size();
		
		int[] count = new int[numRegions];
		for (int i = 0 ; i < state.me.placeArmies ; ++i) {
			int r = rand.nextInt(numRegions);
			count[r]++;
		}
		
		List<PlaceCommand> ret = new ArrayList<PlaceCommand>();
		for (int i = 0 ; i < numRegions ; ++i)
			if (count[i] > 0)
				ret.add(new PlaceCommand(mine.get(i), count[i]));
		return ret;
	}
	
	// Decide where to move armies this turn.
	
	@Override
	public List<MoveCommand> moveArmies(long timeout) {
		List<MoveCommand> ret = new ArrayList<MoveCommand>();
		
		for (RegionState rs : state.me.regions.values()) {
			int count = rand.nextInt(rs.armies);
			if (count > 0) {
				List<Region> neighbors = rs.region.getNeighbours();
				Region to = neighbors.get(rand.nextInt(neighbors.size()));
				ret.add(new MoveCommand(rs.region, to, count));
			}
		}
		return ret;		
	}
	
	public static void runInternal() {
		Config config = new Config();
		
		config.bot1Init = "internal:conquest.bot.playground.MyBot";
		
		config.bot2Init = "internal:conquest.bot.custom.AggressiveBot";
		//config.bot2Init = "human";
		
		config.engine.botCommandTimeoutMillis = 20 * 1000;
		
		config.engine.maxGameRounds = 200;
		
		config.engine.fight = FightMode.CONTINUAL_1_1_A60_D70;
		
		config.visualize = true;
		config.forceHumanVisualization = true; // prepare for hijacking bot controls
		
		config.replayLog = new File("./replay.log");
		
		RunGame run = new RunGame(config);
		run.go();
		
		System.exit(0);
	}
	
	public static void runExternal() {
		BotParser parser = new BotParser(new MyBot());
		parser.setLogFile(new File("./MyBot.log"));
		parser.run();
	}

	public static void main(String[] args)
	{
		runInternal();

		//JavaBot.exec(new String[]{"conquest.bot.custom.AggressiveBot", "./AggressiveBot.log"});
	}

}
