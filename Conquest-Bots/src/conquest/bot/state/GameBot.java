package conquest.bot.state;

import java.util.List;

import conquest.bot.Bot;
import conquest.bot.BotState;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;
import conquest.view.GUI;

public abstract class GameBot implements Bot {
    protected BotState botState;
	
	protected GameState state;
	
	void updateState(BotState botState) {
		this.botState = botState;
		if (this.state == null) this.state = new GameState(botState);
		else this.state.update(botState);
	}
	
	@Override
	public final Region getStartingRegion(BotState state, Long timeOut) {
		updateState(state);
		
		ChooseCommand cmd = chooseRegion(state.getPickableStartingRegions(),
		                                 timeOut == null ? Long.MAX_VALUE : timeOut);

		return cmd.region;
	}
	
	public abstract ChooseCommand chooseRegion(List<Region> choosable, long timeout);	
	
	@Override
	public List<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
		updateState(state);
		
		return placeArmies(timeOut == null ? Long.MAX_VALUE : timeOut);
	}
	
	public abstract List<PlaceArmiesMove> placeArmies(long timeout);
	
	@Override
	public List<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
		updateState(state);
		
		return moveArmies(timeOut == null ? Long.MAX_VALUE : timeOut);
	}
	
	public abstract List<AttackTransferMove> moveArmies(long timeout);

	@Override
	public void setGUI(GUI gui) {
	}
}
