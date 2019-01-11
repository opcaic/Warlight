package conquest.bot.state;

import java.util.List;

public class PlaceMoveAction implements Action {
	public List<PlaceCommand> placeCommands;
	public List<MoveCommand> moveCommands;
	
	public PlaceMoveAction(List<PlaceCommand> placeCommands, List<MoveCommand> moveCommands) {
		this.placeCommands = placeCommands;
		this.moveCommands = moveCommands;
	}

	public void apply(GameState state) {
		state.placeArmies(placeCommands);
		state.moveArmies(moveCommands);
	}
}