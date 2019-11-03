package conquest.bot.state;

import java.util.*;

import conquest.bot.BotState;
import conquest.game.*;
import conquest.game.move.*;
import conquest.game.world.Continent;
import conquest.game.world.Region;

public class GameState implements Cloneable {
    ConquestGame game;
	
	GameState(ConquestGame game) {
		this.game = game;
	}
	
	public GameState() {
		this(new ConquestGame());
	}
	
	GameState(BotState state) {
		this(state.toConquestGame());
	}
	
	public GameState(GameStateCompact state) {
		this(state.game);
	}
	
	@Override
	public GameState clone() {
		return new GameState(game.clone());
	}
	
	/**
	 * Update this {@link GameState} according to the information from given {@link BotState}.
	 * @param state
	 */
	public void update(BotState state) {
	    game = state.toConquestGame();
	}
	
	public RegionData region(Region region) {
		return game.getMap().getRegion(region.id);
	}
	
	public ContinentData continent(Continent continent) {
		return game.getMap().getContinent(continent.id);
	}
	
	public ContinentData continent(Region region) {
		return continent(region.continent);
	}
	
	public int getRoundNumber() {
		return game.getRoundNumber();
	}

	public int me() {
		return game.getTurn();
	}

	public int opp() {
		return 3 - me();
	}
	
	public Phase getPhase() {
		return game.getPhase();
	}
	
	public boolean isDone() {
		return game.isDone();
	}
	
	public int winningPlayer() {
		return game.winningPlayer();
	}

	public ArrayList<RegionData> regionsOwnedBy(int player) {
		return game.getMap().ownedRegionsByPlayer(player);
	}

	public int armiesPerTurn(int player) {
		return game.armiesPerTurn(player);
	}
	
	public List<Region> getPickableRegions() {
		return game.getPickableRegions();
	}

	public void chooseRegion(ChooseCommand command) {
		game.chooseRegion(command.region);
	}
	
	public void placeArmies(List<PlaceArmiesMove> commands) {
		game.placeArmies(commands, null);
	}
	
	public void moveArmies(List<AttackTransferMove> commands) {
		game.attackTransfer(commands, null);
	}
	
	@Override
	public String toString() {
		return "GameState[" + me() + "|" + opp() + "]";
	}
}
