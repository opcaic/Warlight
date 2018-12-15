package conquest.bot.state;

import conquest.game.Player;
import conquest.game.world.Region;

public class RegionState {
	
	/**
	 * What {@link Region} state this object describes. 
	 */
	public final Region region;
	
	/**
	 * Who owns this {@link #region}.
	 */
	public PlayerState owner;
	
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
		if (owner.player != other.owner.player) return false;
		if (armies != other.armies) return false;
		
		// neighbours should be the same...
	
		return true;
	}
	
			
	/**
	 * Is {@link #region} owned by 'player'?
	 * @param player
	 * @return
	 */
	public boolean owned(Player player) {
		return owner != null && owner.player == player;
	}
	
	/**
	 * Is {@link #region} owned by me ({@link Player#ME}) ?
	 * @return
	 */
	public boolean isMine() {
		return owned(Player.ME);
	}
	
	@Override
	public String toString() {
		return (region == null ? "RegionState" : region.name()) + "[" + (owner == null ? "null" : owner.player.name()) + "|" + armies + "]";
	}

	/*
	public void swapPlayer() {
		// NOTHING TO DO...
	}
	*/
	
}