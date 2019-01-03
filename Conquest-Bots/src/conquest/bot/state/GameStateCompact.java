package conquest.bot.state;

import conquest.game.ConquestGame;
import conquest.game.world.Continent;
import conquest.game.world.Region;

/**
 * Compact game state suitable for game-tree search.
 * 
 * @author Jimmy
 */
public class GameStateCompact implements Cloneable {
    ConquestGame game;
	
    public GameStateCompact(ConquestGame game) {
        this.game = game;
    }
    
	/**
	 * Who owns the continent?
	 * @param continent
	 * @return
	 */
	public int ownedBy(Continent continent) {
		return game.getMap().getContinent(continent.id).owner();
	}
	
	/**
	 * Whether 'region' is owned by 'player'.
	 * @param region
	 * @param player
	 * @return
	 */
	public boolean owned(Region region, int player) {
		return ownedBy(region) == player;
	}
	
	/**
	 * Who owns the region?
	 * @param region
	 * @return
	 */
	public int ownedBy(Region region) {
		return game.getMap().getRegion(region.id).getOwner();
	}
	
	/**
	 * Whether 'continent' is owned by 'player'.
	 * @param continent
	 * @param player
	 * @return
	 */
	public boolean owned(Continent continent, int player) {
		return ownedBy(continent) == player;
	}
	
	/**
	 * How many armies are at 'region'?
	 * @param region
	 * @return
	 */
	public int armiesAt(Region region) {
		return game.getMap().getRegion(region.id).getArmies();
	}
	
	/**
	 * How many total armies does 'player' have?
	 * @param player
	 * @return
	 */
	public int totalArmies(int player) {
		int result = 0;
		for (Region region : Region.values()) {
			if (owned(region, player)) result += armiesAt(region);
		}
		return result;
	}
	
	/**
	 * How many armies does/will 'player' have to place in this/next turn given this state?
	 * @param player
	 * @return
	 */
	public int placeArmies(int player) {
		return game.armiesPerTurn(player);
	}
	
	public GameStateCompact clone() {
	    return new GameStateCompact(game);
	}
	
	public GameState toGameState() {
		return new GameState(this);
	}

	public static GameStateCompact fromGameState(GameState state) {
	    return new GameStateCompact(state.game);
	}
}
