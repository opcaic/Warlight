package conquest.engine.robot;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import conquest.bot.BotParser;
import conquest.engine.Robot;
import conquest.engine.io.InputOutputStream;
import conquest.game.Team;
import conquest.game.world.Region;

public class InternalRobot implements Robot {
	
	private class MyKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}
		
		@Override
		public void keyReleased(KeyEvent e) {}
		
		@Override
		public void keyPressed(KeyEvent e) {
			if (InternalRobot.this.config.team == Team.PLAYER_1) {
				if (Character.toLowerCase(e.getKeyChar()) == 'h') {
					hijacked = !hijacked;
					if (config.gui != null) {
						config.gui.showNotification(
							hijacked ? InternalRobot.this.config.player + " hijacked!" : InternalRobot.this.config.player + " resumed!"
						);
					}
				}
			}
			if (InternalRobot.this.config.team == Team.PLAYER_2) {
				if (Character.toLowerCase(e.getKeyChar()) == 'j') {
					hijacked = !hijacked;
					if (config.gui != null) {
						config.gui.showNotification(
							hijacked ? InternalRobot.this.config.player + " hijacked!" : InternalRobot.this.config.player + " resumed!"
						);
					}
				}
			}
		}
	}
	
	private Object mutex = new Object();
	
	private InputOutputStream botInput;
	private InputOutputStream botOutput;
	
	private BotParser bot;

	private IORobot robot;

	private RobotConfig config;
	
	private boolean hijacked = false;
	
	private HumanRobot humanHijack;

	private MyKeyListener myKeyListener;

	private String botFQCN;
	
	public InternalRobot(int player, String botFQCN) throws IOException {
		this.botFQCN = botFQCN;
		
		botInput = new InputOutputStream();
		botOutput = new InputOutputStream();
		
		bot = BotParser.runInternal(botFQCN, botInput.getInputStream(), new PrintStream(botOutput.getOutputStream()));
		System.out.println(player + " -> " + botFQCN);
		
		robot = new IORobot(player, botInput.getOutputStream(), true, botOutput.getInputStream(), null);
		
		humanHijack = new HumanRobot();
	}

	public InternalRobot(int player, String botFQCN, String jarPath) throws IOException {
		this.botFQCN = botFQCN;

		botInput = new InputOutputStream();
		botOutput = new InputOutputStream();

		bot = BotParser.runInternal(botFQCN, jarPath, botInput.getInputStream(), new PrintStream(botOutput.getOutputStream()));
		System.out.println(player + " -> " + botFQCN);

		robot = new IORobot(player, botInput.getOutputStream(), true, botOutput.getInputStream(), null);

		humanHijack = new HumanRobot();
	}

	@Override
	public void setup(RobotConfig config) {
		this.config = config;
		
		robot.setup(config);
		humanHijack.setup(config);
		
		if (config.gui != null) {
			myKeyListener = new MyKeyListener();
			config.gui.addKeyListener(myKeyListener);
			
			bot.getBot().setGUI(config.gui);
		}
	}
	
	@Override
	public String getStartingRegion(long timeOut, ArrayList<Region> pickableRegions)
	{
		if (hijacked) {
			return humanHijack.getStartingRegion(timeOut, pickableRegions);			
		}
		return robot.getStartingRegion(timeOut, pickableRegions);
	}
	
	@Override
	public String getPlaceArmiesMoves(long timeOut)
	{
		if (hijacked) {
			return humanHijack.getPlaceArmiesMoves(timeOut);		
		}
		return robot.getPlaceArmiesMoves(timeOut);
	}
	
	@Override
	public String getAttackTransferMoves(long timeOut)
	{
		if (hijacked) {
			return humanHijack.getAttackTransferMoves(timeOut);	
		}
		return robot.getAttackTransferMoves(timeOut);
	}
	
	@Override
	public void writeInfo(String info){
		robot.writeInfo(info);
		humanHijack.writeInfo(info);
	}

	public boolean isRunning() {
		if (robot == null || bot == null) return false;
		synchronized(mutex) {
			// BOT & ROBOT RUNNING?
			if (robot != null && robot.isRunning() && bot != null && bot.isAlive()) return true;
			
			// SOMEONE DIED OFF...
			finishInternal();
		}		
		return false;
	}
	
	public void finish() {
		if (!isRunning()) return;
		synchronized(mutex) {			
			finishInternal();
		}
	}
	
	private void finishInternal() {
		if (config.gui != null) {
			config.gui.removeKeyListener(myKeyListener);
		}
		if (bot != null) {
			try {
				bot.interrupt();
			} catch (Exception e) {				
			}
			bot = null;			
		}
		if (robot != null) {
			try {
				robot.finish();
			} catch (Exception e) {			
			}
			robot = null;
		}		
		if (humanHijack != null) {
			try {
				humanHijack.finish();
			} catch (Exception e) {				
			}
			humanHijack = null;
		}
	}

	@Override
	public int getRobotPlayer() {
		if (config == null) return 0;
		return config.player;
	}
	
	public String getRobotPlayerName() {
		if (config == null) return botFQCN.substring(1+botFQCN.lastIndexOf("."));
		return botFQCN.substring(1+botFQCN.lastIndexOf("."));
	}

}
