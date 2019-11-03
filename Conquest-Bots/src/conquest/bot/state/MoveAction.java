package conquest.bot.state;

import java.util.List;

import conquest.game.move.AttackTransferMove;

public class MoveAction implements Action {
	public List<AttackTransferMove> commands;
	
	public MoveAction(List<AttackTransferMove> commands) { this.commands = commands; }
	
	public void apply(GameState state) {
		state.moveArmies(commands);
	}
}
