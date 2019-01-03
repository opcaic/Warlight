package conquest.bot.state;

import java.util.List;

public class PlaceAction implements Action {
	public List<PlaceCommand> commands;
	
	public PlaceAction(List<PlaceCommand> commands) { this.commands = commands; }
	
	public void apply(GameState state) {
		state.placeArmies(commands);
	}
}