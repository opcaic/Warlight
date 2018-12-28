package conquest.bot.state;

import java.util.HashSet;
import java.util.Set;

import conquest.bot.BotState;
import conquest.game.*;
import conquest.game.world.Continent;
import conquest.game.world.Region;

public class GameState implements Cloneable {
    ConquestGame game;
	
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
	 * Player state for each player (0 = neutral, 1 = player 1, 2 = player 2)
	 * 
	 * @return
	 */
	public PlayerState[] players;
	
	/**
	 * The player whose turn it is to move.
	 */
	public int me;
	
	/**
	 * The other player.
	 */
	public int opp;
	
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
		players = new PlayerState[3];
		for (int i = 0; i <= 2; ++i) {
			players[i] = new PlayerState(i);
		}
	}
	
	ConquestGame fromBotState(BotState state) {
	    return new ConquestGame(
	        new GameConfig(), state.getMap(), null,
	        state.getRoundNumber(), state.getMyPlayerNumber(), state.getPickableStartingRegions());
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
			regionState.owner = players[gameStateCompact.ownedBy(region)];
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned[regionState.owner.player] += 1;
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			boolean isNeutral = true;
			for (int player = 0 ; player <= 2 ; ++player) {
				if (continentState.owned[player] == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					isNeutral = player == 0;
					break;
				}
			}
			if (isNeutral) {
				continentState.owner = 0;
				player(0).continents.put(continent, continentState);
			}
		}
	}

	/**
	 * Fully (re)creates the state out of the {@link BotState}, throwing out all existing objects it has.
	 * @param state
	 */
	private void reset(BotState state) {
		reset();
		
		game = fromBotState(state);

		// FILL IN STATES
		for (RegionData rd : state.getMap().regions) {
			// REGION
			RegionState regionState = region(rd.getRegion());
			regionState.armies = rd.getArmies();
			int owner = rd.getOwner();
			regionState.owner = player(owner);
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned[regionState.owner.player] += 1;
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			boolean isNeutral = true;
			for (int player = 0 ; player <= 2 ; ++player) {
				if (continentState.owned[player] == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					isNeutral = player == 0;
					break;
				}
			}
			if (isNeutral) {
				continentState.owner = 0;
				player(0).continents.put(continent, continentState);
			}
		}
		
		for (int p = 1 ; p <= 2 ; ++p)
		    players[p].placeArmies = game.armiesPerTurn(p);
		
        me  = state.getMyPlayerNumber();
        opp = 3 - me;
	}
	
	/**
	 * Fully (re)creates the state out of the {@link GameState}, throwing out all existing objects it has.
	 * @param gameState
	 */
	private void reset(GameState gameState) {
		reset();
		
		game = gameState.game.clone();

		// FILL IN STATES
		for (Region region : Region.values()) {
			// REGION
			RegionState regionState = region(region);
			regionState.armies = gameState.region(region).armies;
			regionState.owner = player(gameState.region(region).owner.player);
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned[regionState.owner.player] += 1;
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			boolean isNeutral = true;
			for (int player = 0 ; player <= 2 ; ++player) {
				if (continentState.owned[player] == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					isNeutral = player == 0;
					break;
				}
			}
			if (isNeutral) {
				continentState.owner = 0;
				player(0).continents.put(continent, continentState);
			}
		}
	}
	
	/**
	 * Update this {@link GameState} according to the information from given {@link BotState}.
	 * @param state
	 */
	public void update(BotState state) {
	    game = fromBotState(state);
	    
		// RESET TOTAL ARMIES & PLACE ARMIES
		for (int i = 1; i < players.length; ++i) {
			PlayerState player = players[i];
			player.totalArmies = 0;
		}
		
		Set<Continent> regionOwnershipChanged = new HashSet<Continent>();
		
		// UPDATE REGION STATES
		for (RegionData rd : state.getMap().regions) {
			RegionState regionState = region(rd.getRegion());
			ContinentState continentState = continent(regionState.region);
			PlayerState oldOwnerState = regionState.owner;
			
			regionState.armies = rd.getArmies();
			
			int newOwner = rd.getOwner();
			PlayerState newOwnerState = player(newOwner);
			
			if (newOwner != regionState.owner.player) {
				// OWNER CHANGED
				regionOwnershipChanged.add(regionState.region.continent);
				
				continentState.owned[regionState.owner.player] -= 1;
				continentState.owned[newOwner] += 1;
				
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

		for (int p = 1 ; p <= 2 ; ++p)
            players[p].placeArmies = game.armiesPerTurn(p);
	}
	
	protected void updateContinentOwnership(Continent continent) {
		updateContinentOwnership(continent(continent));
	}
	
	protected void updateContinentOwnership(ContinentState continentState) {
		int newOwner = 0;
		
		if (continentState.owned[1] == continentState.regions.size()) {
			newOwner = 1;
		} else
		if (continentState.owned[2] == continentState.regions.size()) {
			newOwner = 2;
		}
		
		PlayerState newOwnerState = player(newOwner);
		
		if (continentState.owner != newOwner) {
			// CONTINENT OWNER HAS CHANGED!
			PlayerState oldOwnerState = player(continentState.owner);
			
			oldOwnerState.continents.remove(continentState.continent);
			newOwnerState.continents.put(continentState.continent, continentState);
			
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
	
	public PlayerState player(int player) {
		return players[player];
	}
	
	@Override
	public String toString() {
		return "GameState[" + me + "|" + opp + "]";
	}	
}
