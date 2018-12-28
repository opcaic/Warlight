package conquest.bot.state;

import java.util.HashMap;
import java.util.Map;

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
	public int owner;
	
	/**
	 * All {@link Region} states of this {@link #continent}.
	 */
	public Map<Region, RegionState> regions;
	
	/**
	 * How many regions each player controls within this continent.
	 */
	public int[] owned;
	
	public ContinentState(Continent continent) {
		this.continent = continent;
		owner = 0;
		owned = new int[3];
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
		
		for (int p = 0 ; p <= 2 ; ++p) {
			if (owned[p] != other.owned[p]) return false;
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
	public boolean ownedBy(int player) {
		return owner == player;
	}
	
	/**
	 * Returns how many {@link Region}s given 'player' controls in this {@link #continent}.
	 * @param player
	 * @return
	 */
	public int regionsOwnedBy(int player) {
		return owned[player];
	}
	
	@Override
	public String toString() {
		return continent.name() + "[" + owner + 
				   "|P1=" + owned[1] + "|P2=" + owned[2] + "|NEU=" + owned[0] + "]";
	}
	
}