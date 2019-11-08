package conquest.game.move;

import java.util.List;

import conquest.game.GameState;

public class PlaceAction implements Action {
	public List<PlaceArmiesMove> commands;
	
	public PlaceAction(List<PlaceArmiesMove> commands) { this.commands = commands; }
	
	public void apply(GameState state) {
		state.placeArmies(commands);
	}
}
