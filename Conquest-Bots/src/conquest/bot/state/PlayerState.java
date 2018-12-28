package conquest.bot.state;

import java.util.HashMap;
import java.util.Map;

import conquest.game.world.Continent;
import conquest.game.world.Region;

public class PlayerState {
	
	/**
	 * What player this object describes.
	 */
	public int player;
	
	/**
	 * What {@link Region} {@link #player} owns.
	 */
	public Map<Region, RegionState> regions;
	
	/**
	 * What {@link Continent} {@link #player} owns.
	 */
	public Map<Continent, ContinentState> continents;
	
	/**
	 * How many armies this {@link #player} has in total in all controlled regions.
	 */
	public int totalArmies;
	
	/**
	 * How many armies this player will be placing next round.
	 */
	public int placeArmies;
	
	public PlayerState(int player) {
		this.player = player;
		regions = new HashMap<Region, RegionState>();
		continents = new HashMap<Continent, ContinentState>();
		totalArmies = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof PlayerState)) return false;
		PlayerState other = (PlayerState)obj;
		
		if (player != other.player) return false;
		
		if (other.regions == null || regions.size() != other.regions.size()) return false;
		for (Region region : regions.keySet()) {
			if (!regions.get(region).equals(other.regions.get(region))) return false;
		}
		
		if (other.continents == null || continents.size() != other.continents.size()) return false;
		for (Continent continent : continents.keySet()) {
			if (!continents.get(continent).equals(other.continents.get(continent))) return false;
		}
	
		return true;
	}
	
	/**
	 * Does this {@link #player} own 'region'? 
	 * @param region
	 * @return
	 */
	public boolean ownsRegion(Region region) {
		return regions.containsKey(region);
	}
	
	/**
	 * Returns {@link RegionState} of the 'region' controlled by this {@link #player}.
	 * @param region
	 * @return
	 */
	public RegionState region(Region region) {
		return regions.get(region);
	}
	
	/**
	 * Does this {@link #player} own 'continent'? 
	 * @param region
	 * @return
	 */
	public boolean ownsContinent(Continent continent) {
		return continents.containsKey(continent);
	}
	
	/**
	 * Returns {@link ContinentState} of the 'continent' controlled by this {@link #player}.
	 * @param region
	 * @return
	 */
	public ContinentState continent(Continent continent) {
		return continents.get(continent);
	}
	
	@Override
	public String toString() {
		return player + " " +
		    "[#continents=" + (continents == null ? "null" : continents.size()) +
		    "|#regions=" + (regions == null ? "null" : regions.size()) +
		    "|totalArmies=" + totalArmies + "|placeArmies=" + placeArmies + "]";
	}
}