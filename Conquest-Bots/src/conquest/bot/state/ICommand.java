package conquest.bot.state;

public interface ICommand {

	/**
	 * Apply the action to a {@link GameState}.
	 * @param state
	 */
	public void apply(GameState state);
	
	/**
	 * Revert this action in {@link GameState}.
	 * @param state
	 */
	public void revert(GameState state);
	
}
