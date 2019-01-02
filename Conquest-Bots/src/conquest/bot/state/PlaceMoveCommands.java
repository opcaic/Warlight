package conquest.bot.state;

import java.util.List;

public class PlaceMoveCommands implements CommandSet {
	public List<PlaceCommand> placeCommands;
	
	public List<MoveCommand> moveCommands;
	
	public PlaceMoveCommands(List<PlaceCommand> placeCommands, List<MoveCommand> moveCommands) {
		this.placeCommands = placeCommands;
		this.moveCommands = moveCommands;
	}
	
	public void apply(GameState state) {
		state.apply(this);
	}
}
