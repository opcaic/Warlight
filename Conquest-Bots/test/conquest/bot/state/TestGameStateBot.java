package conquest.bot.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import conquest.game.world.Region;
import conquest.view.GUI;

public class TestGameStateBot extends GameBot 
{
	public boolean gameStateTested = false;
	public boolean gameStateTestOk = true;
	
	private Random random = new Random(System.nanoTime());
	
	public TestGameStateBot() {
	}
	
	@Override
	public void setGUI(GUI gui) {
	}
	
	// ================
	// CHOOSING REGIONS
	// ================
	
	@Override
	public List<ChooseCommand> chooseRegions(List<Region> choosable, long timeout) {
		// CREATE COMMANDS
		List<ChooseCommand> result = new ArrayList<ChooseCommand>(choosable.size());
		for (Region region : choosable) {
			result.add(new ChooseCommand(region));
		}		
		return result;
	}
	
	// ==============
	// PLACING ARMIES
	// ==============
	
	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
		List<PlaceCommand> result = new ArrayList<PlaceCommand>();
		
		// CLONE REGIONS OWNED BY ME
		List<RegionState> mine = new ArrayList<RegionState>(state.me.regions.values());
		
		// DISTRIBUTE ARMIES
		int armiesLeft = state.me.placeArmies;
		
		int index = 0;
		
		while (armiesLeft > 0) {
			result.add(new PlaceCommand(mine.get(index).region, 3));
			armiesLeft -= 3;
			++index;
			index %= mine.size();
		}
		
		return result;
	}

	// =============
	// MOVING ARMIES
	// =============

	private void gameStateTestFailed(String msg) {
		gameStateTestOk = false;
		throw new RuntimeException(msg);
	}
	
	@Override
	public List<MoveCommand> moveArmies(long timeout) {
		testGameState();

		List<MoveCommand> result = new ArrayList<MoveCommand>();
		return result;
	}

	private List<ICommand> testGameState() {
		System.out.println("TEST GAME STATE!");
		
		gameStateTested = true;
		
		GameState clone1 = state.clone();
		
		if (!clone1.equals(state)) {
			clone1 = state.clone();
			clone1.equals(state);
			gameStateTestFailed("GameState.clone() not working correctly! GameState not equals after clone!");
		}
		
		GameState clone2 = clone1.clone();
		
		if (!clone2.equals(state)) {
			clone2 = clone1.clone();
			clone2.equals(state);
			gameStateTestFailed("GameState.clone() not working correctly! GameState not equals after cloning twice!");
		}
		
		if (!clone2.equals(clone1)) {
			clone2.equals(clone1);
			gameStateTestFailed("GameState.clone() not working correctly! Two clones of the state are not equal!");
		}
		
		// DOUBLE-SWAP PLAYERS
		clone1.swapPlayers();
		clone1.swapPlayers();
		
		if (!clone1.equals(clone2)) {
			gameStateTestFailed("GameState.swapPlayers() not working correctly! GameState not equals after swapping the players twice!");
		}
		
		// APPLY / REVERT RANDOM MOVES
		
		List<ICommand> commands = new ArrayList<ICommand>();
		
		// GENERATE UP-TO 100 COMMANDS, APPLY/REVERT THEM EVERY ITERATION
		for (int i = 0; i < 100; ++i) {
			System.out.println("  +-- Iteration!");
			
			if (!clone1.equals(clone2)) {
				gameStateTestFailed("GameState commands apply/revert not working correctly! Clones not equals after apply/revert!");
			}
			
			// APPLY ALL COMMANDS
			for (int j = 0; j < commands.size(); ++j) {
				System.out.println("    +-- APPLYING[" + j + "] : " + commands.get(j));
				String msg = commands.get(j).toString();
				commands.get(j).apply(clone1);
				if (!msg.equals(commands.get(j).toString())) {
					System.out.println("      +-- Change: " + commands.get(j));
				}
				
			}
			
			// GENERATE ANOTHER COMMAND
			ICommand cmd = null;
			if (random.nextDouble() > 0.5) {
				cmd = getRandomAttackCommand(clone1);
			}
			if (cmd == null) {
				cmd = getRandomMoveCommend(clone1);				
			}
			// APPLY NEW COMMAND
			if (cmd != null) {
				System.out.println("    +-- APPLYING[" + commands.size() + "] : " + cmd);
				String msg = cmd.toString();
				cmd.apply(clone1);
				if (!msg.equals(cmd.toString())) {
					System.out.println("      +-- Change: " + cmd);
				}
				// ADD COMMANDS TO THE LIST OF APPLIED COMMANDS
				commands.add(cmd);
			}
			
			// REVERT ALL APPLIED COMMANDS
			for (int j = commands.size()-1; j >= 0; --j) {
				System.out.println("    +-- REVERTING[" + j + "]: " + commands.get(j));
				commands.get(j).revert(clone1);
			}
			
			// CHECK THE RESULT
			if (!clone1.equals(clone2)) {
				clone1.equals(clone2);
				gameStateTestFailed("GameState commands apply/revert not working correctly! Clones not equals after apply/revert! Last added command: " + cmd);
			}
			
			if (cmd == null) {
				// no more commands...
				break;
			}			
		}	
		
		return commands;
	}

	private MoveCommand getRandomMoveCommend(GameState state) {
		List<RegionState> suitable = new ArrayList<RegionState>();
		
		for (RegionState region : state.me.regions.values()) {
			if (region.armies > 1) {
				for (RegionState neigh : region.neighbours) {
					if (neigh.isMine()) {
						suitable.add(region);
						break;
					}
				}					
			}
		}
		
		if (suitable.size() == 0) return null;
		
		RegionState moveFrom = suitable.get(random.nextInt(suitable.size()));
		
		suitable.clear();
		
		for (RegionState neigh : moveFrom.neighbours) {
			if (neigh.isMine()) {
				suitable.add(neigh);
				break;
			}
		}	
				
		RegionState moveTo = suitable.get(random.nextInt(suitable.size()));
		
		return new MoveCommand(moveFrom, moveTo, random.nextInt(moveFrom.armies+2));
	}

	private AttackCommand getRandomAttackCommand(GameState state) {
		List<RegionState> suitable = new ArrayList<RegionState>();
		
		for (RegionState region : state.me.regions.values()) {
			if (region.armies > 1) {
				for (RegionState neigh : region.neighbours) {
					if (!neigh.isMine()) {
						suitable.add(region);
						break;
					}
				}					
			}
		}
		
		if (suitable.size() == 0) return null;
		
		RegionState moveFrom = suitable.get(random.nextInt(suitable.size()));
		
		suitable.clear();
		
		for (RegionState neigh : moveFrom.neighbours) {
			suitable.add(neigh);
		}	
				
		RegionState moveTo = suitable.get(random.nextInt(suitable.size()));
		
		int armies = random.nextInt(moveFrom.armies+2);
		int attackersCasaulties = random.nextInt(armies+2);
		int defendersCasaulties = random.nextInt(moveTo.armies*2);
		
		return new AttackCommand(moveFrom, moveTo, armies, attackersCasaulties, defendersCasaulties);
	}

}