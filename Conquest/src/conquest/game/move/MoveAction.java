package conquest.game.move;

import java.util.List;

import conquest.game.GameState;

public class MoveAction implements Action {
	public List<AttackTransferMove> commands;
	
	public MoveAction(List<AttackTransferMove> commands) { this.commands = commands; }
	
	public void apply(GameState state) {
		state.attackTransfer(commands);
	}
}
