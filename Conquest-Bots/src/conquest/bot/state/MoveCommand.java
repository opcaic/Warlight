package conquest.bot.state;

import conquest.game.world.Region;

public class MoveCommand {

	public Region from;
	public Region to;
	public int armies;

	public MoveCommand(Region from, Region to, int armies) {
		this.from = from;
		this.to = to;
		this.armies = armies;
	}
	
	public MoveCommand(RegionState from, Region to, int armies) {
		this.from = from.region;
		this.to = to;
		this.armies = armies;
	}
	
	public MoveCommand(Region from, RegionState to, int armies) {
		this.from = from;
		this.to = to.region;
		this.armies = armies;
	}
	
	public MoveCommand(RegionState from, RegionState to, int armies) {
		this.from = from.region;
		this.to = to.region;
		this.armies = armies;
	}
	
	@Override
	public String toString() {
		return "MoveCommand[from=" + from + ",to=" + to +",armies=" + armies + "]";
	}
}
