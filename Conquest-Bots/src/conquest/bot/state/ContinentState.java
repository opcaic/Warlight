package conquest.bot.state;

import java.util.HashMap;
import java.util.Map;

import conquest.game.Player;
import conquest.game.world.Continent;
import conquest.game.world.Region;

public class ContinentState {
	/**
	 * What {@link Continent} state this object describes.
	 */
	public final Continent continent;
	
	/**
	 * Who owns this {@link #continent}.
	 */
	public Player owner;
	
	/**
	 * All {@link Region} states of this {@link #continent}.
	 */
	public Map<Region, RegionState> regions;
	
	/**
	 * How many regions particular {@link Player} controls within this continent.
	 * 
	 * Indexed by {@link Player#id}.
	 * 
	 * 1-based! [0] is 0 and does not have any meaning!
	 */
	public int[] owned;
	
	public ContinentState(Continent continent) {
		this.continent = continent;
		owner = Player.NEUTRAL;
		owned = new int[4];
		for (int i = 0; i < 4; ++i) owned[i] = 0;
		regions = new HashMap<Region, RegionState>();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof ContinentState)) return false;
		ContinentState other = (ContinentState)obj;
		
		if (continent != other.continent) return false;
		if (owner != other.owner) return false;
		
		for (Player player : Player.values()) {
			if (owned[player.id] != other.owned[player.id]) return false;
		}
		if (other.regions == null || regions.size() != other.regions.size()) return false;
		for (Region region : regions.keySet()) {
			if (!other.regions.containsKey(region)) return false;
			if (!regions.get(region).equals(other.regions.get(region))) return false;
		}
	
		return true;
	}
			
	/**
	 * Returns {@link RegionState} of the {@link #continent}'s {@link Region}.
	 * @param region
	 * @return
	 */
	public RegionState region(Region region) {
		return regions.get(region);
	}
	
	/**
	 * Is this {@link #continent} owned by 'player'?
	 * @param player
	 * @return
	 */
	public boolean ownedBy(Player player) {
		return owner == player;
	}
	
	/**
	 * Returns how many {@link Region}s given 'player' controls in this {@link #continent}.
	 * @param player
	 * @return
	 */
	public int regionsOwnedBy(Player player) {
		return owned[player.id];
	}
	
	@Override
	public String toString() {
		return (continent == null ? "ContinentState" : continent.name())
				  + "[" + (owner == null ? "null" : owner.name()) 
				  + "|ME=" + (owned == null ? "N/A" : owned[Player.ME.id])
				  + "|OPP=" + (owned == null ? "N/A" : owned[Player.OPPONENT.id]) 
				  + "|NEU=" + (owned == null ? "N/A" : owned[Player.NEUTRAL.id])
				  + "]";
	}

	/**
	 * ME becomes OPPONENT and vice versa, OPPONENT becomes ME.
	 */
	protected void swapPlayer() {
		owner = Player.swapPlayer(owner);
		
		int newOppOwned = owned[Player.ME.id];
		int newMeOwned  = owned[Player.OPPONENT.id];
		
		owned[Player.OPPONENT.id] = newOppOwned;
		owned[Player.ME.id] = newMeOwned;
	}
	
}