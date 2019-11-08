package conquest.bot.state;

import java.util.ArrayList;
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
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
		updateState(state);
		
		List<PlaceCommand> cmds = placeArmies(timeOut == null ? Long.MAX_VALUE : timeOut);
		
		ArrayList<PlaceArmiesMove> result = new ArrayList<PlaceArmiesMove>(cmds.size());
		for (PlaceCommand cmd : cmds) {
			if (cmd == null) continue;
			result.add(new PlaceArmiesMove(cmd.region, cmd.armies));
		}
		
		return result;
	}
	
	public abstract List<PlaceCommand> placeArmies(long timeout);
	
	@Override
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
		updateState(state);
		
		List<MoveCommand> cmds = moveArmies(timeOut == null ? Long.MAX_VALUE : timeOut);
		
		ArrayList<AttackTransferMove> result = new ArrayList<AttackTransferMove>(cmds.size());
		for (MoveCommand cmd : cmds) {
			if (cmd == null) continue;
			result.add(new AttackTransferMove(cmd.from, cmd.to, cmd.armies));
		}
		
		return result;
	}
	
	public abstract List<MoveCommand> moveArmies(long timeout);

	@Override
	public void setGUI(GUI gui) {
	}
}
