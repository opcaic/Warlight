package conquest.bot.state;

import conquest.game.Player;
import conquest.game.world.Region;

public class AttackCommand implements ICommand, Cloneable {

	public Region from;
	public Player fromOwner;
	
	public Region to;
	public Player toOwner;
	
	public int armies;
	
	/**
	 * Injected by {@link GameState#apply(AttackCommand)}.
	 * Interpreted by {@link GameState#revert(AttackCommand)}.
	 */
	protected int defendersArmies;
	
	/**
	 * Injected by {@link GameState#apply(AttackCommand)}.
	 * Interpreted by {@link GameState#revert(AttackCommand)}.
	 */
	protected int defendersExtraOneArmy = 0;
	
	public int attackersCasaulties;
	public int defendersCasaulties;
	
	/**
	 * Injected by {@link GameState#apply(AttackCommand)}.
	 * Interpreted by {@link GameState#revert(AttackCommand)}.
	 */
	protected MoveCommand movedInstead = null;
	
	public AttackCommand(Region from, Player fromOwner, Region to, Player toOwner, int armies) {
		this(from, fromOwner, to, toOwner, armies, -1, -1);
	}
	
	public AttackCommand(RegionState from, RegionState to, int armies) {
		this(from.region, from.owner.player, to.region, to.owner.player, armies, -1, -1);
	}
	
	public AttackCommand(RegionState from, RegionState to, int armies, int attackersCasaulties, int defendersCasaulties) {
		this(from.region, from.owner.player, to.region, to.owner.player, armies, attackersCasaulties, defendersCasaulties);
	}
	
	public AttackCommand(GameState state, MoveCommand move) {
		this(move.from, state.region(move.from).owner.player, move.to, state.region(move.to).owner.player, move.armies);
	}
	
	public AttackCommand(Region from, Player fromOwner, Region to, Player toOwner, int armies, int attackersCasaulties, int defendersCasaulties) {
		this.from = from;
		this.fromOwner = fromOwner;
		
		this.to = to;
		this.toOwner = toOwner;
		this.armies = armies;
		
		this.attackersCasaulties = attackersCasaulties;
		this.defendersCasaulties = defendersCasaulties;
	}
	
	/**
	 * Be sure to have {@link #attackersCasaulties} and {@link #defendersCasaulties} defined before applying!
	 * @param state
	 */
	@Override
	public void apply(GameState state) {
		state.apply(this);		
	}
	
	/**
	 * Be sure to have {@link #attackersCasaulties} and {@link #defendersCasaulties} defined before reverting!
	 * @param state
	 */
	@Override
	public void revert(GameState state) {
		state.revert(this);
	}
	
	@Override
	public String toString() {
		return "AttackCommand[from=" + from + ",to=" + to +",armies=" + armies + ",attackersCasaulties=" + attackersCasaulties + ",defendersCasaulties=" + defendersCasaulties + ",defendersArmies=" + defendersArmies + (defendersExtraOneArmy > 0 ? ",defendersExtraOneArmy=" + defendersExtraOneArmy : "") + "]";
	}
		
}