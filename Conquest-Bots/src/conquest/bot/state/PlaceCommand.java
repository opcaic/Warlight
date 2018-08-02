package conquest.bot.state;

import conquest.game.world.Region;

public class PlaceCommand implements ICommand {

	public Region region;
	public int armies;

	public PlaceCommand(Region region, int armies) {
		this.region = region;
		this.armies = armies;
	}
		
	@Override
	public void apply(GameState state) {
		state.apply(this);
	}
	
	@Override
	public void revert(GameState state) {
		state.revert(this);
	}
	
}

