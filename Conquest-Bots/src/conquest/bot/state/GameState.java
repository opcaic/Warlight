package conquest.bot.state;

import java.util.*;

import conquest.bot.BotState;
import conquest.game.*;
import conquest.game.move.*;
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
	
	GameState(ConquestGame game) {
		this.game = game;
		reset();
		update();
	}
	
	public GameState() {
		this(new ConquestGame());
	}
	
	GameState(BotState state) {
		this(state.toConquestGame());
	}
	
	public GameState(GameStateCompact state) {
		this(state.game);
	}
	
	@Override
	public GameState clone() {
		return new GameState(game.clone());
	}
	
	/**
	 * Recreates all STATE objects.
	 */
	private void reset() {
		regions = new RegionState[Region.LAST_ID + 1];
		for (int i = 1; i <= Region.LAST_ID; ++i) {
			Region region = Region.forId(i);
			regions[i] = new RegionState(region);
			regions[i].neighbours = new RegionState[region.getNeighbours().size()];
		}

		// fill in neighbours
		for (int i = 1; i <= Region.LAST_ID; ++i) {
			List<Region> neighbours = Region.forId(i).getNeighbours();
			for (int j = 0; j < neighbours.size(); ++j) {				
				regions[i].neighbours[j] = region(neighbours.get(j));
			}			
		}
		
		continents = new ContinentState[Continent.LAST_ID + 1];
		for (int i = 1; i <= Continent.LAST_ID; ++i) {
			Continent c = Continent.forId(i);
			continents[i] = new ContinentState(c);
			
			for (Region r : c.getRegions())
				continents[i].regions.put(r, regions[r.id]);
		}
		
		players = new PlayerState[3];
		for (int i = 0; i <= 2; ++i) {
			players[i] = new PlayerState(i);
		}
	}
	
	// Update all objects based on the underlying ConquestGame.
	void update() {
		GameMap map = game.getMap();
		
		for (int p = 0 ; p <= 2 ; ++p) {
			PlayerState ps = players[p];
			
			Iterator<Region> regions = ps.regions.keySet().iterator();
			while (regions.hasNext()) {
				Region r = regions.next();
				if (map.getRegion(r.id).getOwner() != p)
					regions.remove();  // no longer owned by this player
			}
			
			Iterator<Continent> continents = ps.continents.keySet().iterator();
			while (continents.hasNext()) {
				Continent c = continents.next();
				if (map.getContinent(c.id).owner() != p)
					continents.remove();  // no longer owned by this player
			}
			
			ps.totalArmies = 0;
			ps.placeArmies = game.armiesPerTurn(p);
		}
		
		for (int i = 1 ; i <= Region.LAST_ID ; ++i) {
			Region r = Region.forId(i);
			RegionData rd = map.getRegion(i);
			int owner = rd.getOwner();
			RegionState rs = regions[i];
			rs.owner = owner;
			rs.armies = rd.getArmies();
			
			if (!players[owner].regions.containsKey(r))
				players[owner].regions.put(r, rs);
			
			players[owner].totalArmies += rd.getArmies();
		}
		
		for (int i = 1 ; i <= Continent.LAST_ID ; ++i) {
			Continent c = Continent.forId(i);
			ContinentData cd = map.getContinent(i);
			int owner = cd.owner();
			ContinentState cs = continents[i];
			cs.owner = owner;
			
			for (int j = 0 ; j < 3 ; ++j)
				cs.owned[j] = 0;
			for (RegionData rd : cd.getRegions())
				cs.owned[rd.getOwner()] += 1;
			
			if (!players[owner].continents.containsKey(c))
				players[owner].continents.put(c, cs);
		}
		
		me = game.getTurn();
		opp = 3 - me;
	}
	
	/**
	 * Update this {@link GameState} according to the information from given {@link BotState}.
	 * @param state
	 */
	public void update(BotState state) {
	    game = state.toConquestGame();
	    update();
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
	
	public int getRoundNumber() {
		return game.getRoundNumber();
	}
	
	public Phase getPhase() {
		return game.getPhase();
	}
	
	public boolean isDone() {
		return game.isDone();
	}
	
	public int winningPlayer() {
		return game.winningPlayer();
	}
	
    public List<Region> getPickableRegions() {
    	return game.getPickableRegions();
    }
	
	public void chooseRegion(ChooseCommand command) {
		game.chooseRegion(command.region);
		update();
	}
	
	public void placeArmies(List<PlaceCommand> commands) {
		List<PlaceArmiesMove> placeMoves = new ArrayList<PlaceArmiesMove>();
		for (PlaceCommand pc : commands)
			placeMoves.add(new PlaceArmiesMove(pc.region, pc.armies));
		game.placeArmies(placeMoves, null);
		
		update();
	}
	
	public void moveArmies(List<MoveCommand> commands) {
		List<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		for (MoveCommand mc : commands)
			attackTransferMoves.add(new AttackTransferMove(mc.from, mc.to, mc.armies));
		game.attackTransfer(attackTransferMoves, null);

		update();
	}
	
	@Override
	public String toString() {
		return "GameState[" + me + "|" + opp + "]";
	}
}
