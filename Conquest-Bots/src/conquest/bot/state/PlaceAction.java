package conquest.bot.state;

import java.util.List;

import conquest.game.move.PlaceArmiesMove;

public class PlaceAction implements Action {
	public List<PlaceArmiesMove> commands;
	
	public PlaceAction(List<PlaceArmiesMove> commands) { this.commands = commands; }
	
	public void apply(GameState state) {
		state.placeArmies(commands);
	}
}
