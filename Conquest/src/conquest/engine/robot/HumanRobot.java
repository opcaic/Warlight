package conquest.engine.robot;

import java.util.ArrayList;
import java.util.List;

import conquest.engine.Robot;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;

public class HumanRobot implements Robot {
	private RobotConfig config;
	private boolean running = true;;
	
	@Override
	public void setup(RobotConfig config) {
		this.config = config;
	}

	@Override
	public String getStartingRegion(long timeOut, ArrayList<Region> pickableRegions) {
		if (config.gameLog != null) {
			System.out.println(config.player + "-Human --> getPreferredStartingArmies()");
		}
		
		Region chosen = config.gui.chooseRegionHuman();
		
		String result = chosen.id + "";
		
		if (config.gameLog != null) {
			System.out.println(config.player + "-Human <-- " + result);
		}
		
		return result;
	}

	@Override
	public String getPlaceArmiesMoves(long timeOut) {
		if (config.gameLog != null) {
			System.out.println(config.player + "-Human --> getPlaceArmiesMoves()");
		}
		
		List<PlaceArmiesMove> commands = config.gui.placeArmiesHuman(config.team);
		
		String result = "";
		
		for (PlaceArmiesMove command : commands) {
			result += command.getString() + ",";
		}
		
		if (config.gameLog != null) {
			System.out.println(config.player + "-Human <-- " + result);
		}
		
		return result;
	}

	@Override
	public String getAttackTransferMoves(long timeOut) {
		if (config.gameLog != null) {
			System.out.println(config.player + "-Human --> getAttackTransferMoves()");
		}
		
		List<AttackTransferMove> commands = config.gui.moveArmiesHuman(config.team);
		
		String result = "";
		
		for (AttackTransferMove command : commands) {
			result += command.getString() + ",";
		}
		
		if (config.gameLog != null) {
			System.out.println(config.player + "-Human <-- " + result);
		}
		
		return result;
	}

	@Override
	public void writeInfo(String info) {
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void finish() {
		running = false;
	}

	@Override
	public int getRobotPlayer() {
		if (config == null) return 0;
		return config.player;
	}
	
	public String getRobotPlayerName() {
		return "Human";
	}

}
