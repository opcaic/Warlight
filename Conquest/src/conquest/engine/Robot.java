// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package conquest.engine;

import java.util.ArrayList;

import conquest.engine.replay.GameLog;
import conquest.game.Team;
import conquest.view.GUI;
import conquest.game.world.Region;

public interface Robot {
	
	public static class RobotConfig {
		
		public final int player;
		
		public final String playerName;
		
		public final Team team;
		
		public final long timeoutMillis;
		
		public final GameLog gameLog;
		
		public final boolean logToConsole;
		
		public final GUI gui;

		public RobotConfig(int player, String playerName, Team team, long timeoutMillis,
				           GameLog gameLog, boolean logToConsole, GUI gui) {
			super();
			this.player = player;
			this.playerName = playerName;
			this.team = team;
			this.timeoutMillis = timeoutMillis;
			this.gameLog = gameLog;
			this.logToConsole = logToConsole;
			this.gui = gui;
		}
		
	}
	
	public int getRobotPlayer();
	
	public String getRobotPlayerName();

	public void setup(RobotConfig config);
	
	//public void writeMove(Move move);
	
	public String getStartingRegion(long timeOut, ArrayList<Region> pickableRegions);
	
	public String getPlaceArmiesMoves(long timeOut);
	
	public String getAttackTransferMoves(long timeOut);
	
	public void writeInfo(String info);

	/**
	 * Whether this robot is up and running correctly...
	 * @return
	 */
	public boolean isRunning();
	
	/**
	 * Kills the robot.
	 */
	public void finish();

}
