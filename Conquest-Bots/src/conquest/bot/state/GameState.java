package conquest.bot.state;

import java.util.HashSet;
import java.util.Set;

import conquest.bot.BotState;
import conquest.bot.state.compact.GameStateCompact;
import conquest.game.Player;
import conquest.game.RegionData;
import conquest.game.world.Continent;
import conquest.game.world.Region;

public class GameState implements Cloneable {
	
	/**
	 * Region state can be found under index 'Region.id'.
	 * 
 	 * Region indices are 1-based! [0] is null!
	 * 
	 * @return
	 */
	public RegionState[] regions;
	
	/**
	 * Continent state can be found under index 'Continent.id'.
	 * 
	 * Continent indices are 1-based! [0] is null!
	 * 
	 * @return
	 */
	public ContinentState[] continents;
	
	/**
	 * Player state can be found under index 'Player.id'.
	 * 
	 * Player indices as 1-based! [0] is null!
	 * 
	 * @return
	 */
	public PlayerState[] players;
	
	/**
	 * My state.
	 */
	public PlayerState me;
	
	/**
	 * Opponent state.
	 */
	public PlayerState opp;
	
	public GameState(GameState state) {
		reset(state);
	}
	
	public GameState(BotState state) {
		reset(state);		
	}
	
	public GameState(GameStateCompact gameStateCompact) {
		reset(gameStateCompact);
	}
	
	@Override
	public GameState clone() {
		return new GameState(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof GameState)) return false;
		GameState other = (GameState)obj;
		
		if (!me.equals(other.me)) {
			System.out.println("NOT EQUALS: " + me + " vs. " + other.me);
			return false;
		}
		if (!opp.equals(other.opp)) {
			System.out.println("NOT EQUALS: " + opp + " vs. " + other.opp);
			return false;
		}
		
		for (int i = 1; i < regions.length; ++i) {
			if (other.regions[i] == null || !regions[i].equals(other.regions[i])) {
				System.out.println("NOT EQUALS: " + regions[i] + " vs. " + other.regions[i]);
				return false;
			}
		}
		for (int i = 1; i < continents.length; ++i) {
			if (other.continents[i] == null || !continents[i].equals(other.continents[i])) {
				System.out.println("NOT EQUALS: " + continents[i] + " vs. " + other.continents[i]);
				return false;
			}
		}
		for (int i = 1; i < players.length; ++i) {
			if (other.players[i] == null || !players[i].equals(other.players[i])) {
				System.out.println("NOT EQUALS: " + players[i] + " vs. " + other.players[i]);
				return false;
			}
		}
	
		return true;
	}

	/**
	 * Recreates all STATE objects.
	 */
	private void reset() {
		// INIT CONTINENTS
		continents = new ContinentState[Continent.values().length+1];
		for (int i = 1; i <= Continent.values().length; ++i) {
			continents[i] = new ContinentState(Continent.values()[i-1]);
		}
		
		// INIT REGIONS
		regions = new RegionState[Region.values().length+1];
		for (int i = 1; i <= Region.values().length; ++i) {
			regions[i] = new RegionState(Region.values()[i-1]);
		}
		
		// INIT REGION NEIGHBOURS
		for (int i = 1; i <= Region.values().length; ++i) {
			regions[i].neighbours = new RegionState[Region.values()[i-1].getNeighbours().size()];
			for (int j = 0; j < Region.values()[i-1].getNeighbours().size(); ++j) {				
				regions[i].neighbours[j] = region(Region.values()[i-1].getNeighbours().get(j));
			}			
		}
		
		// INIT PLAYER STATES
		players = new PlayerState[Player.values().length+1];
		for (int i = 1; i <= Player.values().length; ++i) {
			players[i] = new PlayerState(Player.values()[i-1]);
		}
		
		// INIT PLAYERS
		this.me  = players[Player.ME.id];
		this.opp = players[Player.OPPONENT.id];
	}
	
	/**
	 * Fully (re)creates the state out of the {@link GameStateCompact}, throwing out all existing objects it has.
	 * @param gameStateCompact
	 */
	private void reset(GameStateCompact gameStateCompact) {
		reset();

		// FILL IN STATES
		for (Region region : Region.values()) {
			// REGION
			RegionState regionState = region(region);
			regionState.armies = gameStateCompact.armiesAt(region);
			regionState.owner = player(gameStateCompact.ownedBy(region));
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned[regionState.owner.player.id] += 1;
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			boolean isNeutral = true;
			for (Player player : Player.values()) {
				if (continentState.owned[player.id] == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					if (player != Player.NEUTRAL) {
						playerState.placeArmies += continent.reward;
					}
					isNeutral = player == Player.NEUTRAL;
					break;
				}
			}
			if (isNeutral) {
				continentState.owner = Player.NEUTRAL;
				player(Player.NEUTRAL).continents.put(continent, continentState);
			}
		}
	}

	/**
	 * Fully (re)creates the state out of the {@link BotState}, throwing out all existing objects it has.
	 * @param state
	 */
	private void reset(BotState state) {
		reset();

		// FILL IN STATES
		for (RegionData data : state.getMap().regions) {
			// REGION
			RegionState regionState = region(data.getRegion());
			regionState.armies = data.getArmies();
			Player owner = getRegionOwner(state, data.getPlayerName());
			regionState.owner = player(owner);
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned[regionState.owner.player.id] += 1;
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			boolean isNeutral = true;
			for (Player player : Player.values()) {
				if (continentState.owned[player.id] == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					if (player != Player.NEUTRAL) {
						playerState.placeArmies += continent.reward;
					}
					isNeutral = player == Player.NEUTRAL;
					break;
				}
			}
			if (isNeutral) {
				continentState.owner = Player.NEUTRAL;
				player(Player.NEUTRAL).continents.put(continent, continentState);
			}
		}
	}
	
	/**
	 * Fully (re)creates the state out of the {@link GameState}, throwing out all existing objects it has.
	 * @param gameState
	 */
	private void reset(GameState gameState) {
		reset();

		// FILL IN STATES
		for (Region region : Region.values()) {
			// REGION
			RegionState regionState = region(region);
			regionState.armies = gameState.region(region).armies;
			regionState.owner = player(gameState.region(region).owner.player);
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned[regionState.owner.player.id] += 1;
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			boolean isNeutral = true;
			for (Player player : Player.values()) {
				if (continentState.owned[player.id] == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					if (player != Player.NEUTRAL) {
						playerState.placeArmies += continent.reward;
					}
					isNeutral = player == Player.NEUTRAL;
					break;
				}
			}
			if (isNeutral) {
				continentState.owner = Player.NEUTRAL;
				player(Player.NEUTRAL).continents.put(continent, continentState);
			}
		}
	}
	
	/**
	 * Update this {@link GameState} according to the information from given {@link BotState}.
	 * @param state
	 */
	public void update(BotState state) {
		// RESET TOTAL ARMIES & PLACE ARMIES
		for (int i = 1; i < players.length; ++i) {
			PlayerState player = players[i];
			player.totalArmies = 0;
		}
		
		Set<Continent> regionOwnershipChanged = new HashSet<Continent>();
		
		// UPDATE REGION STATES
		for (RegionData data : state.getMap().regions) {
			// REGION
			RegionState regionState = region(data.getRegion());
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			// PLAYER STATE
			PlayerState oldOwnerState = regionState.owner;
			
			regionState.armies = data.getArmies();
			
			Player newOwner = getRegionOwner(state, data.getPlayerName());
			PlayerState newOwnerState = player(newOwner);
			
			if (newOwner != regionState.owner.player) {
				// OWNER CHANGED
				regionOwnershipChanged.add(regionState.region.continent);
				
				continentState.owned[regionState.owner.player.id] -= 1;
				continentState.owned[newOwner.id] += 1;
				
				oldOwnerState.regions.remove(regionState.region);
				newOwnerState.regions.put(regionState.region, regionState);
				
				regionState.owner = newOwnerState;
			}
			
			newOwnerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : regionOwnershipChanged) {
			ContinentState continentState = continent(continent);
			updateContinentOwnership(continentState);
		}

	}
	
	protected void updateContinentOwnership(Continent continent) {
		updateContinentOwnership(continent(continent));
	}
	
	protected void updateContinentOwnership(ContinentState continentState) {
		Player newOwner = Player.NEUTRAL;
		
		if (continentState.owned[Player.ME.id] == continentState.regions.size()) {
			newOwner = Player.ME;
		} else
		if (continentState.owned[Player.OPPONENT.id] == continentState.regions.size()) {
			newOwner = Player.OPPONENT;
		}
		
		PlayerState newOwnerState = player(newOwner);
		
		if (continentState.owner != newOwner) {
			// CONTINENT OWNER HAS CHANGED!
			PlayerState oldOwnerState = player(continentState.owner);
			
			oldOwnerState.continents.remove(continentState.continent);
			newOwnerState.continents.put(continentState.continent, continentState);
			
			if (oldOwnerState.player != Player.NEUTRAL) oldOwnerState.placeArmies -= continentState.continent.reward;
			if (newOwnerState.player != Player.NEUTRAL) newOwnerState.placeArmies += continentState.continent.reward;					
						
			continentState.owner = newOwnerState.player;
		}
	}
	
	public RegionState region(Region region) {
		return regions[region.id];
	}
	
	public ContinentState continent(Region region) {
		return continents[region.continent.id];
	}
	
	public ContinentState continent(Continent continent) {
		return continents[continent.id];
	}
	
	public PlayerState player(Player player) {
		return players[player.id];
	}
		
	public void apply(PlaceCommand cmd) {
		RegionState region = region(cmd.region); 
		region.armies += cmd.armies;
		region.owner.totalArmies += cmd.armies;
	}
	
	public void revert(PlaceCommand cmd) {
		RegionState region = region(cmd.region);
		region.armies -= cmd.armies;
		region.owner.totalArmies -= cmd.armies;
	}
	
	public void apply(MoveCommand cmd) {
		RegionState from = region(cmd.from);
		RegionState to = region(cmd.to);
		
		if (from.owner.player != to.owner.player) {
			throw new RuntimeException("Cannot apply " + cmd + " as regions have different owners " + from.region + "->" + from.owner.player + " and " + to.region + "->" + to.owner.player + "!");
		}
		
		if (cmd.armies >= from.armies) cmd.armies = from.armies-1;
		
		if (cmd.armies < 1) return;
		
		from.armies            -= cmd.armies;
		to.armies            += cmd.armies;		
	}
	
	public void revert(MoveCommand cmd) {
		RegionState from = region(cmd.from);
		RegionState to = region(cmd.to);
		
		if (from.owner.player != to.owner.player) {
			throw new RuntimeException("Cannot revert " + cmd + " as regions have different owners " + from.region + "->" + from.owner.player + " and " + to.region + "->" + to.owner.player + "!");
		}
		
		if (cmd.armies < 1) return;
		
		from.armies += cmd.armies;		
		to.armies   -= cmd.armies;
	}
	
	public void apply(AttackCommand cmd) {		
		RegionState regionFrom = region(cmd.from);
		RegionState regionTo = region(cmd.to);
		
		PlayerState attacker = regionFrom.owner;
		PlayerState defender = player(cmd.toOwner);
		
		if (regionFrom.armies <= cmd.armies) cmd.armies = regionFrom.armies-1;
		
		if (cmd.armies < 1) return;
		
		if (attacker == defender) {
			// MOVE INSTEAD!
			cmd.movedInstead = new MoveCommand(cmd.from, cmd.to, cmd.armies);
			apply(cmd.movedInstead);
			return;
		} else {
			cmd.movedInstead = null; // just to be sure... may be someone is reusing those instances...
		}
				
		if (cmd.attackersCasaulties < 0)               cmd.attackersCasaulties = 0;
		if (cmd.defendersCasaulties < 0)               cmd.defendersCasaulties = 0;
		if (cmd.attackersCasaulties > cmd.armies)      cmd.attackersCasaulties = cmd.armies;
		if (cmd.defendersCasaulties > regionTo.armies) cmd.defendersCasaulties = regionTo.armies;
		
		cmd.defendersArmies = regionTo.armies;
		
		if (cmd.defendersCasaulties < regionTo.armies) {
			// defenders won
			cmd.attackersCasaulties = cmd.armies;
			
			regionFrom.armies -= cmd.attackersCasaulties;
			regionTo.armies -= cmd.defendersCasaulties;
			
			// update total armies
			attacker.totalArmies -= cmd.attackersCasaulties;
			defender.totalArmies -= cmd.defendersCasaulties;
		} else
		if (cmd.defendersCasaulties >= regionTo.armies && cmd.armies == cmd.attackersCasaulties) {
			// defenders are granted 1 army
			regionFrom.armies -= cmd.attackersCasaulties;
			regionTo.armies = 1;
			
			// update total armies
			attacker.totalArmies -= cmd.armies;
			defender.totalArmies -= cmd.defendersCasaulties;
			defender.totalArmies += 1; // extra one army
			
			cmd.defendersExtraOneArmy = 1; // mark that defenders have been granted one army
		} else {
			// attackers won
			cmd.defendersCasaulties = regionTo.armies;
			
			// update armies in regions
			regionFrom.armies -= cmd.armies;
			regionTo.armies = cmd.armies - cmd.attackersCasaulties;
			
			// update total armies
			attacker.totalArmies -= cmd.attackersCasaulties;
			defender.totalArmies -= cmd.defendersCasaulties;
			
			// change region ownership
			player(cmd.fromOwner).regions.put(cmd.to, regionTo);
			player(cmd.toOwner).regions.remove(cmd.to);
			regionTo.owner = player(cmd.fromOwner);
			
			// update continent state
			ContinentState continent = continent(regionTo.region.continent);
			continent.owned[attacker.player.id] += 1;
			continent.owned[defender.player.id] -= 1;			
			updateContinentOwnership(continent);
		}		
	}
	
	public void revert(AttackCommand cmd) {		
		if (cmd.movedInstead != null) {
			revert(cmd.movedInstead);
			return;
		}		
		
		if (cmd.armies < 1) return;
		
		RegionState regionFrom = region(cmd.from);
		RegionState regionTo   = region(cmd.to);

		PlayerState attacker = regionFrom.owner;
		PlayerState defender = player(cmd.toOwner);

		// REVERT ARMIES
		regionFrom.armies += cmd.armies;		
		regionTo.armies   -= cmd.armies - cmd.attackersCasaulties - cmd.defendersCasaulties + cmd.defendersExtraOneArmy;
		
		// REVERT PLAYER TOTAL ARMIES
		attacker.totalArmies += cmd.attackersCasaulties;
		defender.totalArmies += cmd.defendersCasaulties - cmd.defendersExtraOneArmy;
		
		// REVERT OWNER
		if (cmd.defendersCasaulties >= cmd.defendersArmies && cmd.defendersExtraOneArmy == 0) {
			// ATTACKERS WON		
			
			// REVERT REGION OWNERSHIP
			regionTo.owner = defender;			
			attacker.regions.remove(regionTo.region);
			defender.regions.put(regionTo.region, regionTo);
			
			// REVERT CONTINENT STATE
			ContinentState continent = continent(regionTo.region.continent);
			continent.owned[attacker.player.id] -= 1;
			continent.owned[defender.player.id] += 1;			
			updateContinentOwnership(continent);
		}		
	}
	
	/**
	 * ME becomes OPPONENT and vice versa, OPPONENT becomes ME.
	 */
	public void swapPlayers() {
		PlayerState newMe = opp;
		PlayerState newOpp = me;
		
		newMe.player  = Player.swapPlayer(newMe.player);
		newOpp.player = Player.swapPlayer(newOpp.player);
		
		this.me = newMe;
		this.opp = newOpp;
		
		players[Player.ME.id] = newMe;
		players[Player.OPPONENT.id] = newOpp;
		
		for (int i = 1; i < Continent.LAST_ID; ++i) {
			continents[i].swapPlayer();			
		}
		/* NO NEED TO DO THAT, region only contains reference to PlayerState, which we have already swapPlayer() for.
		for (int i = 1; i < Region.LAST_ID; ++i) {
			regions[i].swapPlayer();			
		}
		*/
	}
	
	@Override
	public String toString() {
		return "GameState[" + me + "|" + opp + "]";
	}
	
	// =====
	// UTILS
	// =====
	
	public static Player getRegionOwner(BotState botState, String playerName) {
		return botState.getMyPlayerName().equals(playerName) ? Player.ME : (botState.getOpponentPlayerName().equals(playerName) ? Player.OPPONENT : Player.NEUTRAL); 
	}
	
}
