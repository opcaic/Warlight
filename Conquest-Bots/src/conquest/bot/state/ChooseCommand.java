package conquest.bot.state;

import conquest.game.world.Region;

public class ChooseCommand implements Action {
	public Region region;
	
	public ChooseCommand(Region region) {
		this.region = region;
	}
	
	public void apply(GameState state) {
		state.chooseRegion(this);
	}
}