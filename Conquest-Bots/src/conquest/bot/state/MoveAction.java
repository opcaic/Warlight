package conquest.bot.state;

import java.util.List;

public class MoveAction implements Action {
	public List<MoveCommand> commands;
	
	public MoveAction(List<MoveCommand> commands) { this.commands = commands; }
	
	public void apply(GameState state) {
		state.moveArmies(commands);
	}
}
