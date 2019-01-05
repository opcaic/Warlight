package conquest.bot.custom;

import java.io.File;
import java.util.*;

import conquest.bot.BotParser;
import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.fight.FightSimulation.FightDefendersResults;
import conquest.bot.map.RegionBFS;
import conquest.bot.map.RegionBFS.*;
import conquest.bot.state.*;
import conquest.engine.Config;
import conquest.engine.GameResult;
import conquest.engine.RunGame;
import conquest.game.FightMode;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.utils.Util;
import conquest.view.GUI;

public class AggressiveBot extends GameBot 
{
	FightAttackersResults aRes;
	FightDefendersResults dRes;
	
	public AggressiveBot() {
		aRes = FightAttackersResults.loadFromFile(Util.file("../Conquest-Bots/FightSimulation-Attackers-A200-D200.obj"));
		dRes = FightDefendersResults.loadFromFile(Util.file("../Conquest-Bots/FightSimulation-Defenders-A200-D200.obj"));
		System.err.println("---==[ AGGRESSIVE BOT INITIALIZED ]==---");
	}
	
	@Override
	public void setGUI(GUI gui) {
	}
	
	// ================
	// CHOOSING REGIONS
	// ================
	
	@Override
	public ChooseCommand chooseRegion(List<Region> choosable, long timeout) {
	    int min = Integer.MAX_VALUE;
	    Region best = null;
	    
	    for (Region r : choosable) {
	        int p = getPreferredContinentPriority(r.continent);
	        if (p < min) {
	            min = p;
	            best = r;
	        }
	    }
	    
		return new ChooseCommand(best);
	}
	
	public int getPreferredContinentPriority(Continent continent) {
		switch (continent) {
		case Australia:     return 1;
		case South_America: return 2;
		case North_America: return 3;
		case Europe:        return 4;		
		case Africa:        return 5;
		case Asia:          return 6;
		default:            return 7;
		}
	}

	// ==============
	// PLACING ARMIES
	// ==============
	
	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
	    PlayerState me = state.players[state.me];
	    System.out.format("AggressiveBot: placing %d armies\n", me.placeArmies);
		List<PlaceCommand> result = new ArrayList<PlaceCommand>();
		
		// CLONE REGIONS OWNED BY ME
		List<RegionState> mine = new ArrayList<RegionState>(me.regions.values());
		
		// SORT THEM IN DECREASING ORDER BY SCORE
		Collections.sort(mine, new Comparator<RegionState>() {

			@Override
			public int compare(RegionState o1, RegionState o2) {
				int regionScore1 = getRegionScore(o1);
				int regionScore2 = getRegionScore(o2);
				return regionScore2 - regionScore1;
			}

		});
		
		// DO NOT ADD SOLDIER TO REGIONS THAT HAVE SCORE 0 (not perspective)
		int i = 0;
		while (i < mine.size() && getRegionScore(mine.get(i)) > 0) ++i;
		while (i < mine.size()) mine.remove(i);

		// DISTRIBUTE ARMIES
		int armiesLeft = me.placeArmies;
		
		int index = 0;
		
		while (armiesLeft > 0) {
		    int count = Math.min(3, armiesLeft);
			result.add(new PlaceCommand(mine.get(index).region, count));
			armiesLeft -= count;
			++index;
			if (index >= mine.size()) index = 0;
		}
		
		return result;
	}
	
	private int getRegionScore(RegionState o1) {
		int result = 0;
		
		for (Region reg : o1.region.getNeighbours()) {
			result += (state.region(reg).owned(0) ? 1 : 0) * 5;
			result += (state.region(reg).owned(state.opp) ? 1 : 0) * 2;
		}
		
		return result;
	}

	// =============
	// MOVING ARMIES
	// =============

	@Override
	public List<MoveCommand> moveArmies(long timeout) {
		List<MoveCommand> result = new ArrayList<MoveCommand>();
		Collection<RegionState> regions = state.players[state.me].regions.values();
		
		// CAPTURE ALL REGIONS WE CAN
		for (RegionState from : regions) {
			int available = from.armies - 1;  // 1 army must stay behind
			
			for (RegionState to : from.neighbours) {
				// DO NOT ATTACK OWN REGIONS
				if (to.owned(state.me)) continue;
				
				// IF YOU HAVE ENOUGH ARMY TO WIN WITH 70%
				int need = getRequiredSoldiersToConquerRegion(from, to, 0.7);
				
				if (available >= need) {
					// => ATTACK
					result.add(new MoveCommand(from.region, to.region, need));
					available -= need;
				}
			}
		}
		
		// MOVE LEFT OVERS CLOSER TO THE FRONT
		for (RegionState from : regions) {
			if (hasOnlyMyNeighbours(from) && from.armies > 1) {
				result.add(moveToFront(from));
			}
		}
		
		return result;
	}
	
	private boolean hasOnlyMyNeighbours(RegionState from) {
		for (RegionState region : from.neighbours) {			
			if (!region.owned(state.me)) return false;
		}
		return true;
	}

	private int getRequiredSoldiersToConquerRegion(RegionState from, RegionState to, double winProbability) {
		int attackers = from.armies - 1;
		int defenders = to.armies;
		
		for (int a = defenders; a <= attackers; ++a) {
			double chance = aRes.getAttackersWinChance(a, defenders);
			if (chance >= winProbability) {
				return a;
			}
		}
		
		return Integer.MAX_VALUE;
	}
		
	private MoveCommand transfer(RegionState from, RegionState to) {
		MoveCommand result = new MoveCommand(from.region, to.region, from.armies-1);
		return result;
	}
	
	private Region moveToFrontRegion;
	
	private MoveCommand moveToFront(RegionState from) {
		RegionBFS<BFSNode> bfs = new RegionBFS<BFSNode>();
		moveToFrontRegion = null;
		bfs.run(from.region, new BFSVisitor<BFSNode>() {

			@Override
			public BFSVisitResult<BFSNode> visit(Region region, int level, BFSNode parent, BFSNode thisNode) {
				//System.err.println((parent == null ? "START" : parent.level + ":" + parent.region) + " --> " + level + ":" + region);
				if (!hasOnlyMyNeighbours(state.region(region))) {
					moveToFrontRegion = region;
					return new BFSVisitResult<BFSNode>(BFSVisitResultType.TERMINATE, thisNode == null ? new BFSNode() : thisNode);
				}
				return new BFSVisitResult<BFSNode>(thisNode == null ? new BFSNode() : thisNode);
			}
			
		});
		
		if (moveToFrontRegion != null) {
			//List<Region> path = fw.getPath(from.getRegion(), moveToFrontRegion);
			List<Region> path = bfs.getAllPaths(moveToFrontRegion).get(0);
			Region moveTo = path.get(1);
			
			boolean first = true;
			for (Region region : path) {
				if (first) first = false;
				else System.err.print(" --> ");
				System.err.print(region);
			}
			System.err.println();
			
			return transfer(from, state.region(moveTo));
		}
		
		return null;
	}
	
	
	public static void runInternal() {
		Config config = new Config();
		
		config.bot1Init = "internal:conquest.bot.custom.AggressiveBot";
		//config.bot1Init = "dir;process:../Conquest-Bots;java -cp ./bin;../Conquest/bin conquest.bot.external.JavaBot conquest.bot.custom.AggressiveBot ./AggressiveBot.log";
		//config.bot2Init = "internal:conquest.bot.BotStarter";
		config.bot2Init = "human";
		
		config.botCommandTimeoutMillis = 24*60*60*1000;
		//config.botCommandTimeoutMillis = 20 * 1000;
		
		config.game.maxGameRounds = 200;
		
		config.game.fight = FightMode.CONTINUAL_1_1_A60_D70;
		
		config.visualize = true;
		
		config.replayLog = new File("./replay.log");
		
		RunGame run = new RunGame(config);
		GameResult result = run.go();
		
		System.exit(0);
	}
	
	public static void runExternal() {
		BotParser parser = new BotParser(new AggressiveBot());
		parser.setLogFile(new File("./AggressiveBot.log"));
		parser.run();
	}

	public static void main(String[] args)
	{
		//JavaBot.exec(new String[]{"conquest.bot.custom.AggressiveBot", "./AggressiveBot.log"});		
		runInternal();
	}

}
