package conquest.bot.state;

import conquest.game.RegionData;
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
	
	public MoveCommand(RegionData from, Region to, int armies) {
		this.from = from.getRegion();
		this.to = to;
		this.armies = armies;
	}
	
	public MoveCommand(Region from, RegionData to, int armies) {
		this.from = from;
		this.to = to.getRegion();
		this.armies = armies;
	}
	
	public MoveCommand(RegionData from, RegionData to, int armies) {
		this.from = from.getRegion();
		this.to = to.getRegion();
		this.armies = armies;
	}
	
	@Override
	public String toString() {
		return "MoveCommand[from=" + from + ",to=" + to +",armies=" + armies + "]";
	}
}
