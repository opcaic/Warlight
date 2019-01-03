package conquest.bot.state;

import conquest.game.world.Region;

public class RegionState {
	
	/**
	 * What {@link Region} state this object describes. 
	 */
	public final Region region;
	
	/**
	 * Who owns this {@link #region}.
	 */
	public int owner;
	
	/**
	 * How many armies are in this {@link #region}.
	 */
	public int armies;
	
	/**
	 * What neighbours this {@link #region} has.
	 */
	public RegionState[] neighbours;
	
	public RegionState(Region region) {
		this.region = region;
		armies = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof RegionState)) return false;
		RegionState other = (RegionState)obj;
		
		if (region != other.region) return false;
		if (owner != other.owner) return false;
		if (armies != other.armies) return false;
		
		// neighbours should be the same...
	
		return true;
	}
	
			
	/**
	 * Is {@link #region} owned by 'player'?
	 * @param player
	 * @return
	 */
	public boolean owned(int player) {
		return owner == player;
	}
	
	@Override
	public String toString() {
		return (region == null ? "RegionState" : region.name()) + "[" + owner + "|" + armies + "]";
	}
	
}