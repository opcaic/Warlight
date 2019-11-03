package conquest.game.move;

import java.util.List;

import conquest.game.GameState;

public class PlaceMoveAction implements Action {
	public List<PlaceArmiesMove> placeCommands;
	public List<AttackTransferMove> moveCommands;
	
	public PlaceMoveAction(List<PlaceArmiesMove> placeCommands, List<AttackTransferMove> moveCommands) {
		this.placeCommands = placeCommands;
		this.moveCommands = moveCommands;
	}

	public void apply(GameState state) {
		state.placeArmies(placeCommands, null);
		state.attackTransfer(moveCommands, null);
	}
}
