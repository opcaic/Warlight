package conquest.bot.state;

import java.util.ArrayList;
import java.util.List;

import conquest.bot.Bot;
import conquest.bot.BotState;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;

public abstract class GameBot implements Bot {

    protected BotState botState;
	
	protected GameState state;
	
	/**
	 * Overrides bot's internal game {@link #state}.
	 * @param gameState
	 */
	public void enforceState(GameState gameState) {
		this.state = gameState;
	}
	
	public void updateState(BotState botState) {
		this.botState = botState;
		if (this.state == null) this.state = new GameState(botState);
		else this.state.update(botState);
	}
	
	@Override
	public final ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut) {
		List<Region> pickableRegions = new ArrayList<Region>();
		for (Region pickable : state.getPickableStartingRegions()) {
			if (pickable == null) continue;
			pickableRegions.add(pickable);
		}
		
		List<ChooseCommand> cmds = chooseRegions(pickableRegions, timeOut == null ? Long.MAX_VALUE : timeOut);
		
		ArrayList<Region> result = new ArrayList<Region>(cmds.size());
		for (ChooseCommand cmd : cmds) {
			if (cmd == null) continue;
			for (Region pickable : state.getPickableStartingRegions()) {
				if (pickable.id == cmd.region.id) {
					result.add(pickable);
				}
			}
		}
		
		return result;
	}
	
	public abstract List<ChooseCommand> chooseRegions(List<Region> choosable, long timeout);	
	
	@Override
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
		updateState(state);
		
		List<PlaceCommand> cmds = placeArmies(timeOut == null ? Long.MAX_VALUE : timeOut);
		
		ArrayList<PlaceArmiesMove> result = new ArrayList<PlaceArmiesMove>(cmds.size());
		for (PlaceCommand cmd : cmds) {
			if (cmd == null) continue;
			result.add(new PlaceArmiesMove(botState.getMyPlayerName(), cmd.region, cmd.armies));
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
			result.add(new AttackTransferMove(botState.getMyPlayerName(), cmd.from, cmd.to, cmd.armies));
		}
		
		return result;
	}
	
	public abstract List<MoveCommand> moveArmies(long timeout);

}
