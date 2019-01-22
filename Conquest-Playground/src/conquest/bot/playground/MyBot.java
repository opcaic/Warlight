package conquest.bot.playground;

import java.io.File;
import java.util.*;

import conquest.bot.BotParser;
import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.state.*;
import conquest.engine.Config;
import conquest.engine.RunGame;
import conquest.game.FightMode;
import conquest.game.world.Region;
import conquest.utils.Util;
import conquest.view.GUI;

public class MyBot extends GameBot
{
	Random rand = new Random();
	
	FightAttackersResults attackResults;
	
	public MyBot() {
		attackResults = FightAttackersResults.loadFromFile(Util.file(
				"../Conquest-Bots/FightSimulation-Attackers-A200-D200.obj"));
	}
	
	@Override
	public void setGUI(GUI gui) {
	}
		
	// Code your bot here.
	
	//
	// This is a dummy implemementation that moves randomly.
	//
	
	// Choose a starting region.
	
	@Override
	public ChooseCommand chooseRegion(List<Region> choosable, long timeout) {
		return new ChooseCommand(choosable.get(rand.nextInt(choosable.size())));
	}

	// Decide where to place armies this turn.
	// state.me.placeArmies is the number of armies available to place.
	
	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
	    PlayerState me = state.players[state.me];
		List<Region> mine = new ArrayList<Region>(me.regions.keySet());
		int numRegions = mine.size();
		
		int[] count = new int[numRegions];
		for (int i = 0 ; i < me.placeArmies ; ++i) {
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
		
		for (RegionState rs : state.players[state.me].regions.values()) {
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
		
		config.botCommandTimeoutMillis = 20 * 1000;
		
		config.game.maxGameRounds = 200;
		
		config.game.fight = FightMode.CONTINUAL_1_1_A60_D70;
		
		config.visualize = true;
		
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
